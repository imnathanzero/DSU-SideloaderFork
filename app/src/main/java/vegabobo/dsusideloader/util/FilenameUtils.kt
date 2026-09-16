package vegabobo.dsusideloader.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile

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
         * Tries to convert DocumentFile uri to real path
         * isn't guaranteed that will work with all kinds of path
         */
        fun getFilePath(uri: Uri, addQuotes: Boolean = false): String {
            val input = uri.path.toString()
            val finalPath = try {
                if (input.contains("/document/")) {
                    val safStorage = input.split("/document/")[1].replace("/tree/", "")
                    val path = safStorage.split(":")[1]
                    if (path.contains("/storage/emulated")) {
                        "file://$path"
                    } else if (safStorage.contains("primary")) {
                        "file:///storage/emulated/0/$path"
                    } else {
                        "file:///storage/" + safStorage.replace(":", "/")
                    }
                } else {
                    uri.toString()
                }
            } catch (_: Exception) {
                uri.toString()
            }
            return if (addQuotes) "'$finalPath'" else finalPath
        }

        fun queryName(resolver: ContentResolver, uri: Uri): String {
            return try {
                resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        cursor.getString(nameIndex) ?: (uri.lastPathSegment ?: "")
                    } else {
                        uri.lastPathSegment ?: ""
                    }
                } ?: (uri.lastPathSegment ?: "")
            } catch (_: Exception) {
                uri.lastPathSegment ?: ""
            }
        }

        fun getDigits(input: String): String {
            return appendToDigitsToString(input, "")
        }

        fun getLengthFromFile(context: Context, uri: Uri): Long {
            return try {
                DocumentFile.fromSingleUri(context, uri)?.length() ?: -1L
            } catch (_: Exception) {
                -1L
            }
        }
    }
}
