package vegabobo.dsusideloader.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun CardTitle(modifier: Modifier = Modifier, cardTitle: String) {
    Text(
        modifier = modifier,
        text = cardTitle,
        // A single line that is allowed to scroll horizontally gives no hint that
        // anything was cut off, and swiping a title is not a discoverable gesture.
        // Two lines fit every translation of these titles; longer ones ellipsize.
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleLarge,
    )
}
