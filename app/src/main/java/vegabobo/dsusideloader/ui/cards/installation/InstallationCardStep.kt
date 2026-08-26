package vegabobo.dsusideloader.ui.cards.installation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.preparation.InstallationStep
import vegabobo.dsusideloader.ui.cards.installation.content.DsuInstalledCardContent
import vegabobo.dsusideloader.ui.cards.installation.content.NotInstallingCardContent
import vegabobo.dsusideloader.ui.cards.installation.content.ProgressableCardContent
import vegabobo.dsusideloader.ui.screen.home.InstallationCardState

@Composable
fun InstallationCardStep(
    uiState: InstallationCardState,
    textFieldInteraction: MutableInteractionSource,
    minPercentageOfFreeStorage: String = "40",
    onClickClear: () -> Unit,
    onClickInstall: () -> Unit,
    onClickRetryInstallation: () -> Unit,
    onClickUnmountSdCardAndRetry: () -> Unit,
    onClickSetSeLinuxPermissive: () -> Unit,
    onClickCancelInstallation: () -> Unit,
    onClickDiscardInstalledGsiAndInstall: () -> Unit,
    onClickDiscardDsu: () -> Unit,
    onClickRebootToDynOS: () -> Unit,
    onClickViewLogs: () -> Unit,
    onClickViewCommands: () -> Unit,
    onClickBackupConfig: () -> Unit,
    onClickRestoreConfig: () -> Unit,
) {
    when (uiState.installationStep) {
        InstallationStep.NOT_INSTALLING ->
            NotInstallingCardContent(textFieldInteraction, uiState, onClickClear, onClickInstall)
        InstallationStep.DSU_ALREADY_INSTALLED ->
            DsuInstalledCardContent(
                textFieldInteraction = textFieldInteraction,
                uiState = uiState,
                onClickClear = onClickClear,
                onClickInstall = onClickInstall,
                onClickRebootToDynOS = onClickRebootToDynOS,
                onClickDiscardDsu = onClickDiscardDsu,
                onClickViewLogs = onClickViewLogs,
                onClickBackupConfig = onClickBackupConfig,
                onClickRestoreConfig = onClickRestoreConfig,
            )
        InstallationStep.DSU_ALREADY_RUNNING_DYN_OS -> ProgressableCardContent(text = stringResource(R.string.already_running_dsu))
        InstallationStep.PROCESSING -> ProgressableCardContent(text = stringResource(R.string.processing), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, isIndeterminate = true)
        InstallationStep.COPYING_FILE -> ProgressableCardContent(text = stringResource(R.string.copying_file), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.DECOMPRESSING_XZ -> ProgressableCardContent(text = stringResource(R.string.decompressing_xz), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.COMPRESSING_TO_GZ -> ProgressableCardContent(text = stringResource(R.string.compressing_to_gz), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.DECOMPRESSING_GZIP -> ProgressableCardContent(text = stringResource(R.string.extracting_file), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.EXTRACTING_FILE -> ProgressableCardContent(text = stringResource(R.string.extracting_file), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.DISCARD_CURRENT_GSI -> ProgressableCardContent(text = stringResource(R.string.discard_dsu_otg), textFirstButton = stringResource(R.string.discard_dsu), onClickFirstButton = onClickDiscardInstalledGsiAndInstall, textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, progress = uiState.installationProgress)
        InstallationStep.WAITING_USER_CONFIRMATION -> ProgressableCardContent(text = stringResource(R.string.installation_prompt), textFirstButton = stringResource(R.string.try_again), onClickFirstButton = onClickRetryInstallation, textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation)
        InstallationStep.PROCESSING_LOG_READABLE -> ProgressableCardContent(text = stringResource(R.string.installing), textFirstButton = stringResource(R.string.cancel), onClickFirstButton = onClickCancelInstallation, textSecondButton = stringResource(R.string.view_logs), onClickSecondButton = onClickViewLogs, showProgressBar = true, isIndeterminate = true)
        InstallationStep.INSTALLING -> ProgressableCardContent(text = stringResource(R.string.installing_partition, uiState.currentPartitionText), textFirstButton = stringResource(R.string.cancel), onClickFirstButton = onClickCancelInstallation, textSecondButton = stringResource(R.string.view_logs), onClickSecondButton = onClickViewLogs, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.INSTALLING_ROOTED -> ProgressableCardContent(text = stringResource(R.string.installing_partition, uiState.currentPartitionText), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.CREATING_PARTITION -> ProgressableCardContent(text = stringResource(R.string.creating_partition, uiState.currentPartitionText), textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation, showProgressBar = true, progress = uiState.installationProgress)
        InstallationStep.ERROR -> ProgressableCardContent(text = stringResource(R.string.unknown_error, uiState.errorText), textFirstButton = stringResource(R.string.view_logs), onClickFirstButton = onClickViewLogs, textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
        InstallationStep.ERROR_CANCELED -> ProgressableCardContent(text = stringResource(R.string.installation_canceled), textFirstButton = stringResource(R.string.view_logs), onClickFirstButton = onClickViewLogs, textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
        InstallationStep.ERROR_REQUIRES_DISCARD_DSU -> ProgressableCardContent(text = stringResource(R.string.discard_dsu_otg), textFirstButton = stringResource(R.string.discard), onClickFirstButton = onClickDiscardInstalledGsiAndInstall, textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation)
        InstallationStep.ERROR_ALREADY_RUNNING_DYN_OS -> ProgressableCardContent(text = stringResource(R.string.already_running_dsu), textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
        InstallationStep.ERROR_CREATE_PARTITION -> ProgressableCardContent(text = stringResource(R.string.failed_create_partition), textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
        InstallationStep.ERROR_EXTERNAL_SDCARD_ALLOC -> ProgressableCardContent(text = stringResource(R.string.allocation_error_description, uiState.errorText), textFirstButton = stringResource(R.string.allocation_error_action), onClickFirstButton = onClickUnmountSdCardAndRetry, textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation)
        InstallationStep.ERROR_NO_AVAIL_STORAGE -> ProgressableCardContent(text = stringResource(R.string.storage_error_description, minPercentageOfFreeStorage), textFirstButton = stringResource(R.string.try_again), onClickFirstButton = onClickRetryInstallation, textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation)
        InstallationStep.ERROR_F2FS_WRONG_PATH -> ProgressableCardContent(text = stringResource(R.string.fs_features_error_description, uiState.errorText), textFirstButton = stringResource(R.string.view_logs), onClickFirstButton = onClickViewLogs, textSecondButton = stringResource(R.string.clear), onClickSecondButton = onClickClear)
        InstallationStep.ERROR_EXTENTS -> ProgressableCardContent(text = stringResource(R.string.extents_error_description), textFirstButton = stringResource(R.string.view_logs), onClickFirstButton = onClickViewLogs, textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
        InstallationStep.ERROR_SELINUX -> ProgressableCardContent(text = stringResource(R.string.selinux_error_description), textFirstButton = stringResource(R.string.selinux_error_action), onClickFirstButton = onClickSetSeLinuxPermissive, textSecondButton = stringResource(R.string.cancel), onClickSecondButton = onClickCancelInstallation)
        InstallationStep.ERROR_SELINUX_ROOTLESS -> ProgressableCardContent(text = stringResource(R.string.selinux_error_description), textFirstButton = stringResource(R.string.view_logs), onClickFirstButton = onClickViewLogs, textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
        InstallationStep.INSTALL_SUCCESS -> ProgressableCardContent(text = stringResource(R.string.installation_finished_rootless), textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
        InstallationStep.INSTALL_SUCCESS_REBOOT_DYN_OS -> ProgressableCardContent(text = stringResource(R.string.installation_finished), textFirstButton = stringResource(R.string.reboot_into_dsu), onClickFirstButton = onClickRebootToDynOS, textSecondButton = stringResource(R.string.discard), onClickSecondButton = onClickDiscardDsu)
        InstallationStep.REQUIRES_ADB_CMD_TO_CONTINUE -> ProgressableCardContent(text = stringResource(R.string.require_adb_cmd_to_continue), textFirstButton = stringResource(R.string.see_commands), onClickFirstButton = onClickViewCommands, textSecondButton = stringResource(R.string.mreturn), onClickSecondButton = onClickClear)
    }
}
