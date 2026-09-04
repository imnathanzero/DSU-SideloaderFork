package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CardBox(
    modifier: Modifier = Modifier,
    cardTitle: String = "",
    addToggle: Boolean = false,
    isToggleChecked: Boolean = false,
    isToggleEnabled: Boolean = true,
    addPadding: Boolean = true,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    onCheckedChange: ((Boolean) -> Unit) = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .then(if (addPadding) Modifier.padding(16.dp) else Modifier),
        ) {
            if (cardTitle.isNotEmpty()) {
                if (addToggle) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CardTitle(
                            modifier = Modifier.weight(1F),
                            cardTitle = cardTitle,
                        )
                        Spacer(modifier = Modifier.padding(4.dp))
                        Switch(
                            checked = isToggleChecked,
                            onCheckedChange = onCheckedChange,
                            enabled = isToggleEnabled,
                        )
                    }
                } else {
                    CardTitle(
                        cardTitle = cardTitle,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
            }
            content()
        }
    }
}
