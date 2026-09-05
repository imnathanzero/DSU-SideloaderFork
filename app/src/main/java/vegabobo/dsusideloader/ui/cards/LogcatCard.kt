package vegabobo.dsusideloader.ui.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vegabobo.dsusideloader.ui.theme.Shapes
import vegabobo.dsusideloader.ui.theme.cardContainerHighest

@Composable
fun LogcatCard(
    logs: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.cardContainerHighest,
        shape = Shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(14.dp)
                .fillMaxSize(),
        ) {
            Text(
                text = logs,
                // Monospace keeps installer output aligned, which matters for logs.
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
