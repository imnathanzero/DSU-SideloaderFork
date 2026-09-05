package vegabobo.dsusideloader.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.content.pm.SigningInfo
import android.os.Build
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import vegabobo.dsusideloader.preferences.AppPrefs

private const val DIGEST_ALGORITHM = "SHA-1"

/**
 * Check if app was signed using original author's keystore.
 *
 * Update feature should fetch only updates from builds
 * signed with the same keystore as build author.
 *
 * This is done to avoid updater to fetch conflicting signed apks
 * which may lead into installation errors anyway.
 */
fun Context.isBuildSignedByAuthor(): Boolean =
    installedSigningDigests().contains(AppPrefs.AUTHOR_SIGN_DIGEST)

/**
 * Whether [apk] is an update of *this* app, signed with the same key.
 *
 * The platform package installer enforces the same rule, but it only does so after
 * the user has confirmed the install; checking here means a file that could never
 * be installed is deleted instead of handed to the installer.
 */
fun Context.isUpdateOfInstalledApp(apk: File): Boolean {
    if (!apk.isFile || apk.length() == 0L) {
        return false
    }
    val archive = packageManager
        .getPackageArchiveInfo(apk.path, PackageManager.GET_SIGNING_CERTIFICATES)
        ?: return false
    if (archive.packageName != packageName) {
        return false
    }
    val apkDigests = archive.signingDigests()
    if (apkDigests.isEmpty()) {
        return false
    }
    val installedDigests = installedSigningDigests()
    return installedDigests.isNotEmpty() && apkDigests.any { it in installedDigests }
}

private fun Context.installedSigningDigests(): List<String> {
    val packageInfo = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(
                    PackageManager.GET_SIGNING_CERTIFICATES.toLong(),
                ),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        return emptyList()
    }
    return packageInfo.signingDigests()
}

private fun PackageInfo.signingDigests(): List<String> {
    val info: SigningInfo = signingInfo ?: return emptyList()
    val signatures =
        if (info.hasMultipleSigners()) info.apkContentsSigners else info.signingCertificateHistory
    return signatures?.mapNotNull { signatureDigest(it) } ?: emptyList()
}

private fun signatureDigest(signature: Signature): String? {
    return try {
        MessageDigest.getInstance(DIGEST_ALGORITHM)
            .digest(signature.toByteArray())
            .joinToString("") { "%02x".format(it) }
    } catch (e: NoSuchAlgorithmException) {
        null
    }
}
