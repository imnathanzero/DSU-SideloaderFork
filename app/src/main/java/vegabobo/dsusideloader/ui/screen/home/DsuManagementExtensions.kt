package vegabobo.dsusideloader.ui.screen.home

import android.content.Intent
import android.net.Uri
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vegabobo.dsusideloader.model.DsuConfig
import vegabobo.dsusideloader.util.CmdRunner
import vegabobo.dsusideloader.util.FilenameUtils

private const val DSU_USERDATA_IMAGE = "/data/gsi/dsu/userdata_gsi.img"

/** Reads the canonical DSU userdata image size from the installed backing image. */
suspend fun HomeViewModel.detectInstalledUserdataSize(): Long? = withContext(Dispatchers.IO) {
    if (!uiState.value.isDsuInstalled || !session.isRoot()) return@withContext null
    val output = CmdRunner.run("stat -c %s $DSU_USERDATA_IMAGE 2>/dev/null").trim()
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
