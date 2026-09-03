package vegabobo.dsusideloader.service

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import vegabobo.dsusideloader.IPrivilegedService

class Connection : ServiceConnection {

    @Volatile
    var SERVICE: IPrivilegedService? = null
        private set

    @Synchronized
    fun set(service: IPrivilegedService?) {
        SERVICE = service
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        set(service?.let(IPrivilegedService.Stub::asInterface))
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        set(null)
    }
}
