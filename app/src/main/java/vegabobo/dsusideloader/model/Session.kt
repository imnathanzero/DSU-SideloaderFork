package vegabobo.dsusideloader.model

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import vegabobo.dsusideloader.util.FilenameUtils
import vegabobo.dsusideloader.util.OperationMode

data class InstallationPreferences(
    var isUnmountSdCard: Boolean = false,
    var useBuiltinInstaller: Boolean = false,
    var preserveUserdata: Boolean = true,
)

class UserSelection(
    var userSelectedUserdata: Long = DSUConstants.DEFAULT_USERDATA,
    var selectedFileUri: Uri = Uri.EMPTY,
    var selectedFileName: String = "",
) {

    fun getUserDataSizeAsGB(): String = "${(this.userSelectedUserdata / 1024L / 1024L / 1024L)}"

    fun setUserDataSize(size: String) {
        userSelectedUserdata =
            if (size.isNotEmpty()) {
                (FilenameUtils.getDigits(size).toLong()) * 1024L * 1024L * 1024L
            } else {
                DSUConstants.DEFAULT_USERDATA
            }
    }

    // The uri and the name identify a file the user picked; they are kept out of
    // toString() so they do not end up in logs or a saved diagnostic report.
    override fun toString(): String {
        return "UserSelection(userSelectedUserdata=$userSelectedUserdata, " +
            "hasSelectedFile=${selectedFileUri != Uri.EMPTY})"
    }
}

class Session(
    var userSelection: UserSelection = UserSelection(),
    var dsuInstallation: DSUInstallationSource = DSUInstallationSource(),
    var preferences: InstallationPreferences = InstallationPreferences(),
    var operationMode: MutableStateFlow<OperationMode> = MutableStateFlow(OperationMode.ADB),
) {

    fun isRoot(): Boolean {
        return (
            operationMode.value == OperationMode.SYSTEM_AND_ROOT ||
                operationMode.value == OperationMode.ROOT
            )
    }

    fun getOperationMode() = operationMode.value
    fun setOperationMode(newOpMode: OperationMode) {
        operationMode.value = newOpMode
    }

    // Only populated on UNROOTED mode
    var installationScriptPath = ""

    fun getInstallationParameters(): Triple<Long, String, Long> {
        val userdataSize = userSelection.userSelectedUserdata
        val absoluteFilePath = FilenameUtils.getFilePath(dsuInstallation.uri, true)
        return Triple(userdataSize, absoluteFilePath, dsuInstallation.fileSize)
    }

    override fun toString(): String {
        return "$userSelection\n$dsuInstallation\n$preferences\noperationMode: ${operationMode.value}"
    }
}
