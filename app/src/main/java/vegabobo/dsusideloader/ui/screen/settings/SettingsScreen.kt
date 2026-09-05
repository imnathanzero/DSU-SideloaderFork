package vegabobo.dsusideloader.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.ScreenLockPortrait
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preferences.AppPrefs
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.DialogLikeBottomSheet
import vegabobo.dsusideloader.ui.components.PreferenceItem
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.Title
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.ui.theme.GroupedItemSpacing
import vegabobo.dsusideloader.ui.theme.ScreenHorizontalPadding
import vegabobo.dsusideloader.util.OperationMode
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    navigate: (String) -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        settingsViewModel.checkDevOpt()
    }

    ApplicationScreen(
        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(GroupedItemSpacing),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.settings),
                scrollBehavior = it,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        Title(title = stringResource(id = R.string.installation))
        SimpleCard(addPadding = false) {
            PreferenceItem(
                title = stringResource(id = R.string.builtin_installer),
                description =
                if (settingsViewModel.isAndroidQ()) {
                    stringResource(id = R.string.unsupported)
                } else if (uiState.isRoot) {
                    stringResource(id = R.string.builtin_installer_description)
                } else {
                    stringResource(R.string.requires_root)
                },
                icon = Icons.Outlined.Bolt,
                showToggle = true,
                isEnabled = uiState.isRoot && !settingsViewModel.isAndroidQ(),
                isChecked = uiState.preferences[AppPrefs.USE_BUILTIN_INSTALLER] ?: false,
                onClick = {
                    if (it) {
                        settingsViewModel.updateSheetDisplay(DialogSheetState.BUILT_IN_INSTALLER)
                    }
                    settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, it)
                },
            )
            PreferenceItem(
                title = stringResource(id = R.string.preserve_userdata),
                description = stringResource(id = R.string.preserve_userdata_desc),
                icon = Icons.Outlined.Save,
                showToggle = true,
                isChecked = uiState.preferences[AppPrefs.KEEP_USERDATA] ?: true,
                onClick = { settingsViewModel.togglePreference(AppPrefs.KEEP_USERDATA, it) },
            )
            PreferenceItem(
                title = stringResource(id = R.string.unmount_sd_title),
                description = stringResource(id = R.string.unmount_sd_description),
                icon = Icons.Outlined.SdCard,
                showToggle = true,
                isChecked = uiState.preferences[AppPrefs.UMOUNT_SD] ?: false,
                onClick = { settingsViewModel.togglePreference(AppPrefs.UMOUNT_SD, it) },
            )
            PreferenceItem(
                title = stringResource(id = R.string.keep_screen_on),
                icon = Icons.Outlined.ScreenLockPortrait,
                showToggle = true,
                isChecked = uiState.preferences[AppPrefs.KEEP_SCREEN_ON] ?: false,
                onClick = { settingsViewModel.togglePreference(AppPrefs.KEEP_SCREEN_ON, it) },
            )
        }

        if (uiState.isDevOptEnabled) {
            Title(
                title = stringResource(id = R.string.developer_options),
            )
            SimpleCard(addPadding = false) {
                PreferenceItem(
                    title = stringResource(id = R.string.storage_check_title),
                    description = stringResource(id = R.string.storage_check_description),
                    icon = Icons.Outlined.Storage,
                    showToggle = true,
                    isChecked = uiState.preferences[AppPrefs.DISABLE_STORAGE_CHECK] ?: false,
                    onClick = {
                        if (it) {
                            settingsViewModel.updateSheetDisplay(DialogSheetState.DISABLE_STORAGE_CHECK)
                        }
                        settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, it)
                    },
                )
                if (settingsViewModel.getOperationMode() != OperationMode.ADB) {
                    PreferenceItem(
                        title = stringResource(id = R.string.full_logcat_logging_title),
                        description = stringResource(id = R.string.full_logcat_logging_description),
                        icon = Icons.Outlined.BugReport,
                        showToggle = true,
                        isChecked = uiState.preferences[AppPrefs.FULL_LOGCAT_LOGGING] ?: false,
                        onClick = { settingsViewModel.togglePreference(AppPrefs.FULL_LOGCAT_LOGGING, it) },
                    )
                }
            }
        }

        Title(title = stringResource(id = R.string.other))
        SimpleCard(addPadding = false) {
            PreferenceItem(
                title = stringResource(id = R.string.operation_mode),
                description = settingsViewModel.checkOperationMode(),
                icon = Icons.Outlined.Terminal,
            )
            PreferenceItem(
                title = stringResource(id = R.string.about),
                description = stringResource(id = R.string.about_description),
                icon = Icons.Outlined.Info,
                onClick = { navigate(Destinations.About) },
            )
        }
    }

    when (uiState.dialogSheetDisplay) {
        DialogSheetState.BUILT_IN_INSTALLER ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.experimental_feature),
                icon = Icons.Outlined.NewReleases,
                text = stringResource(id = R.string.experimental_feature_description),
                confirmText = stringResource(id = R.string.yes),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.USE_BUILTIN_INSTALLER, false)
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                },
                onClickConfirm = { settingsViewModel.updateSheetDisplay(DialogSheetState.NONE) },
            )

        DialogSheetState.DISABLE_STORAGE_CHECK ->
            DialogLikeBottomSheet(
                title = stringResource(id = R.string.warning_storage_check_title),
                icon = Icons.Outlined.WarningAmber,
                text = stringResource(id = R.string.warning_storage_check_description),
                confirmText = stringResource(id = R.string.continue_anyway),
                cancelText = stringResource(id = R.string.cancel),
                onClickCancel = {
                    settingsViewModel.togglePreference(AppPrefs.DISABLE_STORAGE_CHECK, false)
                    settingsViewModel.updateSheetDisplay(DialogSheetState.NONE)
                },
                onClickConfirm = { settingsViewModel.updateSheetDisplay(DialogSheetState.NONE) },
            )

        else -> {}
    }
}
