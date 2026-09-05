package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.ui.theme.cardContainer

@Composable
fun SimpleCard(
    modifier: Modifier = Modifier,
    cardTitle: String = "",
    cardIcon: ImageVector? = null,
    text: String = "",
    addToggle: Boolean = false,
    isToggleEnabled: Boolean = false,
    cardColor: Color = MaterialTheme.colorScheme.cardContainer,
    justifyText: Boolean = false,
    addPadding: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    CardBox(
        modifier = modifier,
        cardTitle = cardTitle,
        cardIcon = cardIcon,
        addToggle = addToggle,
        isToggleChecked = isToggleEnabled,
        addPadding = addPadding,
        cardColor = cardColor,
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = if (justifyText) TextAlign.Justify else TextAlign.Start,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        content()
    }
}
