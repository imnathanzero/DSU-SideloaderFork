package vegabobo.dsusideloader.model

import org.json.JSONObject

/** Configuration that can be backed up and restored independently of the GSI image. */
data class DsuConfig(
    val userdataSize: Long,
    val imageSize: Long,
    val preserveUserdata: Boolean,
    val useBuiltinInstaller: Boolean,
    val selectedFileUri: String,
    val selectedFileName: String,
) {
    fun toJson(): String = JSONObject().apply {
        put("version", 1)
        put("userdataSize", userdataSize)
        put("imageSize", imageSize)
        put("preserveUserdata", preserveUserdata)
        put("useBuiltinInstaller", useBuiltinInstaller)
        put("selectedFileUri", selectedFileUri)
        put("selectedFileName", selectedFileName)
    }.toString(2)

    companion object {
        fun fromJson(value: String): DsuConfig {
            val json = JSONObject(value)
            require(json.optInt("version", 1) == 1) { "Unsupported DSU config version" }
            return DsuConfig(
                userdataSize = json.getLong("userdataSize"),
                imageSize = json.getLong("imageSize"),
                preserveUserdata = json.optBoolean("preserveUserdata", true),
                useBuiltinInstaller = json.optBoolean("useBuiltinInstaller", false),
                selectedFileUri = json.optString("selectedFileUri", ""),
                selectedFileName = json.optString("selectedFileName", ""),
            )
        }
    }
}
