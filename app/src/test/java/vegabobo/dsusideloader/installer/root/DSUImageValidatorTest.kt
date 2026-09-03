package vegabobo.dsusideloader.installer.root

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DSUImageValidatorTest {
    @Test
    fun extractsPartitionNameFromImageEntry() {
        assertEquals("system", DSUImageValidator.partitionNameFromImageEntry("system.img"))
        assertEquals("vendor", DSUImageValidator.partitionNameFromImageEntry("vendor.img"))
    }

    @Test
    fun rejectsDirectoriesAndNestedEntries() {
        assertNull(DSUImageValidator.partitionNameFromImageEntry("system/partition.img"))
        assertNull(DSUImageValidator.partitionNameFromImageEntry("../system.img"))
        assertNull(DSUImageValidator.partitionNameFromImageEntry("system\\partition.img"))
    }

    @Test
    fun rejectsUnsupportedAndMalformedNames() {
        assertNull(DSUImageValidator.partitionNameFromImageEntry("boot.img"))
        assertNull(DSUImageValidator.partitionNameFromImageEntry("userdata.img"))
        assertNull(DSUImageValidator.partitionNameFromImageEntry(".img"))
        assertNull(DSUImageValidator.partitionNameFromImageEntry("system.bin"))
    }
}
