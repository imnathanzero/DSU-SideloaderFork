package vegabobo.dsusideloader.service

import android.app.IActivityManager
import android.content.Intent
import android.content.pm.IPackageManager
import android.gsi.GsiProgress
import android.gsi.IGsiService
import android.os.Build
import android.os.Binder
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.os.Process
import android.os.SystemProperties
import android.os.image.IDynamicSystemService
import android.os.storage.IStorageManager
import android.os.storage.VolumeInfo
import android.util.Log
import kotlin.system.exitProcess
import org.lsposed.hiddenapibypass.HiddenApiBypass
import vegabobo.dsusideloader.BuildConfig
import vegabobo.dsusideloader.IPrivilegedService

class PrivilegedService(private val allowedCallerUid: Int) : IPrivilegedService.Stub() {

    override fun onTransact(code: Int, data: android.os.Parcel, reply: android.os.Parcel?, flags: Int): Boolean {
        enforceCaller()
        return super.onTransact(code, data, reply, flags)
    }

    private fun enforceCaller() {
        val callingUid = Binder.getCallingUid()
        if (callingUid != allowedCallerUid) {
            Log.w(BuildConfig.APPLICATION_ID, "Rejected privileged Binder caller uid=$callingUid")
            throw SecurityException("Unauthorized caller")
        }
    }

    override fun exit() {
        destroy()
    }

    override fun destroy() {
        exitProcess(0)
    }

    private fun getBinder(service: String): IBinder {
        val serviceManager = Class.forName("android.os.ServiceManager")
        val binder = HiddenApiBypass.invoke(serviceManager, null, "getService", service)
        return binder as IBinder
    }

    fun setProp(key: String, value: String) {
        try {
            SystemProperties.set(key, value)
        } catch (e: Exception) {
            Log.w(BuildConfig.APPLICATION_ID, e.stackTraceToString())
        }
    }

    override fun setDynProp() {
        setProp("persist.sys.fflag.override.settings_dynamic_system", "true")
    }

    override fun getUid(): Int {
        return Process.myUid()
    }

    private var ACTIVITY_MANAGER: IActivityManager? = null

    private fun requiresActivityManager() {
        if (ACTIVITY_MANAGER == null) {
            ACTIVITY_MANAGER = IActivityManager.Stub.asInterface(getBinder("activity"))
        }
    }

    override fun startActivity(intent: Intent?) {
        requiresActivityManager()
        val component = intent?.component
        val allowed =
            (component?.packageName == "com.android.dynsystem" &&
                component.className == "com.android.dynsystem.VerificationActivity") ||
                (component?.packageName == BuildConfig.APPLICATION_ID &&
                    component.className == "${BuildConfig.APPLICATION_ID}.MainActivity")
        if (!allowed) {
            throw SecurityException("Activity is not allowed")
        }

        val callerPackage =
            if (uid == 2000 || uid == 0) "com.android.shell" else BuildConfig.APPLICATION_ID
        if (Build.VERSION.SDK_INT > 29) {
            ACTIVITY_MANAGER!!.startActivityAsUserWithFeature(
                null, callerPackage, null, intent, null, null, null,
                0, 0, null, null, 0,
            )
        } else {
            ACTIVITY_MANAGER!!.startActivityAsUser(
                null, callerPackage, intent, null, null, null,
                0, 0, null, null, 0,
            )
        }
    }

    override fun forceStopPackage(packageName: String?) {
        if (packageName != "com.android.dynsystem" && packageName != BuildConfig.APPLICATION_ID) {
            throw SecurityException("Package is not allowed")
        }
        requiresActivityManager()
        ACTIVITY_MANAGER!!.forceStopPackage(packageName, 0)
    }

    private var PACKAGE_MANAGER: IPackageManager? = null

    private fun requiresPackageManager() {
        if (PACKAGE_MANAGER == null) {
            PACKAGE_MANAGER = IPackageManager.Stub.asInterface(getBinder("package"))
        }
    }

    override fun grantPermission(permissionName: String?) {
        if (permissionName != "android.permission.READ_LOGS") {
            throw SecurityException("Permission is not allowed")
        }
        requiresPackageManager()
        PACKAGE_MANAGER!!.grantRuntimePermission(BuildConfig.APPLICATION_ID, permissionName, 0)
    }

    private var STORAGE_MANAGER: IStorageManager? = null

    private fun requiresStorageManager() {
        if (STORAGE_MANAGER == null) {
            STORAGE_MANAGER = IStorageManager.Stub.asInterface(getBinder("mount"))
        }
    }

    override fun getVolumes(): List<VolumeInfo> {
        requiresStorageManager()
        val vols = ArrayList<VolumeInfo>()
        vols.addAll(STORAGE_MANAGER!!.getVolumes(0))
        return vols
    }

    override fun unmount(volId: String?) {
        requiresStorageManager()
        STORAGE_MANAGER!!.unmount(volId)
    }

    override fun mount(volId: String?) {
        requiresStorageManager()
        STORAGE_MANAGER!!.mount(volId)
    }

    private var DYNAMIC_SYSTEM: IDynamicSystemService? = null

    private fun requiresDynamicSystem() {
        if (DYNAMIC_SYSTEM == null) {
            DYNAMIC_SYSTEM = IDynamicSystemService.Stub.asInterface(getBinder("dynamic_system"))
        }
    }

    override fun closePartition(): Boolean {
        if (Build.VERSION.SDK_INT <= 30) return true
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.closePartition()
    }

    override fun finishInstallation(): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.finishInstallation()
    }

    override fun getInstallationProgress(): GsiProgress? {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.installationProgress
    }

    override fun abort(): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.abort()
    }

    override fun isEnabled(): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.isEnabled
    }

    override fun remove(): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.remove()
    }

    override fun setEnable(enable: Boolean, oneShot: Boolean): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.setEnable(enable, oneShot)
    }

    override fun startInstallation(dsuSlot: String?): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.startInstallation(dsuSlot)
    }

    override fun createPartition(name: String?, size: Long, readOnly: Boolean): Int {
        requiresDynamicSystem()
        if (Build.VERSION.SDK_INT < 33) {
            val result = HiddenApiBypass.invoke(
                DYNAMIC_SYSTEM!!.javaClass,
                DYNAMIC_SYSTEM!!,
                "createPartition",
                name,
                size,
                readOnly,
            )
            return if (result as Boolean) IGsiService.INSTALL_OK else IGsiService.INSTALL_ERROR_GENERIC
        }
        return DYNAMIC_SYSTEM!!.createPartition(name, size, readOnly)
    }

    override fun setAshmem(fd: ParcelFileDescriptor?, size: Long): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.setAshmem(fd, size)
    }

    override fun submitFromAshmem(bytes: Long): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.submitFromAshmem(bytes)
    }

    override fun suggestScratchSize(): Long {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.suggestScratchSize()
    }

    override fun isInUse(): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.isInUse
    }

    override fun isInstalled(): Boolean {
        requiresDynamicSystem()
        return DYNAMIC_SYSTEM!!.isInstalled
    }
}
