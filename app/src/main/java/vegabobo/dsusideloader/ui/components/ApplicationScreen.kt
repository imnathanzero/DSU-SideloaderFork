package vegabobo.dsusideloader.ui.components

import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationScreen(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(0.dp),
    columnContent: Boolean = true,
    enableDefaultScrollBehavior: Boolean = true,
    topBar: @Composable (TopAppBarScrollBehavior) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        flingAnimationSpec = decayAnimationSpec,
        state = rememberTopAppBarState(),
    )

    val scrollBehaviorModifier =
        if (enableDefaultScrollBehavior) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else Modifier

    Surface {
        Scaffold(
            modifier = scrollBehaviorModifier
                .fillMaxSize()
                // Vertical insets are handled per-slot below, but nothing used to keep
                // content clear of a side navigation bar or a display cutout, so in
                // landscape the top bar title and the cards ran underneath both. The
                // padding sits on the Scaffold rather than on the Surface so the
                // background still paints edge to edge behind the bar.
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                ),
            topBar = { topBar(scrollBehavior) },
            bottomBar = { bottomBar() },
            content = { innerPadding ->
                val scrollModifier =
                    if (enableDefaultScrollBehavior) Modifier.verticalScroll(rememberScrollState()) else Modifier
                if (columnContent) {
                    Column(
                        modifier = modifier
                            .padding(top = innerPadding.calculateTopPadding())
                            .then(scrollModifier),
                        verticalArrangement = verticalArrangement,
                    ) {
                        content()
                        Spacer(
                            modifier = Modifier
                                .height(innerPadding.calculateBottomPadding() + 24.dp),
                        )
                    }
                } else {
                    // Content that scrolls itself (a LazyColumn, for instance) only needs the
                    // scaffold insets as padding, so it starts below the top bar and ends above
                    // the navigation bar instead of underneath either.
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding(),
                            ),
                    ) {
                        content()
                    }
                }
            },
        )
    }
}
