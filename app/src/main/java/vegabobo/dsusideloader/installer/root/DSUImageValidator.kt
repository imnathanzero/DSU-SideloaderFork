package vegabobo.dsusideloader.installer.root

object DSUImageValidator {
    private val UNSUPPORTED_PARTITIONS = setOf(
        "vbmeta",
        "boot",
        "userdata",
        "dtbo",
        "super_empty",
        "system_other",
        "scratch",
    )

    fun isPartitionSupported(partitionName: String): Boolean =
        partitionName.isNotBlank() && !UNSUPPORTED_PARTITIONS.contains(partitionName)

    fun partitionNameFromImageEntry(name: String): String? {
        if (name.isEmpty() || name.contains('/') || name.contains('\\')) return null
        if (!name.endsWith(".img") || name.length <= 4) return null
        val partitionName = name.substringBeforeLast('.')
        return partitionName.takeIf(::isPartitionSupported)
    }
}
