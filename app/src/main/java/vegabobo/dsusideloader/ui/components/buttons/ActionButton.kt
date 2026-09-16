package vegabobo.dsusideloader.ui.components.buttons

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    colorButton: Color? = null,
    colorText: Color? = null,
    textButton: Boolean = false,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    if (textButton) {
        TextButton(
            modifier = modifier,
            onClick = onClick,
            enabled = isEnabled,
        ) {
            Text(text = text)
        }
    } else {
        val defaultContainer = MaterialTheme.colorScheme.primary
        val defaultContent = MaterialTheme.colorScheme.onPrimary

        Button(
            modifier = modifier,
            onClick = onClick,
            enabled = isEnabled,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorButton ?: defaultContainer,
                contentColor = colorText ?: defaultContent,
            ),
        ) {
            Text(
                text = text,
                color = colorText ?: defaultContent,
            )
            content()
        }
    }
}
