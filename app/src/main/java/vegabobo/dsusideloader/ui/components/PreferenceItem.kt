package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun PreferenceItem(
    title: String,
    description: String = "",
    icon: ImageVector? = null,
    onClick: (Boolean) -> Unit = {},
    isChecked: Boolean = false,
    showToggle: Boolean = false,
    isEnabled: Boolean = true,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEnabled,
                onClick = { onClick(isChecked) },
            ),
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        supportingContent = if (description.isNotEmpty()) {
            {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            null
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                )
            }
        },
        trailingContent = if (showToggle) {
            {
                Switch(
                    checked = isChecked,
                    enabled = isEnabled,
                    onCheckedChange = { onClick(isChecked) },
                )
            }
        } else {
            null
        },
    )
}
