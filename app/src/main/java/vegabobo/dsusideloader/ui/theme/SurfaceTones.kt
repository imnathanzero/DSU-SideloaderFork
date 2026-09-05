package vegabobo.dsusideloader.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * A tonal container ladder for cards and other grouped containers.
 *
 * Material 3 gained dedicated `surfaceContainer*` roles after the version this app builds
 * against, so the ladder has to be derived. [ColorScheme.surface] and
 * [ColorScheme.surfaceVariant] come from two *different* neutral palettes, so blending
 * between them changes chroma as well as lightness and the steps drift in hue. Where the
 * platform can hand over the wallpaper's own palette the ladder is built from that single
 * palette instead, so only lightness changes — which is what Material's real container
 * roles do.
 *
 * Every step then gets a few percent of [ColorScheme.surfaceTint] composited over it, the
 * same trick tonal elevation uses, so containers keep the extracted accent even when the
 * wallpaper's neutrals are nearly grey.
 *
 * Names deliberately differ from Material's `surfaceContainer*`: an extension property is
 * shadowed silently by a real member of the same name, so a future Material 3 upgrade
 * would change these colors without a single compile error.
 */
@Immutable
class SurfaceTones(
    val containerLow: Color,
    val container: Color,
    val containerHigh: Color,
    val containerHighest: Color,
)

// Accent carried by each step. Tonal elevation spends 5%-14% of surfaceTint on its own
// overlays; these stay below that because the neutral steps already separate the levels,
// and the tint is here for hue, not for contrast.
private const val TINT_LOW = 0.03F
private const val TINT = 0.05F
private const val TINT_HIGH = 0.065F
private const val TINT_HIGHEST = 0.08F

private fun Color.tintedWith(accent: Color, alpha: Float): Color =
    accent.copy(alpha = alpha).compositeOver(this)

/**
 * The ladder Material specifies, read off the wallpaper's neutral palette: tones 96/94/92/90
 * in light and 12/17/22/24 in dark. The platform numbers its shades the other way round
 * (0 is white, 1000 is black), and it publishes them in steps of ten tones, so the
 * in-between tones are interpolated from the two shades that bracket them.
 */
@RequiresApi(Build.VERSION_CODES.S)
private fun dynamicSurfaceTones(
    context: Context,
    darkTheme: Boolean,
    accent: Color,
): SurfaceTones {
    fun shade(id: Int) = Color(context.getColor(id))
    return if (darkTheme) {
        val tone10 = shade(android.R.color.system_neutral1_900)
        val tone20 = shade(android.R.color.system_neutral1_800)
        val tone30 = shade(android.R.color.system_neutral1_700)
        SurfaceTones(
            containerLow = lerp(tone10, tone20, 0.2F).tintedWith(accent, TINT_LOW),
            container = lerp(tone10, tone20, 0.7F).tintedWith(accent, TINT),
            containerHigh = lerp(tone20, tone30, 0.2F).tintedWith(accent, TINT_HIGH),
            containerHighest = lerp(tone20, tone30, 0.4F).tintedWith(accent, TINT_HIGHEST),
        )
    } else {
        val tone99 = shade(android.R.color.system_neutral1_10)
        val tone95 = shade(android.R.color.system_neutral1_50)
        val tone90 = shade(android.R.color.system_neutral1_100)
        SurfaceTones(
            containerLow = lerp(tone99, tone95, 0.75F).tintedWith(accent, TINT_LOW),
            container = lerp(tone95, tone90, 0.2F).tintedWith(accent, TINT),
            containerHigh = lerp(tone95, tone90, 0.6F).tintedWith(accent, TINT_HIGH),
            containerHighest = tone90.tintedWith(accent, TINT_HIGHEST),
        )
    }
}

/**
 * Fallback for the hand-written schemes, and for anything below Android 12. Both endpoints
 * of the blend are designed together there, so the chroma drift a dynamic palette suffers
 * from is not visible.
 */
private fun staticSurfaceTones(colorScheme: ColorScheme, darkTheme: Boolean): SurfaceTones {
    fun step(fraction: Float, tint: Float) =
        lerp(colorScheme.surface, colorScheme.surfaceVariant, fraction)
            .tintedWith(colorScheme.surfaceTint, tint)
    return if (darkTheme) {
        SurfaceTones(
            containerLow = step(0.17F, TINT_LOW),
            container = step(0.25F, TINT),
            containerHigh = step(0.46F, TINT_HIGH),
            containerHighest = step(0.67F, TINT_HIGHEST),
        )
    } else {
        SurfaceTones(
            containerLow = step(0.25F, TINT_LOW),
            container = step(0.5F, TINT),
            containerHigh = step(0.75F, TINT_HIGH),
            containerHighest = step(1F, TINT_HIGHEST),
        )
    }
}

internal fun surfaceTonesFor(
    context: Context,
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    dynamicColor: Boolean,
): SurfaceTones =
    if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicSurfaceTones(context, darkTheme, colorScheme.surfaceTint)
    } else {
        staticSurfaceTones(colorScheme, darkTheme)
    }

/** Provided by [DSUHelperTheme]; absent in previews, which fall back to the blended ladder. */
internal val LocalSurfaceTones = staticCompositionLocalOf<SurfaceTones?> { null }

@Composable
@ReadOnlyComposable
private fun ColorScheme.surfaceTones(): SurfaceTones =
    LocalSurfaceTones.current ?: staticSurfaceTones(this, surface.luminance() < 0.5F)

/** One step above the background — grouped rows and other quiet containers. */
val ColorScheme.cardContainerLow: Color
    @Composable
    @ReadOnlyComposable
    get() = surfaceTones().containerLow

/** Default card container: clearly separated from the background, still calm. */
val ColorScheme.cardContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = surfaceTones().container

/** A container nested inside a card, which has to read above [cardContainer]. */
val ColorScheme.cardContainerHigh: Color
    @Composable
    @ReadOnlyComposable
    get() = surfaceTones().containerHigh

/** The highest step, for dense content such as the log viewer. */
val ColorScheme.cardContainerHighest: Color
    @Composable
    @ReadOnlyComposable
    get() = surfaceTones().containerHighest

/**
 * A soft two-hue wash of the extracted accent. Primary and tertiary are rotations of the
 * same wallpaper hue, so a gradient between their containers shows what was extracted
 * without competing with the content on top of it. Both stops are translucent, so the
 * wash tints whatever container it is drawn over rather than replacing it.
 */
val ColorScheme.accentHalo: Brush
    get() = Brush.verticalGradient(
        listOf(
            primaryContainer.copy(alpha = 0.55F),
            tertiaryContainer.copy(alpha = 0.55F),
        ),
    )
