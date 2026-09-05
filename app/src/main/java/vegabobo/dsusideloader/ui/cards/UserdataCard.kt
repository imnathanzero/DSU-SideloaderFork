package vegabobo.dsusideloader.ui.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.components.FileSelectionBox
import vegabobo.dsusideloader.ui.screen.home.UserDataCardState

@Composable
fun UserdataCard(
    isEnabled: Boolean,
    uiState: UserDataCardState,
    isDsuInstalled: Boolean,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit = {},
    onPreserveCheckedChange: (Boolean) -> Unit = {},
) {
    if (isDsuInstalled) {
        return
    }

    CardBox(
        modifier = modifier,
        cardTitle = stringResource(id = R.string.userdata_size),
        cardIcon = Icons.Outlined.DataUsage,
        addToggle = true,
        isToggleEnabled = !isEnabled,
        isToggleChecked = uiState.isSelected,
        onCheckedChange = onCheckedChange,
    ) {
        AnimatedVisibility(
            visible = uiState.isSelected,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                FileSelectionBox(
                    modifier = Modifier.padding(bottom = 4.dp),
                    isEnabled = !isEnabled,
                    isError = uiState.isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textFieldValue = uiState.text,
                    textFieldTitle = stringResource(id = R.string.userdata_size_info),
                    onValueChange = onValueChange,
                )
                AnimatedVisibility(visible = uiState.isError) {
                    Text(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp),
                        text = stringResource(
                            id = R.string.allowed_userdata_allocation,
                            uiState.maximumAllowed,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
