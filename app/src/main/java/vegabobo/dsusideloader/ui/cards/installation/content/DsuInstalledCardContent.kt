package vegabobo.dsusideloader.ui.cards.installation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.FileSelectionBox
import vegabobo.dsusideloader.ui.components.buttons.ErrorButton
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton
import vegabobo.dsusideloader.ui.screen.home.InstallationCardState

@Composable
fun DsuInstalledCardContent(
    textFieldInteraction: MutableInteractionSource,
    uiState: InstallationCardState,
    onClickClear: () -> Unit,
    onClickInstall: () -> Unit,
    onClickRebootToDynOS: () -> Unit,
    onClickDiscardDsu: () -> Unit,
    onClickViewLogs: () -> Unit,
    onClickBackupConfig: () -> Unit,
    onClickRestoreConfig: () -> Unit,
) {
    Text(
        text = stringResource(R.string.dsu_already_installed),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )

    FileSelectionBox(
        textFieldInteraction = textFieldInteraction,
        isEnabled = uiState.isTextFieldEnabled,
        isError = uiState.isError,
        isReadOnly = true,
        textFieldValue = uiState.text,
        textFieldTitle = stringResource(id = R.string.select_gsi_info),
    )
    Spacer(modifier = Modifier.padding(top = 10.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        AnimatedVisibility(visible = uiState.isError) {
            Text(
                text = stringResource(id = R.string.file_unsupported),
                modifier = Modifier.padding(start = 2.dp),
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(modifier = Modifier.weight(1F))
        if (uiState.isInstallable) {
            SecondaryButton(text = stringResource(R.string.clear), onClick = onClickClear)
            Spacer(modifier = Modifier.padding(end = 6.dp))
        }
        PrimaryButton(
            text = stringResource(R.string.update),
            onClick = onClickInstall,
            isEnabled = uiState.isInstallable,
        )
    }

    Spacer(modifier = Modifier.padding(top = 8.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.weight(1F))
        SecondaryButton(text = stringResource(R.string.view_logs), onClick = onClickViewLogs)
        Spacer(modifier = Modifier.padding(end = 6.dp))
        SecondaryButton(text = stringResource(R.string.backup_config), onClick = onClickBackupConfig)
        Spacer(modifier = Modifier.padding(end = 6.dp))
        SecondaryButton(text = stringResource(R.string.restore_config), onClick = onClickRestoreConfig)
    }

    Spacer(modifier = Modifier.padding(top = 8.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.weight(1F))
        SecondaryButton(
            text = stringResource(id = R.string.reboot_into_dsu),
            onClick = onClickRebootToDynOS,
        )
        Spacer(modifier = Modifier.padding(end = 6.dp))
        ErrorButton(
            text = stringResource(id = R.string.discard_dsu),
            onClick = onClickDiscardDsu,
        )
    }
}
