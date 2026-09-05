package vegabobo.dsusideloader.ui.cards.installation.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton

@Composable
fun ProgressableCardContent(
    text: String,
    showProgressBar: Boolean = false,
    isIndeterminate: Boolean = false,
    progress: Float = 0F,
    textFirstButton: String = "",
    textSecondButton: String = "",
    onClickFirstButton: (() -> Unit)? = null,
    onClickSecondButton: (() -> Unit)? = null,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    AnimatedVisibility(visible = showProgressBar) {
        val progressBarModifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 6.dp)
            .clip(RoundedCornerShape(percent = 50))
        if (isIndeterminate) {
            LinearProgressIndicator(modifier = progressBarModifier)
        } else {
            // The installer only publishes progress every 128 MiB, so the raw value
            // arrives in large jumps; animating between them reads as a partition
            // being written rather than as a bar teleporting. ProgressAnimationSpec is
            // the spring Material uses for exactly this.
            val animatedProgress by animateFloatAsState(
                targetValue = progress.coerceIn(0F, 1F),
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            )
            LinearProgressIndicator(
                modifier = progressBarModifier,
                progress = animatedProgress,
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.weight(1F))
        if (onClickSecondButton != null) {
            SecondaryButton(
                text = textSecondButton,
                onClick = onClickSecondButton,
            )
        }
        if (onClickFirstButton != null && onClickSecondButton != null) {
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (onClickFirstButton != null) {
            PrimaryButton(
                text = textFirstButton,
                onClick = onClickFirstButton,
            )
        }
    }
}
