package vegabobo.dsusideloader.preferences

import java.net.URI
import java.net.URISyntaxException

object AppPrefs {
    /**
     * Update feature only works if AUTHOR_SIGN_DIGEST is set
     * with same SHA1 digest from signed apk OR is a DEBUG build
     * if AUTHOR_SIGN_DIGEST doesn't match, also no problem
     * app will work as expected, just without update feature.
     * check AboutViewModel init
     */
    const val UPDATE_CHECK_URL =
        "https://raw.githubusercontent.com/imnathanzero/DSU-SideloaderFork/master/other/updater.json"

    /**
     * Only hosts in this list may serve the updater feed and the update apk.
     * Anything else is rejected before a single byte is downloaded.
     */
    val UPDATE_ALLOWED_HOSTS = setOf(
        "raw.githubusercontent.com",
        "github.com",
        "objects.githubusercontent.com",
        "release-assets.githubusercontent.com",
    )

    /**
     * Whether [url] may be fetched by the updater: https only, and an exact host
     * match against [UPDATE_ALLOWED_HOSTS]. The feed is a plain text file in a
     * repository, so a redirect or an edited feed must not be able to point the
     * download at an arbitrary origin.
     */
    fun isAllowedUpdateUrl(url: String): Boolean {
        val parsed = try {
            URI(url)
        } catch (e: URISyntaxException) {
            return false
        }
        if (!"https".equals(parsed.scheme, ignoreCase = true)) {
            return false
        }
        val host = parsed.host?.lowercase() ?: return false
        return host in UPDATE_ALLOWED_HOSTS
    }

    const val AUTHOR_SIGN_DIGEST = "0da046eb480972124e2fe2251ebc5b19ea9e13d9"
    const val USER_PREFERENCES = "user_preferences"
    const val BOOTLOADER_UNLOCKED_WARNING = "bootloader_unlocked_warning"
    const val SAF_PATH = "writable_path"
    const val DEVELOPER_OPTIONS = "developer_options"
    const val USE_BUILTIN_INSTALLER = "builtin_installer"
    const val KEEP_SCREEN_ON = "keep_screen_on"
    const val KEEP_USERDATA = "keep_userdata"
    const val UMOUNT_SD = "umount_sd"
    const val DISABLE_STORAGE_CHECK = "disable_storage_check"
    const val FULL_LOGCAT_LOGGING = "full_logcat_logging"

    /**
     * Default value of every boolean preference, used whenever the key has never
     * been written to DataStore.
     *
     * Keys are intentionally listed even when their default is `false`, so that a
     * missing entry here is a missing preference rather than a silent `false`.
     * KEEP_USERDATA defaults to `true`: losing userdata is destructive and must
     * never be the consequence of an unwritten preference.
     */
    private val BOOL_DEFAULTS = mapOf(
        BOOTLOADER_UNLOCKED_WARNING to false,
        DEVELOPER_OPTIONS to false,
        USE_BUILTIN_INSTALLER to false,
        KEEP_SCREEN_ON to false,
        KEEP_USERDATA to true,
        UMOUNT_SD to false,
        DISABLE_STORAGE_CHECK to false,
        FULL_LOGCAT_LOGGING to false,
    )

    /** Default for [key], or `false` for keys without an explicit default. */
    fun boolDefault(key: String): Boolean = BOOL_DEFAULTS[key] ?: false

    /** Defaults for the preferences shown on the settings screen. */
    fun settingsDefaults(): HashMap<String, Boolean> = hashMapOf(
        USE_BUILTIN_INSTALLER to boolDefault(USE_BUILTIN_INSTALLER),
        KEEP_SCREEN_ON to boolDefault(KEEP_SCREEN_ON),
        KEEP_USERDATA to boolDefault(KEEP_USERDATA),
        UMOUNT_SD to boolDefault(UMOUNT_SD),
        DISABLE_STORAGE_CHECK to boolDefault(DISABLE_STORAGE_CHECK),
        FULL_LOGCAT_LOGGING to boolDefault(FULL_LOGCAT_LOGGING),
    )
}
