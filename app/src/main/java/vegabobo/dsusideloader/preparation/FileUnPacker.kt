package vegabobo.dsusideloader.preparation

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Job
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import vegabobo.dsusideloader.core.StorageManager

class FileUnPacker(
    private val storageManager: StorageManager,
    private val inputFile: Uri,
    outputFile: String,
    private val installationJob: Job,
    private val onProgressChange: (Float) -> Unit,
) {

    private var finalFile: DocumentFile = storageManager.createDocumentFile(outputFile)
    private val inputFileSize = storageManager.getFilesizeFromUri(inputFile)

    private fun copy(
        inputStr: InputStream,
        outputStr: OutputStream,
        onReadedBuffer: (Long) -> Unit,
    ) {
        val buffer = ByteArray(8 * 1024)
        var n: Int
        var readed: Long = 0
        while (-1 != inputStr.read(buffer)
                .also { n = it } && !installationJob.isCancelled
        ) {
            readed += n
            onReadedBuffer(readed)
            outputStr.write(buffer, 0, n)
        }
        outputStr.flush()
    }

    fun pack(): Pair<Uri, Long> {
        storageManager.openInputStream(inputFile).use { inputStr ->
            storageManager.openOutputStream(finalFile.uri).use { rawOut ->
                GzipCompressorOutputStream(rawOut).use { gzOut ->
                    copy(inputStr, gzOut) {
                        updateProgress(inputFileSize, it)
                    }
                }
            }
        }
        val fileLength = storageManager.getFilesizeFromUri(finalFile.uri)
        return Pair(finalFile.uri, fileLength)
    }

    fun unpack(): Pair<Uri, Long> {
        storageManager.openInputStream(inputFile).use { rawIn ->
            val filename = storageManager.getFilenameFromUri(inputFile)
            val archiveInputStream: InputStream = when {
                filename.endsWith("xz") -> XZCompressorInputStream(rawIn)
                filename.endsWith("gz") || filename.endsWith("gzip") -> GzipCompressorInputStream(rawIn)
                else -> throw Exception("File type not supported")
            }
            archiveInputStream.use { inputStr ->
                storageManager.openOutputStream(finalFile.uri).use { outputStr ->
                    copy(inputStr, outputStr) {
                        val count = if (archiveInputStream is XZCompressorInputStream) {
                            archiveInputStream.compressedCount
                        } else if (archiveInputStream is GzipCompressorInputStream) {
                            archiveInputStream.compressedCount
                        } else {
                            it
                        }
                        updateProgress(inputFileSize, count)
                    }
                }
            }
        }
        val fileLength = storageManager.getFilesizeFromUri(finalFile.uri)
        return Pair(finalFile.uri, fileLength)
    }

    private fun updateProgress(fileSize: Long, readed: Long) {
        if (fileSize > 0) {
            val percent: Float = (readed.toFloat() / fileSize.toFloat()).coerceIn(0F, 1F)
            onProgressChange(percent)
        }
    }
}
