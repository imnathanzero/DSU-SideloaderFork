package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.ui.theme.CardCornerRadius
import vegabobo.dsusideloader.ui.theme.cardContainer

@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    cardTitle: String = "",
    cardIcon: ImageVector? = null,
    addToggle: Boolean = false,
    isToggleChecked: Boolean = false,
    isToggleEnabled: Boolean = true,
    addPadding: Boolean = true,
    cardColor: Color = MaterialTheme.colorScheme.cardContainer,
    onCheckedChange: ((Boolean) -> Unit) = {},
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(CardCornerRadius),
    content: @Composable (ColumnScope) -> Unit,
) {
    // Surface resolves the matching "on" color for cardColor, so text and icons stay
    // readable even on tinted cards such as errorContainer.
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = roundedCornerShape,
        color = cardColor,
    ) {
        Column(
            modifier = if (addPadding) {
                Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            } else {
                Modifier
            },
        ) {
            if (cardTitle.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (cardIcon != null) {
                        Icon(
                            imageVector = cardIcon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    CardTitle(Modifier.weight(1F), cardTitle = cardTitle)
                    if (addToggle) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isToggleChecked,
                            onCheckedChange = onCheckedChange,
                            enabled = isToggleEnabled,
                        )
                    }
                }
            }
            content(this)
        }
    }
}
