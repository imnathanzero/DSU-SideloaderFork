package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun DynamicListItem(
    listLength: Int,
    currentValue: Int,
    content: @Composable () -> Unit,
) {
    val shape = when {
        listLength <= 0 -> RoundedCornerShape(16.dp)
        currentValue == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        currentValue == listLength -> RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }
    CardBox(
        addPadding = false,
        roundedCornerShape = shape,
    ) {
        content()
    }
}
