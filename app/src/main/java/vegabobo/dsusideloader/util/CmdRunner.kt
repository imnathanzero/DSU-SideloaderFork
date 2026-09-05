package vegabobo.dsusideloader.util

import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object CmdRunner {

    // Android has no /bin/sh before API 30, and even then only as a symlink.
    private const val SHELL = "/system/bin/sh"

    @Volatile
    var process: Process? = null

    fun run(cmd: String): String {
        return if (Shell.getShell().isRoot) {
            Shell.cmd(cmd).exec().out.toString()
        } else {
            runCommand(cmd)
        }
    }

    fun runReadEachLine(cmd: String, onReceive: (String) -> Unit) {
        if (Shell.getShell().isRoot) {
            val callbackList: CallbackList<String> = object : CallbackList<String>() {
                override fun onAddElement(s: String) {
                    onReceive(s)
                }
            }
            Shell.cmd(cmd).to(callbackList).submit()
        } else {
            runCommand(cmd, onReceive)
        }
    }

    private fun runCommand(cmd: String, onReceive: (String) -> Unit) {
        // Fold stderr into stdout: without this the error pipe fills up and the child
        // process blocks forever once it writes more than the pipe buffer.
        val started = ProcessBuilder(SHELL, "-c", cmd)
            .redirectErrorStream(true)
            .start()
        process = started
        try {
            BufferedReader(InputStreamReader(started.inputStream)).use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.isNotEmpty()) onReceive(line)
                }
            }
            started.waitFor()
        } catch (_: IOException) {
        } catch (_: InterruptedException) {
        } finally {
            started.destroy()
            if (process === started) {
                process = null
            }
        }
    }

    private fun runCommand(cmd: String): String {
        val output = StringBuilder()
        runCommand(cmd) {
            output.append(it).append('\n')
        }
        return output.toString()
    }

    fun destroy() {
        if (Shell.getShell().isRoot) {
            Shell.getShell().close()
            Shell.getShell()
            return
        }
        process?.destroy()
        process = null
    }
}
