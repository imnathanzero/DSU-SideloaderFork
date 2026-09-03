package vegabobo.dsusideloader.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vegabobo.dsusideloader.IPrivilegedService

object PrivilegedProvider {

    private val tag = this.javaClass.simpleName
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    var connection = Connection()

    fun run(
        onFail: () -> Unit = {},
        onConnected: suspend IPrivilegedService.() -> Unit,
    ) {
        scope.launch {
            if (isConnected()) {
                connection.SERVICE?.let { onConnected(it) }
                return@launch
            }
            var timeout = 0
            while (!isConnected()) {
                timeout += 1000
                if (timeout > 20000) {
                    Log.e(tag, "Service unavailable.")
                    onFail()
                    return@launch
                }
                delay(1000)
                Log.d(tag, "Service unavailable, checking again in 1s.. [${timeout / 1000}s/20s]")
            }
            connection.SERVICE?.let {
                Log.d(tag, "IPrivilegedService available, uid: ${it.uid}")
                onConnected(it)
            }
        }
    }

    // Blocking for legacy callers. Prefer run() for asynchronous work.
    fun getService(): IPrivilegedService {
        var timeout = 0
        while (!isConnected()) {
            timeout += 1000
            if (timeout > 20000) {
                throw Exception("Service unavailable.")
            }
            Thread.sleep(1000)
        }
        return connection.SERVICE ?: throw Exception("Service disconnected.")
    }

    // Blocking for legacy callers.
    fun isRoot(): Boolean = getService().uid == 0

    fun isConnected(): Boolean = connection.SERVICE != null
}
