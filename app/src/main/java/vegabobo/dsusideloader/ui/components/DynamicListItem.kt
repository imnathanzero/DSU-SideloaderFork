package vegabobo.dsusideloader.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import vegabobo.dsusideloader.ui.theme.CardCornerRadius
import vegabobo.dsusideloader.ui.theme.GroupedItemCornerRadius

@Composable
fun DynamicListItem(
    listLength: Int,
    currentValue: Int,
    content: @Composable () -> Unit,
) {
    val shape = when {
        listLength == 0 -> RoundedCornerShape(CardCornerRadius)
        currentValue == 0 -> RoundedCornerShape(
            topStart = CardCornerRadius,
            topEnd = CardCornerRadius,
            bottomStart = GroupedItemCornerRadius,
            bottomEnd = GroupedItemCornerRadius,
        )
        currentValue == listLength -> RoundedCornerShape(
            topStart = GroupedItemCornerRadius,
            topEnd = GroupedItemCornerRadius,
            bottomStart = CardCornerRadius,
            bottomEnd = CardCornerRadius,
        )
        else -> RoundedCornerShape(GroupedItemCornerRadius)
    }
    CardBox(
        addPadding = false,
        roundedCornerShape = shape,
    ) {
        content()
    }
}
