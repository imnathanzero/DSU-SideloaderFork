package vegabobo.dsusideloader.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Slightly rounder than the Material 3 defaults, matching the app's existing 10.dp cards
// while giving containers a clear size hierarchy.
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Corner radius shared by cards and grouped list containers. */
val CardCornerRadius = 16.dp

/** Corner radius used where grouped items meet, keeping the seam visible but soft. */
val GroupedItemCornerRadius = 6.dp
