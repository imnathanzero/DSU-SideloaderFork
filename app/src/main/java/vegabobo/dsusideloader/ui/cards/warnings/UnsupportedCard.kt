package vegabobo.dsusideloader.ui.cards.warnings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.buttons.ErrorButton
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton

@Composable
fun UnsupportedCard(
    onClickClose: () -> Unit = {},
    onClickContinueAnyway: () -> Unit,
) {
    SimpleCard(
        modifier = Modifier.fillMaxWidth(),
        cardColor = MaterialTheme.colorScheme.errorContainer,
        cardIcon = Icons.Outlined.ErrorOutline,
        cardTitle = stringResource(id = R.string.unsupported),
        text = stringResource(id = R.string.device_unsupported_description),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1F))
            SecondaryButton(
                text = stringResource(id = R.string.continue_anyway),
                onClick = onClickContinueAnyway,
            )
            Spacer(modifier = Modifier.width(8.dp))
            ErrorButton(
                onClick = onClickClose,
                text = stringResource(id = R.string.close),
            )
        }
    }
}
