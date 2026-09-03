package vegabobo.dsusideloader.service

import android.app.IActivityManager
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
