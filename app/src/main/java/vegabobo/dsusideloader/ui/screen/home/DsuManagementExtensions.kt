package vegabobo.dsusideloader.ui.screen.home

import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import vegabobo.dsusideloader.util.CmdRunner

private const val DSU_INSTALL_DIR_FILE = "/metadata/gsi/dsu/install_dir"
private const val DEFAULT_DSU_INSTALL_DIR = "/data/gsi/dsu"

/** Lightweight JSON configuration used by the DSU backup/restore feature. */
private data class DsuConfig(
    val userdataSize: Long,
    val imageSize: Long,
    val preserveUserdata: Boolean,
    val useBuiltinInstaller: Boolean,
    val selectedFileUri: String,
    val selectedFileName: String,
) {
    fun toJson(): String = JSONObject().apply {
        put("version", 1)
        put("userdataSize", userdataSize)
        put("imageSize", imageSize)
        put("preserveUserdata", preserveUserdata)
        put("useBuiltinInstaller", useBuiltinInstaller)
        put("selectedFileUri", selectedFileUri)
        put("selectedFileName", selectedFileName)
    }.toString(2)

    companion object {
        fun fromJson(value: String): DsuConfig {
            val json = JSONObject(value)
            require(json.optInt("version", 1) == 1) {
                "Unsupported DSU config version"
            }
            return DsuConfig(
                userdataSize = json.getLong("userdataSize"),
                imageSize = json.getLong("imageSize"),
                preserveUserdata = json.optBoolean("preserveUserdata", true),
                useBuiltinInstaller = json.optBoolean("useBuiltinInstaller", false),
                selectedFileUri = json.optString("selectedFileUri", ""),
                selectedFileName = json.optString("selectedFileName", ""),
            )
        }
    }
}

/** Reads the canonical DSU userdata image size from the installed backing image. */
suspend fun HomeViewModel.detectInstalledUserdataSize(): Long? = withContext(Dispatchers.IO) {
    if (!uiState.value.isDsuInstalled || !session.isRoot()) return@withContext null
    val installDir = CmdRunner.run("cat $DSU_INSTALL_DIR_FILE 2>/dev/null").trim()
        .ifEmpty { DEFAULT_DSU_INSTALL_DIR }
    val imagePath = "$installDir/userdata_gsi.img"
    val output = CmdRunner.run("stat -c %s \"$imagePath\" 2>/dev/null").trim()
    output.toLongOrNull()?.takeIf { it > 0L }
}

fun HomeViewModel.saveDsuConfig(uri: Uri) {
    val config = DsuConfig(
        userdataSize = session.userSelection.userSelectedUserdata,
        imageSize = session.userSelection.userSelectedImageSize,
        preserveUserdata = session.preferences.preserveUserdata,
        useBuiltinInstaller = session.preferences.useBuiltinInstaller,
        selectedFileUri = session.userSelection.selectedFileUri.toString(),
        selectedFileName = session.userSelection.selectedFileName,
    )
    runCatching {
        application.contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
            requireNotNull(writer) { "Unable to open destination" }
            writer.write(config.toJson())
        }
    }
}

fun HomeViewModel.restoreDsuConfig(uri: Uri) {
    runCatching {
        application.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }
    runCatching {
        val text = application.contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
            requireNotNull(reader) { "Unable to open config" }
            reader.readText()
        }
        val config = DsuConfig.fromJson(text)

        session.userSelection.userSelectedUserdata = config.userdataSize
        session.userSelection.userSelectedImageSize = config.imageSize
        session.preferences.preserveUserdata = config.preserveUserdata
        session.preferences.useBuiltinInstaller = config.useBuiltinInstaller
        session.userSelection.selectedFileName = config.selectedFileName

        if (config.selectedFileUri.isNotEmpty()) {
            val selectedUri = Uri.parse(config.selectedFileUri)
            session.userSelection.selectedFileUri = selectedUri
            onFileSelectionResult(selectedUri)
        }

        onCheckPreserveUserdata(config.preserveUserdata)
        updateUserdataSize(session.userSelection.getUserDataSizeAsGB())
        if (session.userSelection.isCustomImageSize()) {
            updateImageSize(session.userSelection.userSelectedImageSize.toString())
        }
    }
}
