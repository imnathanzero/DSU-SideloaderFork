package vegabobo.dsusideloader.installer.privileged

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import vegabobo.dsusideloader.preparation.InstallationStep
import vegabobo.dsusideloader.util.CmdRunner

class LogcatDiagnostic(
    private val onInstallationError: (error: InstallationStep, errorInfo: String) -> Unit,
    private val onStepUpdate: (step: InstallationStep) -> Unit,
    private val onInstallationProgressUpdate: (progress: Float, partition: String) -> Unit,
    private val onInstallationSuccess: () -> Unit,
    private val onLogLineReceived: () -> Unit,
) {

    private val tag = this.javaClass.simpleName
    var logs = ""
    val isLogging = AtomicBoolean(false)
    var shouldLogEverything = false

    fun startLogging(prependString: String) {
        if (isLogging.get()) {
            destroy()
        }
        logs = ""
        isLogging.set(true)
        Log.d(tag, "startLogging(), logEveryting: $shouldLogEverything, isLogging: ${isLogging.get()}")
        CmdRunner.run("logcat -c")
        val logCmd =
            if (shouldLogEverything) {
                "logcat"
            } else {
                "logcat -v tag gsid:* *:S DynamicSystemService:* *:S DynamicSystemInstallationService:* *:S DynSystemInstallationService:* *:S"
            }
        CmdRunner.runReadEachLine(logCmd) {
            if (logs.isEmpty()) {
                logs = "$prependString\n"
            }

            if (it.contains("DynamicSystemService") && it.contains("startInstallation")) {
                onStepUpdate(InstallationStep.INSTALLING)
                onInstallationProgressUpdate(0F, "userdata")
            }

            if (!isLogging.get()) {
                return@runReadEachLine
            }

            logs += "$it\n"
            onLogLineReceived()

            if (it.contains("We are already running in DynamicSystem")) {
                onInstallationError(InstallationStep.ERROR_ALREADY_RUNNING_DYN_OS, it)
                destroy()
            }

            if (it.contains("realpath failed") && it.contains("Permission denied")) {
                onInstallationError(InstallationStep.ERROR_EXTERNAL_SDCARD_ALLOC, it)
                destroy()
                return@runReadEachLine
            }

            if (it.contains("is below the minimum threshold of")) {
                onInstallationError(InstallationStep.ERROR_NO_AVAIL_STORAGE, it)
                destroy()
                return@runReadEachLine
            }

            if (it.contains("read failed") && it.contains("No such file or directory") && it.contains("f2fs")) {
                onInstallationError(InstallationStep.ERROR_F2FS_WRONG_PATH, it)
                destroy()
                return@runReadEachLine
            }

            if (it.contains("Failed to get stat for block device") && it.contains("Permission denied")) {
                onInstallationError(InstallationStep.ERROR_SELINUX, it)
                destroy()
                return@runReadEachLine
            }

            if (it.contains("File is too fragmented") && it.contains("512")) {
                onInstallationError(InstallationStep.ERROR_EXTENTS, it)
                destroy()
                return@runReadEachLine
            }

            if (it.contains("NOT_STARTED")) {
                if (it.contains("INSTALL_CANCELLED")) {
                    onInstallationError(InstallationStep.ERROR_CANCELED, it)
                } else {
                    onInstallationError(InstallationStep.ERROR, it)
                }
                destroy()
                return@runReadEachLine
            }

            if (it.contains("IN_PROGRESS")) {
                if (it.contains("progress:") && it.contains("partition name:")) {
                    try {
                        val progressRgx = "(progress: )([\\d+/]+)".toRegex()
                        val partitionRgx = "(partition name: ([a-z+_]+))".toRegex()
                        val progressText = progressRgx.find(it)!!.groupValues[2].split("/")
                        val progress = (progressText[0].toFloat() / progressText[1].toFloat())
                        val partitionText = partitionRgx.find(it)!!.groupValues[2]
                        onInstallationProgressUpdate(progress, partitionText)
                    } catch (_: Exception) {
                        onStepUpdate(InstallationStep.PROCESSING_LOG_READABLE)
                    }
                } else {
                    onStepUpdate(InstallationStep.PROCESSING_LOG_READABLE)
                }
            }

            if (it.contains("READY") && it.contains("INSTALL_COMPLETED")) {
                onInstallationSuccess()
                destroy()
                return@runReadEachLine
            }

            if (it.contains("ACTION_CANCEL_INSTALL")) {
                onInstallationError(InstallationStep.ERROR_CANCELED, it)
                destroy()
                return@runReadEachLine
            }

            if (it.contains("postStatus(): statusCode=2")) {
                onStepUpdate(InstallationStep.PROCESSING_LOG_READABLE)
            }

            if (it.contains("postStatus(): statusCode=3")) {
                onInstallationSuccess()
                destroy()
                return@runReadEachLine
            }

            if (it.contains("postStatus(): statusCode=1")) {
                onInstallationError(InstallationStep.ERROR, it)
                destroy()
                return@runReadEachLine
            }
        }
    }

    /** Capture logcat for the built-in installer without interpreting its output as install state. */
    fun startCaptureOnly(prependString: String) {
        if (isLogging.get()) destroy()
        logs = "$prependString\n"
        isLogging.set(true)
        CmdRunner.run("logcat -c")
        val logCmd = if (shouldLogEverything) {
            "logcat"
        } else {
            "logcat -v tag gsid:* *:S DynamicSystemService:* *:S DynamicSystemInstallationService:* *:S DynSystemInstallationService:* *:S"
        }
        CmdRunner.runReadEachLine(logCmd) {
            if (!isLogging.get()) return@runReadEachLine
            logs += "$it\n"
            onLogLineReceived()
        }
    }

    fun destroy() {
        CmdRunner.destroy()
        isLogging.set(false)
        Log.d(tag, "destroy(), isLogging: ${isLogging.get()}")
    }
}
