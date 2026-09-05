package vegabobo.dsusideloader.ui.components.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    colorButton: Color? = null,
    colorText: Color? = null,
    textButton: Boolean = false,
    tonal: Boolean = false,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    val label: @Composable () -> Unit = {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = colorText ?: Color.Unspecified,
        )
        content()
    }

    when {
        textButton ->
            TextButton(modifier = modifier, onClick = onClick, enabled = isEnabled) { label() }

        // Tonal buttons let Material derive the container/content pair, which keeps the
        // secondary action legible on every background instead of faking transparency.
        colorButton == null && tonal ->
            FilledTonalButton(modifier = modifier, onClick = onClick, enabled = isEnabled) { label() }

        else ->
            Button(
                modifier = modifier,
                onClick = onClick,
                enabled = isEnabled,
                colors = if (colorButton != null) {
                    ButtonDefaults.buttonColors(
                        containerColor = colorButton,
                        contentColor = colorText ?: contentColorFor(colorButton),
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) { label() }
    }
}
