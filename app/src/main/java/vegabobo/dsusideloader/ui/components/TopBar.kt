package vegabobo.dsusideloader.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    barTitle: String,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    onClickIcon: () -> Unit = {},
    onClickBackButton: (() -> Unit)? = null,
) {
    LargeTopAppBar(
        title = {
            Text(
                text = barTitle,
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        navigationIcon = {
            if (onClickBackButton != null) {
                IconButton(onClick = onClickBackButton) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = iconContentDescription,
                    )
                }
            }
        },
        actions = {
            if (icon != null) {
                IconButton(onClick = onClickIcon) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        scrollBehavior = scrollBehavior,
    )
}
