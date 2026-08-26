package vegabobo.dsusideloader.ui.screen.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.system.exitProcess
import kotlinx.coroutines.flow.collectLatest
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.cards.DsuInfoCard
import vegabobo.dsusideloader.ui.cards.ImageSizeCard
import vegabobo.dsusideloader.ui.cards.UserdataCard
import vegabobo.dsusideloader.ui.cards.installation.InstallationCard
import vegabobo.dsusideloader.ui.cards.warnings.GrantingPermissionCard
import vegabobo.dsusideloader.ui.cards.warnings.RequiresLogPermissionCard
import vegabobo.dsusideloader.ui.cards.warnings.SetupStorage
import vegabobo.dsusideloader.ui.cards.warnings.StorageWarningCard
import vegabobo.dsusideloader.ui.cards.warnings.UnlockedBootloaderCard
import vegabobo.dsusideloader.ui.cards.warnings.UnsupportedCard
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.ui.sdialogs.CancelSheet
import vegabobo.dsusideloader.ui.sdialogs.ConfirmInstallationSheet
import vegabobo.dsusideloader.ui.sdialogs.DiscardDSUSheet
import vegabobo.dsusideloader.ui.sdialogs.ImageSizeWarningSheet
import vegabobo.dsusideloader.ui.sdialogs.ViewLogsBottomSheet
import vegabobo.dsusideloader.ui.util.KeepScreenOn
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

object HomeLinks {
    const val DSU_LEARN_MORE = "https://developer.android.com/topic/dsu"
    const val DSU_DOCS = "https://source.android.com/devices/tech/ota/dynamic-system-updates"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    navigate: (String) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val createConfigLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> uri?.let(homeViewModel::saveDsuConfig) }
    val restoreConfigLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let(homeViewModel::restoreDsuConfig) }

    if (uiState.shouldKeepScreenOn) KeepScreenOn()

    LaunchedEffect(Unit) {
        homeViewModel.setupUserPreferences()
        homeViewModel.session.operationMode.collectLatest { homeViewModel.initialChecks() }
    }

    LaunchedEffect(uiState.isDsuInstalled) {
        if (uiState.isDsuInstalled) {
            val size = homeViewModel.detectInstalledUserdataSize()
            if (size != null) homeViewModel.updateUserdataSize((size / 1024L / 1024L / 1024L).toString())
        }
    }

    // Built-in installation does not use the normal diagnostic pipeline, so capture its logcat separately.
    LaunchedEffect(uiState.isInstalling(), homeViewModel.session.preferences.useBuiltinInstaller) {
        if (uiState.isInstalling() && homeViewModel.session.isRoot() && homeViewModel.session.preferences.useBuiltinInstaller) {
            if (homeViewModel.logger == null) {
                homeViewModel.logger = vegabobo.dsusideloader.installer.privileged.LogcatDiagnostic(
                    onInstallationError = { _, _ -> },
                    onStepUpdate = { },
                    onInstallationProgressUpdate = { _, _ -> },
                    onInstallationSuccess = { },
                    onLogLineReceived = { },
                )
            }
            homeViewModel.logger!!.startCaptureOnly("Built-in DSU installer")
        } else if (!uiState.isInstalling() && homeViewModel.logger?.isLogging?.get() == true) {
            homeViewModel.logger!!.destroy()
        }
    }

    ApplicationScreen(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.app_name),
                icon = Icons.Outlined.Settings,
                scrollBehavior = it,
                onClickIcon = { navigate(Destinations.Preferences) },
            )
        },
        content = {
            Box(modifier = Modifier.animateContentSize()) {
                when (uiState.additionalCard) {
                    AdditionalCardState.NO_DYNAMIC_PARTITIONS -> UnsupportedCard(onClickClose = { exitProcess(0) }, onClickContinueAnyway = { homeViewModel.overrideDynamicPartitionCheck() })
                    AdditionalCardState.SETUP_STORAGE -> SetupStorage { homeViewModel.takeUriPermission(it) }
                    AdditionalCardState.UNAVAIABLE_STORAGE -> StorageWarningCard(minPercentageFreeStorage = homeViewModel.allocPercentageInt.toString(), onClick = { homeViewModel.overrideUnavaiableStorage() })
                    AdditionalCardState.MISSING_READ_LOGS_PERMISSION -> RequiresLogPermissionCard(onClickGrant = { homeViewModel.grantReadLogs() }, onClickRefuse = { homeViewModel.refuseReadLogs() })
                    AdditionalCardState.GRANTING_READ_LOGS_PERMISSION -> GrantingPermissionCard()
                    AdditionalCardState.BOOTLOADER_UNLOCKED_WARNING -> UnlockedBootloaderCard { homeViewModel.onClickBootloaderUnlockedWarning() }
                    AdditionalCardState.NONE -> {}
                }
            }
            if (uiState.passedInitialChecks && uiState.additionalCard == AdditionalCardState.NONE) {
                InstallationCard(
                    uiState = uiState.installationCard,
                    onClickInstall = { homeViewModel.onClickInstall() },
                    onClickUnmountSdCardAndRetry = { homeViewModel.onClickUnmountSdCardAndRetry() },
                    onClickSetSeLinuxPermissive = { homeViewModel.onClickSetSeLinuxPermissive() },
                    onClickRetryInstallation = { homeViewModel.onClickRetryInstallation() },
                    onClickClear = { homeViewModel.resetInstallationCard() },
                    onSelectFileSuccess = { homeViewModel.onFileSelectionResult(it) },
                    onClickCancelInstallation = { homeViewModel.onClickCancel() },
                    onClickDiscardInstalledGsiAndInstall = { homeViewModel.onClickDiscardGsiAndStartInstallation() },
                    onClickDiscardDsu = { homeViewModel.showDiscardSheet() },
                    onClickRebootToDynOS = { homeViewModel.onClickRebootToDynOS() },
                    onClickViewLogs = { homeViewModel.showLogsWarning() },
                    onClickViewCommands = { navigate(Destinations.ADBInstallation) },
                    onClickBackupConfig = { createConfigLauncher.launch("dsu-config.json") },
                    onClickRestoreConfig = { restoreConfigLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    minPercentageOfFreeStorage = homeViewModel.allocPercentageInt.toString(),
                )
                UserdataCard(
                    isEnabled = uiState.isInstalling(),
                    uiState = uiState.userDataCard,
                    isDsuInstalled = uiState.isDsuInstalled,
                    onCheckedChange = { homeViewModel.onCheckUserdataCard() },
                    onValueChange = { homeViewModel.updateUserdataSize(it) },
                    onPreserveCheckedChange = { homeViewModel.onCheckPreserveUserdata(it) },
                )
                ImageSizeCard(
                    isEnabled = uiState.isInstalling(),
                    uiState = uiState.imageSizeCard,
                    onCheckedChange = { homeViewModel.onCheckImageSizeCard() },
                    onValueChange = { homeViewModel.updateImageSize(it) },
                )
                DsuInfoCard(
                    onClickViewDocs = { uriHandler.openUri(HomeLinks.DSU_DOCS) },
                    onClickLearnMore = { uriHandler.openUri(HomeLinks.DSU_LEARN_MORE) },
                )
            }
        },
    )

    when (uiState.sheetDisplay) {
        SheetDisplayState.CONFIRM_INSTALLATION -> ConfirmInstallationSheet(filename = homeViewModel.obtainSelectedFilename(), userdata = homeViewModel.session.userSelection.getUserDataSizeAsGB(), fileSize = homeViewModel.session.userSelection.userSelectedImageSize, onClickConfirm = { homeViewModel.onConfirmInstallationSheet() }, onClickCancel = { homeViewModel.dismissSheet() })
        SheetDisplayState.CANCEL_INSTALLATION -> CancelSheet(onClickConfirm = { homeViewModel.onClickCancelInstallationButton() }, onClickCancel = { homeViewModel.dismissSheet() })
        SheetDisplayState.IMAGESIZE_WARNING -> ImageSizeWarningSheet(onClickConfirm = { homeViewModel.dismissSheet() }, onClickCancel = { homeViewModel.onCheckImageSizeCard() })
        SheetDisplayState.DISCARD_DSU -> DiscardDSUSheet(onClickConfirm = { homeViewModel.onClickDiscardGsi() }, onClickCancel = { homeViewModel.dismissSheet() })
        SheetDisplayState.VIEW_LOGS -> ViewLogsBottomSheet(logs = uiState.installationLogs, onClickSaveLogs = { homeViewModel.saveLogs(it) }, onDismiss = { homeViewModel.dismissSheet() })
        SheetDisplayState.NONE -> {}
    }
}
