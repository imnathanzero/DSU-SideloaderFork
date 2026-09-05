package vegabobo.dsusideloader.ui.screen.about

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import vegabobo.dsusideloader.BuildConfig
import vegabobo.dsusideloader.core.BaseViewModel
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.util.isBuildSignedByAuthor
import vegabobo.dsusideloader.util.isUpdateOfInstalledApp

@Serializable
data class UpdaterResponse(
    val identifier: String = "",
    val versionCode: Int = -1,
    val versionName: String = "",
    val apkUrl: String = "",
    val changelogUrl: String = "",
)

@HiltViewModel
class AboutViewModel @Inject constructor(
    val application: Application,
    override val dataStore: DataStore<Preferences>,
) : BaseViewModel(dataStore) {
    private val tag = this.javaClass.simpleName

    private val _uiState = MutableStateFlow(AboutScreenUiState())
    val uiState: StateFlow<AboutScreenUiState> = _uiState.asStateFlow()
    var response = UpdaterResponse()

    var developerOptionsCounter = 0

    /**
     * Extra fields in the feed must not break parsing: the feed is shared with
     * newer app versions that may publish keys this build does not know.
     */
    private val json = Json { ignoreUnknownKeys = true }

    init {
        val isSignedByAuthor = application.isBuildSignedByAuthor()
        _uiState.update { it.copy(isUpdaterAvailable = isSignedByAuthor || BuildConfig.DEBUG) }
    }

    /** The feed's changelog link, or `null` when it is absent or not an allowed host. */
    val changelogUrl: String?
        get() = response.changelogUrl.takeIf { AppPrefs.isAllowedUpdateUrl(it) }

    fun resetDeveloperOptionsCounter() {
        developerOptionsCounter = 0
    }

    fun onToastDisplayed() {
        _uiState.value.toastDisplay.update { DevOptToastDisplay.NONE }
    }

    private fun updateUpdaterCard(update: (UpdaterCardState) -> UpdaterCardState) =
        _uiState.update { it.copy(updaterCardState = update(it.updaterCardState.copy())) }

    fun onClickCheckUpdates() {
        if (uiState.value.updaterCardState.updateStatus == UpdateStatus.CHECKING_FOR_UPDATES) {
            return
        }
        Log.d(tag, "Fetching updates from: ${AppPrefs.UPDATE_CHECK_URL}")
        updateUpdaterCard { it.copy(updateStatus = UpdateStatus.CHECKING_FOR_UPDATES) }
        viewModelScope.launch(Dispatchers.IO) {
            val feed = readAllowed(AppPrefs.UPDATE_CHECK_URL, MAX_FEED_BYTES)
            if (feed == null) {
                updateUpdaterCard { it.copy(updateStatus = UpdateStatus.FAILED) }
                return@launch
            }
            val parsed = try {
                json.decodeFromString(UpdaterResponse.serializer(), feed)
            } catch (e: Exception) {
                Log.w(tag, "Malformed updater feed", e)
                updateUpdaterCard { it.copy(updateStatus = UpdateStatus.FAILED) }
                return@launch
            }
            response = parsed
            Log.d(tag, "$parsed")

            val hasUpdate = parsed.versionCode > BuildConfig.VERSION_CODE &&
                AppPrefs.isAllowedUpdateUrl(parsed.apkUrl)
            updateUpdaterCard {
                it.copy(
                    updateVersion = parsed.versionName,
                    updateStatus =
                    if (hasUpdate) UpdateStatus.UPDATE_FOUND else UpdateStatus.NO_UPDATE_FOUND,
                )
            }
        }
    }

    fun onClickDownloadUpdate() {
        if (uiState.value.updaterCardState.isDownloading) {
            return
        }
        val apkUrl = response.apkUrl
        if (!AppPrefs.isAllowedUpdateUrl(apkUrl)) {
            Log.w(tag, "Refusing to download update from a host that is not allowed")
            updateUpdaterCard { it.copy(updateStatus = UpdateStatus.FAILED) }
            return
        }
        updateUpdaterCard { it.copy(isDownloading = true, progressBar = 0F) }
        viewModelScope.launch(Dispatchers.IO) {
            val apk = downloadUpdate(apkUrl)
            updateUpdaterCard { it.copy(isDownloading = false, progressBar = 0F) }
            if (apk == null) {
                updateUpdaterCard { it.copy(updateStatus = UpdateStatus.FAILED) }
                return@launch
            }
            startInstall(apk)
        }
    }

    /**
     * Downloads [apkUrl] into a private file and returns it, or `null` when the
     * download failed or produced something that is not an update of this app.
     */
    private fun downloadUpdate(apkUrl: String): File? {
        val updatesDir = File(application.filesDir, UPDATES_DIR)
        val target = File(updatesDir, UPDATE_FILE)
        var connection: HttpURLConnection? = null
        try {
            updatesDir.mkdirs()
            connection = openAllowed(apkUrl) ?: return null
            val expectedLength = connection.contentLengthLong
            if (expectedLength > MAX_APK_BYTES) {
                Log.w(tag, "Update apk is larger than the accepted maximum")
                return null
            }
            var received = 0L
            connection.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var lastPublished = -1
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) {
                            break
                        }
                        received += read
                        if (received > MAX_APK_BYTES) {
                            Log.w(tag, "Update apk exceeded the accepted maximum mid-download")
                            return null
                        }
                        output.write(buffer, 0, read)
                        if (expectedLength > 0) {
                            // Publishing every 8 KiB chunk recomposes the card hundreds of
                            // times per second for no visible gain; whole percents suffice.
                            val percent = (received * 100 / expectedLength).toInt()
                            if (percent != lastPublished) {
                                lastPublished = percent
                                updateUpdaterCard { it.copy(progressBar = percent / 100F) }
                            }
                        }
                    }
                    output.fd.sync()
                }
            }
            if (expectedLength > 0 && received != expectedLength) {
                Log.w(tag, "Update apk is truncated: $received of $expectedLength bytes")
                return null
            }
            if (!application.isUpdateOfInstalledApp(target)) {
                Log.w(tag, "Downloaded apk is not a signature-matching update of this app")
                return null
            }
            return target
        } catch (e: Exception) {
            Log.w(tag, "Update download failed", e)
            return null
        } finally {
            connection?.disconnect()
            if (target.isFile && target.length() == 0L) {
                target.delete()
            }
        }
    }

    private fun startInstall(apk: File) {
        val apkUri = FileProvider.getUriForFile(
            application,
            BuildConfig.APPLICATION_ID + ".provider",
            apk,
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(apkUri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            application.startActivity(intent)
        } catch (e: Exception) {
            Log.w(tag, "No handler for the package installer intent", e)
            updateUpdaterCard { it.copy(updateStatus = UpdateStatus.FAILED) }
        }
    }

    /** Opens [url] only when it passes [AppPrefs.isAllowedUpdateUrl], redirects included. */
    private fun openAllowed(url: String): HttpURLConnection? {
        var current = url
        repeat(MAX_REDIRECTS) {
            if (!AppPrefs.isAllowedUpdateUrl(current)) {
                Log.w(tag, "Rejected updater host")
                return null
            }
            val connection = (URL(current).openConnection() as HttpURLConnection).apply {
                // Following redirects internally would skip the host check, so they are
                // resolved here one hop at a time.
                instanceFollowRedirects = false
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
            }
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> return connection

                HttpURLConnection.HTTP_MOVED_PERM,
                HttpURLConnection.HTTP_MOVED_TEMP,
                HttpURLConnection.HTTP_SEE_OTHER,
                HTTP_TEMPORARY_REDIRECT,
                HTTP_PERMANENT_REDIRECT,
                -> {
                    val location = connection.getHeaderField("Location")
                    connection.disconnect()
                    if (location.isNullOrEmpty()) {
                        return null
                    }
                    current = URL(URL(current), location).toString()
                }

                else -> {
                    Log.w(tag, "Updater request failed: HTTP ${connection.responseCode}")
                    connection.disconnect()
                    return null
                }
            }
        }
        Log.w(tag, "Too many redirects while fetching the update")
        return null
    }

    private fun readAllowed(url: String, limit: Int): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = openAllowed(url) ?: return null
            connection.inputStream.use { input ->
                val bytes = ByteArray(limit)
                var filled = 0
                while (filled < limit) {
                    val read = input.read(bytes, filled, limit - filled)
                    if (read == -1) {
                        break
                    }
                    filled += read
                }
                String(bytes, 0, filled, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not read $url", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    fun onClickImage() {
        developerOptionsCounter++
        if (developerOptionsCounter > 7) {
            resetDeveloperOptionsCounter()
            viewModelScope.launch {
                val newDevOptPrefValue = !readBoolPref(AppPrefs.DEVELOPER_OPTIONS)
                Log.d(tag, "newDevOptPrefValue: $newDevOptPrefValue")
                updateBoolPref(
                    AppPrefs.DEVELOPER_OPTIONS,
                    newDevOptPrefValue,
                ) { preferenceValue ->
                    _uiState.value.toastDisplay.update {
                        if (preferenceValue) {
                            DevOptToastDisplay.ENABLED_DEV_OPT
                        } else {
                            DevOptToastDisplay.DISABLED_DEV_OPT
                        }
                    }
                }
                // if developer options have been disabled
                // then restore developer preferences to their default values
                if (!newDevOptPrefValue) {
                    updateBoolPref(AppPrefs.DISABLE_STORAGE_CHECK, false)
                    updateBoolPref(AppPrefs.FULL_LOGCAT_LOGGING, false)
                }
            }
        }
    }

    private companion object {
        const val UPDATES_DIR = "updates"
        const val UPDATE_FILE = "update.apk"
        const val MAX_FEED_BYTES = 8 * 1024
        const val MAX_APK_BYTES = 256L * 1024L * 1024L
        const val MAX_REDIRECTS = 5
        const val TIMEOUT_MS = 15_000
        const val HTTP_TEMPORARY_REDIRECT = 307
        const val HTTP_PERMANENT_REDIRECT = 308
    }
}
