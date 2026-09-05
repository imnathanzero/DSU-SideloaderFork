package vegabobo.dsusideloader.ui.cards.installation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.FileSelectionBox
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton
import vegabobo.dsusideloader.ui.screen.home.InstallationCardState

@Composable
fun NotInstallingCardContent(
    textFieldInteraction: MutableInteractionSource,
    uiState: InstallationCardState,
    onClickClear: () -> Unit,
    onClickInstall: () -> Unit,
) {
    FileSelectionBox(
        textFieldInteraction = textFieldInteraction,
        isEnabled = uiState.isTextFieldEnabled,
        isError = uiState.isError,
        isReadOnly = true,
        textFieldValue = uiState.text,
        textFieldTitle = stringResource(id = R.string.select_gsi_info),
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        AnimatedVisibility(visible = uiState.isError) {
            Text(
                text = stringResource(id = R.string.file_unsupported),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(modifier = Modifier.weight(1F))
        if (uiState.isInstallable) {
            SecondaryButton(
                text = stringResource(R.string.clear),
                onClick = onClickClear,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        PrimaryButton(
            text = stringResource(R.string.install),
            onClick = onClickInstall,
            isEnabled = uiState.isInstallable,
        )
    }
}
