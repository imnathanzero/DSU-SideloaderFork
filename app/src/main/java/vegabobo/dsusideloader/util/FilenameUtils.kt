package vegabobo.dsusideloader.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import vegabobo.dsusideloader.model.DSUConstants

class FilenameUtils {

    companion object {

        /**
         * Append text to the end of all digits containing in a string
         * @param input String containing digits
         * @param textToAppend Text that will be appended
         * @return Formatted string, if there is no digits in "input", a empty string will be returned.
         */
        fun appendToDigitsToString(input: String, textToAppend: String): String {
            var newText = input.filter { it.isDigit() } + textToAppend
            if (newText == textToAppend) {
                newText = ""
            }
            return newText
        }

        /**
         * Wraps [value] in single quotes so a POSIX shell treats it as one literal
         * argument, escaping any single quote it already contains.
         *
         * File names come from the user's own storage and may contain spaces, quotes,
         * `$` or `;`. This string is interpolated into a root/adb shell command, so it
         * has to be quoted as a whole rather than piecewise.
         */
        fun shellQuote(value: String): String =
            "'" + value.replace("'", "'\\''") + "'"

        /**
         * Tries to convert DocumentFile uri to real path
         * isn't guaranteed that will work with all kinds of path
         *
         * @param addQuotes shell-quote the result, for use inside a shell command
         */
        fun getFilePath(uri: Uri, addQuotes: Boolean = false): String {
            val fileUri = resolveFileUri(uri)
            return if (addQuotes) shellQuote(fileUri) else fileUri
        }

        private fun resolveFileUri(uri: Uri): String {
            val input = uri.path ?: return ""

            // Document uris look like /document/primary:Download/gsi.img or
            // /tree/AE5C-6D79:dsu, and "raw" ones embed an absolute path directly.
            // Anything that does not match falls back to whatever path we were given.
            val documentPart = if (input.contains("/document/")) {
                input.substringAfter("/document/")
            } else {
                input
            }
            val safStorage = documentPart.replace("/tree/", "")

            if (!safStorage.contains(":")) {
                return "file://$safStorage"
            }

            val path = safStorage.substringAfter(":")
            if (path.contains("/storage/emulated")) {
                return "file://$path"
            }
            return if (safStorage.contains("primary")) {
                "file:///storage/emulated/0/$path"
            } else {
                "file:///storage/" + safStorage.replace(":", "/")
            }
        }

        /**
         * Resolves the display name of [uri], falling back to its last path segment
         * when the provider exposes no DISPLAY_NAME column.
         */
        fun queryName(resolver: ContentResolver, uri: Uri): String {
            val fallback = uri.lastPathSegment?.substringAfterLast('/') ?: ""
            val cursor = try {
                resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            } catch (_: Exception) {
                null
            } ?: return fallback
            return cursor.use {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex == -1 || !it.moveToFirst()) {
                    return@use fallback
                }
                it.getString(nameIndex) ?: fallback
            }
        }

        fun getDigits(input: String): String {
            return appendToDigitsToString(input, "")
        }

        /**
         * Length of the file behind [uri], or [DSUConstants.DEFAULT_IMAGE_SIZE] when it
         * cannot be determined.
         */
        fun getLengthFromFile(context: Context, uri: Uri): Long {
            val length = DocumentFile.fromSingleUri(context, uri)?.length()
                ?: return DSUConstants.DEFAULT_IMAGE_SIZE
            return if (length > 0L) length else DSUConstants.DEFAULT_IMAGE_SIZE
        }
    }
}
