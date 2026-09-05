package vegabobo.dsusideloader.ui.screen.settings

import vegabobo.dsusideloader.preferences.AppPrefs

enum class DialogSheetState {
    NONE,
    BUILT_IN_INSTALLER,
    DISABLE_STORAGE_CHECK,
}

data class SettingsUiState(
    val preferences: HashMap<String, Boolean> = AppPrefs.settingsDefaults(),
    val dialogSheetDisplay: DialogSheetState = DialogSheetState.NONE,
    val isRoot: Boolean = false,
    val isDevOptEnabled: Boolean = false,
)
