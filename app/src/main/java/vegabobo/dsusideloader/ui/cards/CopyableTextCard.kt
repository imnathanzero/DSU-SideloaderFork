package vegabobo.dsusideloader.ui.cards

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.CardBox
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.theme.Shapes
import vegabobo.dsusideloader.ui.theme.cardContainerHigh

@Composable
fun CopyableTextCard(
    text: String,
    showToast: Boolean = true,
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val copiedText = stringResource(id = R.string.copied)

    CardBox {
        // These cards hold shell commands, so they get the same monospace treatment as the
        // log viewer: in the proportional body font a quoted path is hard to read back and
        // easy to mistype, and the command did not look like a command at all.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.small,
            color = MaterialTheme.colorScheme.cardContainerHigh,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                text = text,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1F))
            PrimaryButton(
                text = stringResource(id = R.string.copy_text),
                onClick = {
                    clipboardManager.setText(AnnotatedString(text))
                    if (showToast) {
                        Toast.makeText(context, copiedText, Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }
}
