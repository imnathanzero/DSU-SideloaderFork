package vegabobo.dsusideloader.ui.cards.warnings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton

@Composable
fun StorageWarningCard(
    minPercentageFreeStorage: String,
    onClick: () -> Unit,
) {
    SimpleCard(
        modifier = Modifier.fillMaxWidth(),
        cardIcon = Icons.Outlined.Storage,
        cardTitle = stringResource(id = R.string.storage_warning),
        text = stringResource(id = R.string.storage_warning_description, minPercentageFreeStorage),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1F))
            PrimaryButton(text = stringResource(id = R.string.continue_anyway), onClick = onClick)
        }
    }
}
