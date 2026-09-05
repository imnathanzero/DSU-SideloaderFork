package vegabobo.dsusideloader.core

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.util.DataStoreUtils

open class BaseViewModel(
    open val dataStore: DataStore<Preferences>,
) : ViewModel() {

    suspend fun readStringPref(
        key: String,
    ): String {
        return DataStoreUtils.readStringPref(dataStore, key, "")
    }

    /**
     * Reads a boolean preference, falling back to the preference's declared
     * default (see [AppPrefs.boolDefault]) when it has never been written.
     */
    suspend fun readBoolPref(
        key: String,
    ): Boolean {
        return DataStoreUtils.readBoolPref(dataStore, key, AppPrefs.boolDefault(key))
    }

    suspend fun updateBoolPref(
        key: String,
        value: Boolean,
        onRead: (Boolean) -> Unit = {},
    ) {
        DataStoreUtils.updateBoolPref(dataStore, key, value) {
            onRead(value)
        }
    }

    suspend fun updateStringPref(
        key: String,
        value: String,
        onRead: (String) -> Unit = {},
    ) {
        DataStoreUtils.updateStringPref(dataStore, key, value) {
            onRead(value)
        }
    }
}
