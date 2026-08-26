package vegabobo.dsusideloader.ui.screen.home

import android.net.Uri

fun HomeViewModel.saveCurrentLogs(uri: Uri) {
    val logs = logger?.logs ?: uiState.value.installationLogs
    application.contentResolver.openOutputStream(uri)?.bufferedWriter().use { writer ->
        requireNotNull(writer) { "Unable to open destination" }
        writer.write(logs)
    }
}
