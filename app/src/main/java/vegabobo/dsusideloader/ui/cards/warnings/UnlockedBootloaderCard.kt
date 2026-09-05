package vegabobo.dsusideloader.ui.cards.warnings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton

@Composable
fun UnlockedBootloaderCard(
    onClickClose: () -> Unit = {},
) {
    SimpleCard(
        modifier = Modifier.fillMaxWidth(),
        cardIcon = Icons.Outlined.LockOpen,
        cardTitle = stringResource(id = R.string.unlocked_bl_warn),
        text = stringResource(id = R.string.unlocked_bl_warn_desc),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1F))
            SecondaryButton(
                text = stringResource(id = R.string.proceed),
                onClick = onClickClose,
            )
        }
    }
}
