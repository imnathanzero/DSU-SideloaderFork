package vegabobo.dsusideloader.ui.cards.warnings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton

@Composable
fun RequiresLogPermissionCard(
    onClickGrant: () -> Unit,
    onClickRefuse: () -> Unit,
) {
    SimpleCard(
        modifier = Modifier.fillMaxWidth(),
        cardIcon = Icons.Outlined.Article,
        cardTitle = stringResource(id = R.string.missing_permission),
        text = stringResource(id = R.string.missing_permission_description),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1F))
            SecondaryButton(text = stringResource(id = R.string.refuse), onClick = onClickRefuse)
            Spacer(modifier = Modifier.width(8.dp))
            PrimaryButton(text = stringResource(id = R.string.grant), onClick = onClickGrant)
        }
    }
}
