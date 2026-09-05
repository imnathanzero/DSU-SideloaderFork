package vegabobo.dsusideloader

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import vegabobo.dsusideloader.model.Session
import vegabobo.dsusideloader.service.PrivilegedProvider
import vegabobo.dsusideloader.service.PrivilegedRootService
import vegabobo.dsusideloader.service.PrivilegedService
import vegabobo.dsusideloader.service.PrivilegedSystemService
import vegabobo.dsusideloader.ui.screen.Navigation
import vegabobo.dsusideloader.ui.theme.DSUHelperTheme
import vegabobo.dsusideloader.util.OperationMode
import vegabobo.dsusideloader.util.OperationModeUtils

@AndroidEntryPoint
class MainActivity : ComponentActivity(), Shizuku.OnRequestPermissionResultListener {

    @Inject
    lateinit var session: Session

    private val tag = this.javaClass.simpleName

    private var shouldCheckShizuku = false

    /** Tracks what was actually bound, so onDestroy never unbinds something that was not. */
    private var boundMode: OperationMode? = null
    private var shizukuListenersRegistered = false

    private fun setupSessionOperationMode() {
        val operationMode = OperationModeUtils.getOperationMode(application, shouldCheckShizuku)
        session.setOperationMode(operationMode)
        Log.d(tag, "Operation mode is: $operationMode")
    }

    //
    // Shizuku
    //

    val userServiceArgs =
        Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, PrivilegedService::class.java.name),
        )
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)

    private val SHIZUKU_REQUEST_CODE = 1000
    private val REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionResult

    private fun addShizukuListeners() {
        if (shizukuListenersRegistered) {
            return
        }
        shizukuListenersRegistered = true
        Shizuku.addBinderReceivedListenerSticky(BINDER_RECEIVED_LISTENER)
        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }

    private fun removeShizukuListeners() {
        if (!shizukuListenersRegistered) {
            return
        }
        shizukuListenersRegistered = false
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
        Shizuku.removeBinderReceivedListener(BINDER_RECEIVED_LISTENER)
    }

    private val BINDER_RECEIVED_LISTENER = Shizuku.OnBinderReceivedListener {
        if (!OperationModeUtils.isShizukuPermissionGranted(this)) {
            askShizukuPermission()
            return@OnBinderReceivedListener
        }
        bindShizuku()
    }

    private fun askShizukuPermission() {
        if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) {
            requestPermissions(arrayOf(ShizukuProvider.PERMISSION), SHIZUKU_REQUEST_CODE)
        } else {
            Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult == PackageManager.PERMISSION_GRANTED && requestCode == SHIZUKU_REQUEST_CODE) {
            bindShizuku()
        }
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }

    fun bindShizuku() {
        Shizuku.bindUserService(userServiceArgs, PrivilegedProvider.connection)
        boundMode = OperationMode.SHIZUKU
        shouldCheckShizuku = true
        setupSessionOperationMode()
    }

    //
    // Root
    //

    companion object {
        init {
            // Shell.enableVerboseLogging = BuildConfig.DEBUG
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setFlags(Shell.FLAG_REDIRECT_STDERR)
                    .setTimeout(10),
            )
        }
    }

    private fun setupService() {
        if (session.isRoot()) {
            val privRootService = Intent(this, PrivilegedRootService::class.java)
            RootService.bind(privRootService, PrivilegedProvider.connection)
            boundMode = OperationMode.ROOT
            return
        }

        if (session.getOperationMode() == OperationMode.SYSTEM) {
            val service = Intent(this, PrivilegedSystemService::class.java)
            if (bindService(service, PrivilegedProvider.connection, Context.BIND_AUTO_CREATE)) {
                boundMode = OperationMode.SYSTEM
            }
            return
        }

        addShizukuListeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kick off su negotiation early; the operation-mode check below needs its result.
        Shell.getShell {}
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DSUHelperTheme {
                Navigation()
            }
        }

        if (savedInstanceState == null) {
            setupSessionOperationMode()
            setupService()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        // Scoped to what the privileged service actually reflects into, instead of
        // lifting the hidden-API restriction for the whole process.
        HiddenApiBypass.addHiddenApiExemptions(
            "Landroid/os/ServiceManager;",
            "Landroid/os/SystemProperties;",
            "Landroid/os/image/IDynamicSystemService;",
            "Landroid/os/storage/IStorageManager;",
            "Landroid/os/storage/VolumeInfo;",
            "Landroid/app/IActivityManager;",
            "Landroid/content/pm/IPackageManager;",
            "Landroid/gsi/IGsiService;",
            "Landroid/gsi/GsiProgress;",
        )
        super.attachBaseContext(newBase)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isChangingConfigurations) {
            return
        }
        // Unbinding something that was never bound (or whose process already died)
        // throws IllegalArgumentException, so key off what was actually bound and
        // tolerate a service that has gone away underneath us.
        when (boundMode) {
            OperationMode.ROOT, OperationMode.SYSTEM_AND_ROOT ->
                runCatching { RootService.unbind(PrivilegedProvider.connection) }
                    .onFailure { Log.w(tag, "RootService already unbound", it) }

            OperationMode.SYSTEM ->
                runCatching { applicationContext.unbindService(PrivilegedProvider.connection) }
                    .onFailure { Log.w(tag, "System service already unbound", it) }

            OperationMode.SHIZUKU ->
                runCatching {
                    Shizuku.unbindUserService(userServiceArgs, PrivilegedProvider.connection, true)
                }.onFailure { Log.w(tag, "Shizuku service already unbound", it) }

            else -> {}
        }
        removeShizukuListeners()
        boundMode = null
    }
}
