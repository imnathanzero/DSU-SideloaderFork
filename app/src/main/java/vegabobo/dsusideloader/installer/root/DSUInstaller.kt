package vegabobo.dsusideloader.installer.root

import android.app.Application
import android.gsi.IGsiService
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.SharedMemory
import android.util.Log
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.lsposed.hiddenapibypass.HiddenApiBypass
import vegabobo.dsusideloader.model.DSUInstallationSource
import vegabobo.dsusideloader.model.ImagePartition
import vegabobo.dsusideloader.model.Type
import vegabobo.dsusideloader.preparation.InstallationStep
import vegabobo.dsusideloader.service.PrivilegedProvider

/**
 * DSU Installer implementation using Android APIs
 * Based on InstallationAsyncTask from DynamicSystemInstallationService
 * DynamicSystemInstallationService/src/com/android/dynsystem/InstallationAsyncTask.java
 *
 * Calling APIs directly to install images are fast, because we can apply images directly
 * instead of preparing a file exclusively to install via DSU system-app
 * also, having access to APIs make everything more flexible.
 *
 * Unfortunately, this implementation has a downside, it requires "MANAGE_DYNAMIC_SYSTEM"
 * and this permission has a protection level of "signature".
 *
 * That's why this installation way requires root.
 */
class DSUInstaller(
    private val application: Application,
    private val userdataSize: Long,
    private val dsuInstallation: DSUInstallationSource,
    private var installationJob: Job = Job(),
    private val onInstallationError: (error: InstallationStep, errorInfo: String) -> Unit,
    private val onInstallationProgressUpdate: (progress: Float, partition: String) -> Unit,
    private val onCreatePartition: (partition: String) -> Unit,
    private val onInstallationStepUpdate: (step: InstallationStep) -> Unit,
    private val onInstallationSuccess: () -> Unit,
) : () -> Unit, DynamicSystemImpl() {

    private val tag = this.javaClass.simpleName
    private var installationStarted = false
    private var installationFinished = false

    object Constants {
        const val DEFAULT_SLOT = "dsu"
        const val SHARED_MEM_SIZE: Int = 524288
        const val MIN_PROGRESS_TO_PUBLISH = (1 shl 27).toLong()
    }

    private class MappedMemoryBuffer(var mBuffer: ByteBuffer?) :
        AutoCloseable {
        override fun close() {
            if (mBuffer != null) {
                SharedMemory.unmap(mBuffer!!)
                mBuffer = null
            }
        }
    }

    private fun isPartitionSupported(partitionName: String): Boolean =
        DSUImageValidator.isPartitionSupported(partitionName)

    private fun getFdDup(sharedMemory: SharedMemory): ParcelFileDescriptor {
        return HiddenApiBypass.invoke(
            sharedMemory.javaClass,
            sharedMemory,
            "getFdDup",
        ) as ParcelFileDescriptor
    }

    private fun shouldInstallEntry(entry: ZipEntry): Boolean {
        if (entry.isDirectory) return false
        return DSUImageValidator.partitionNameFromImageEntry(entry.name) != null
    }

    private fun publishProgress(bytesRead: Long, totalBytes: Long, partition: String) {
        var progress = 0F
        if (totalBytes != 0L && bytesRead != 0L) {
            progress = (bytesRead.toFloat() / totalBytes.toFloat())
        }
        onInstallationProgressUpdate(progress, partition)
    }

    private fun installWritablePartition(
        partition: String,
        partitionSize: Long,
        readOnly: Boolean = false,
    ) {
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            if (!createNewPartition(partition, partitionSize, readOnly)) {
                return@launch
            }
            job.complete()
        }
        publishProgress(0L, partitionSize, partition)
        var prevInstalledSize = 0L
        while (job.isActive) {
            val installedSize = installationProgress.bytes_processed
            if (installedSize > prevInstalledSize + Constants.MIN_PROGRESS_TO_PUBLISH) {
                prevInstalledSize = installedSize
                publishProgress(installedSize, partitionSize, partition)
            }
            runBlocking { delay(100) }
        }
        if (installationJob.isCancelled) {
            return
        }
        if (!closePartition()) {
            Log.e(tag, "Failed to install $partition partition")
            installationJob.cancel()
            onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, partition)
            return
        }

        if (prevInstalledSize != partitionSize) {
            publishProgress(partitionSize, partitionSize, partition)
        }
        Log.d(
            tag,
            "Partition $partition installed, readOnly: $readOnly, partitionSize: $partitionSize",
        )
    }

    private fun installImage(
        partition: String,
        uncompressedSize: Long,
        inputStream: InputStream,
        readOnly: Boolean = true,
    ) {
        val sis = SparseInputStream(
            BufferedInputStream(inputStream),
        )
        val partitionSize = if (sis.unsparseSize != -1L) sis.unsparseSize else uncompressedSize
        if (partitionSize <= 0L) {
            installationJob.cancel()
            onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, partition)
            return
        }
        onCreatePartition(partition)
        if (!createNewPartition(partition, partitionSize, readOnly)) {
            return
        }
        onInstallationStepUpdate(InstallationStep.INSTALLING_ROOTED)
        SharedMemory.create("dsu_buffer_$partition", Constants.SHARED_MEM_SIZE)
            .use { sharedMemory ->
                MappedMemoryBuffer(sharedMemory.mapReadWrite()).use { mappedBuffer ->
                    val fdDup = getFdDup(sharedMemory)
                    if (!setAshmem(fdDup, sharedMemory.size.toLong())) {
                        Log.e(tag, "Failed to set ashmem for $partition")
                        installationJob.cancel()
                        onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, partition)
                        return
                    }
                    publishProgress(0L, partitionSize, partition)
                    var installedSize: Long = 0
                    val readBuffer = ByteArray(sharedMemory.size)
                    val buffer = mappedBuffer.mBuffer
                    var numBytesRead: Int
                    while (0 < sis.read(readBuffer, 0, readBuffer.size)
                            .also { numBytesRead = it }
                    ) {
                        if (installationJob.isCancelled) {
                            return
                        }
                        buffer!!.position(0)
                        buffer.put(readBuffer, 0, numBytesRead)
                        if (!submitFromAshmem(numBytesRead.toLong())) {
                            Log.e(tag, "Failed to submit ashmem data for $partition")
                            installationJob.cancel()
                            onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, partition)
                            return
                        }
                        installedSize += numBytesRead.toLong()
                        publishProgress(installedSize, partitionSize, partition)
                    }
                    publishProgress(partitionSize, partitionSize, partition)
                }
            }

        if (installationJob.isCancelled) {
            return
        }
        if (!closePartition()) {
            Log.d(tag, "Failed to install $partition partition")
            installationJob.cancel()
            onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, partition)
            return
        }
        Log.d(
            tag,
            "Partition $partition installed, readOnly: $readOnly, partitionSize: $partitionSize",
        )
    }

    private fun installStreamingZipUpdate(inputStream: InputStream): Boolean {
        ZipInputStream(inputStream).use { zis ->
            val installedPartitions = mutableSetOf<String>()
            var entry: ZipEntry?
            while (zis.nextEntry.also { entry = it } != null) {
                val currentEntry = entry!!
                val partitionName = DSUImageValidator.partitionNameFromImageEntry(currentEntry.name)
                if (partitionName != null) {
                    if (!shouldInstallEntry(currentEntry) || currentEntry.size <= 0L) {
                        Log.e(tag, "Invalid DSU image entry: ${currentEntry.name}")
                        installationJob.cancel()
                        onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, currentEntry.name)
                        return false
                    }
                    if (!installedPartitions.add(partitionName)) {
                        Log.e(tag, "Duplicate DSU partition: $partitionName")
                        installationJob.cancel()
                        onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, partitionName)
                        return false
                    }
                    installImageFromAnEntry(currentEntry, zis)
                } else {
                    Log.d(tag, "${currentEntry.name} installation is not supported, skip it.")
                }
                if (installationJob.isCancelled) {
                    return false
                }
            }
        }
        return true
    }

    private fun installImageFromAnEntry(entry: ZipEntry, inputStream: InputStream) {
        val fileName = entry.name
        Log.d(tag, "Installing: $fileName")
        val partitionName = DSUImageValidator.partitionNameFromImageEntry(fileName) ?: return
        val uncompressedSize = entry.size
        installImage(partitionName, uncompressedSize, inputStream)
    }

    private fun startInstallation() {
        try {
            PrivilegedProvider.getService().setDynProp()
            if (isInUse) {
                onInstallationError(InstallationStep.ERROR_ALREADY_RUNNING_DYN_OS, "")
                return
            }
            if (isInstalled) {
                onInstallationError(InstallationStep.ERROR_REQUIRES_DISCARD_DSU, "")
                return
            }
            forceStopDSU()
            if (!startInstallation(Constants.DEFAULT_SLOT)) {
                onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, Constants.DEFAULT_SLOT)
                return
            }
            installationStarted = true

            installWritablePartition("userdata", userdataSize)
            if (installationJob.isCancelled) return

            when (dsuInstallation.type) {
                Type.SINGLE_SYSTEM_IMAGE -> {
                    installImage(
                        "system",
                        dsuInstallation.fileSize,
                        dsuInstallation.uri,
                    )
                }

                Type.MULTIPLE_IMAGES -> {
                    installImages(dsuInstallation.images)
                }

                Type.DSU_PACKAGE -> {
                    openInputStream(dsuInstallation.uri).use { inputStream ->
                        installStreamingZipUpdate(inputStream)
                    }
                }

                Type.URL -> {
                    URL(dsuInstallation.uri.toString()).openStream().use { inputStream ->
                        installStreamingZipUpdate(inputStream)
                    }
                }

                else -> {}
            }
            if (installationJob.isCancelled) return

            if (!finishInstallation()) {
                onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, Constants.DEFAULT_SLOT)
                return
            }
            installationFinished = true
            Log.d(tag, "Installation finished successfully.")
            onInstallationSuccess()
        } catch (e: Exception) {
            if (!installationJob.isCancelled) {
                installationJob.cancel()
                onInstallationError(
                    InstallationStep.ERROR_CREATE_PARTITION,
                    e.message ?: e.javaClass.simpleName,
                )
            }
        } finally {
            if (installationStarted && !installationFinished) {
                runCatching {
                    if (!abort()) {
                        Log.w(tag, "Failed to abort incomplete DSU installation")
                    }
                }.onFailure {
                    Log.w(tag, "Exception while aborting incomplete DSU installation", it)
                }
            }
        }
    }

    private fun installImages(images: List<ImagePartition>) {
        for (image in images) {
            if (isPartitionSupported(image.partitionName)) {
                installImage(image.partitionName, image.fileSize, image.uri)
            }
            if (installationJob.isCancelled) {
                return
            }
        }
    }

    private fun installImage(partitionName: String, uncompressedSize: Long, uri: Uri) {
        openInputStream(uri).use { inputStream ->
            installImage(
                partitionName,
                uncompressedSize,
                inputStream,
            )
        }
    }

    fun openInputStream(uri: Uri): InputStream {
        return application.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Unable to open installation source: $uri")
    }

    fun createNewPartition(partition: String, partitionSize: Long, readOnly: Boolean): Boolean {
        val result = createPartition(partition, partitionSize, readOnly)
        if (result != IGsiService.INSTALL_OK) {
            Log.d(
                tag,
                "Failed to create $partition partition, error code: $result (check: IGsiService.INSTALL_*)",
            )
            installationJob.cancel()
            onInstallationError(InstallationStep.ERROR_CREATE_PARTITION, partition)
            return false
        }
        return true
    }

    override fun invoke() {
        startInstallation()
    }
}
