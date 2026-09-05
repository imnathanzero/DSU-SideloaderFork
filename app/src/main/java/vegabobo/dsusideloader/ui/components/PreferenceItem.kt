package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * A single row inside a settings-style card.
 *
 * [onClick] receives the value the preference should take after the interaction:
 * for a row with a toggle that is the negation of [isChecked], for a plain row it
 * is [isChecked] unchanged. Call sites therefore forward the value as-is. Leaving it
 * null marks the row as informational: it then takes no interaction modifier at all,
 * so it neither ripples nor is announced as a target by a screen reader.
 *
 * When [showToggle] is set the whole row acts as the switch — it carries the
 * toggleable semantics and the [Switch] itself is decorative, so screen readers
 * announce one target with its on/off state instead of two competing ones.
 */
@Composable
fun PreferenceItem(
    title: String,
    description: String = "",
    icon: ImageVector? = null,
    onClick: ((Boolean) -> Unit)? = null,
    isChecked: Boolean = false,
    showToggle: Boolean = false,
    isEnabled: Boolean = true,
) {
    val contentAlpha = if (isEnabled) 1F else 0.38F
    val interactionModifier = when {
        onClick == null -> Modifier
        showToggle -> Modifier.toggleable(
            value = isChecked,
            enabled = isEnabled,
            role = Role.Switch,
            onValueChange = { onClick(it) },
        )
        else -> Modifier.clickable(enabled = isEnabled, onClick = { onClick(isChecked) })
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(interactionModifier)
            .padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 14.dp,
                top = 14.dp,
            ),
    ) {
        if (icon != null) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .size(40.dp)
                    .alpha(contentAlpha),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(9.dp),
                )
            }
            Spacer(modifier = Modifier.width(18.dp))
        }
        Column(
            modifier = Modifier
                .weight(1F)
                .align(Alignment.CenterVertically),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
            )
            if (description.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                )
            }
        }
        if (showToggle) {
            Switch(
                checked = isChecked,
                enabled = isEnabled,
                onCheckedChange = null,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}
