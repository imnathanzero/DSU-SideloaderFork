package vegabobo.dsusideloader.ui.screen.about

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.cards.updater.UpdaterCard
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.PreferenceItem
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.Title
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.ui.theme.ScreenHorizontalPadding
import vegabobo.dsusideloader.util.collectAsStateWithLifecycle

object AboutLinks {
    const val CONTRIBUTORS_URL = "https://github.com/VegaBobo/DSU-Sideloader/graphs/contributors"
    const val REPOSITORY_URL = "https://github.com/VegaBobo/DSU-Sideloader"
    const val WSTXDA_GITHUB = "https://github.com/WSTxda"
    const val VEGABOBO_GITHUB = "https://github.com/VegaBobo"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navigate: (String) -> Unit,
    aboutViewModel: AboutViewModel = hiltViewModel(),
) {
    val uiState by aboutViewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        aboutViewModel.resetDeveloperOptionsCounter()
        uiState.toastDisplay.collectLatest {
            val message = when (it) {
                DevOptToastDisplay.ENABLED_DEV_OPT -> R.string.developer_options_enabled
                DevOptToastDisplay.DISABLED_DEV_OPT -> R.string.developer_options_disabled
                DevOptToastDisplay.NONE -> return@collectLatest
            }
            Toast.makeText(context, context.getString(message), Toast.LENGTH_LONG).show()
            // Consume it, otherwise returning to this screen replays the last toast.
            aboutViewModel.onToastDisplayed()
        }
    }

    ApplicationScreen(
        modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.about),
                scrollBehavior = it,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        UpdaterCard(
            uiState = uiState.updaterCardState,
            isUpdaterAvailable = uiState.isUpdaterAvailable,
            onClickImage = { aboutViewModel.onClickImage() },
            onClickCheckUpdates = { aboutViewModel.onClickCheckUpdates() },
            onClickDownloadUpdate = { aboutViewModel.onClickDownloadUpdate() },
            onClickViewChangelog = {
                aboutViewModel.changelogUrl?.let { url -> uriHandler.openUri(url) }
            },
        )
        Title(stringResource(id = R.string.application))
        SimpleCard(
            addPadding = false,
        ) {
            PreferenceItem(
                title = stringResource(id = R.string.github_repo),
                description = stringResource(id = R.string.github_repo_description),
                icon = Icons.Outlined.Code,
                onClick = { uriHandler.openUri(AboutLinks.REPOSITORY_URL) },
            )
            PreferenceItem(
                title = stringResource(id = R.string.libraries_title),
                description = stringResource(id = R.string.libraries_description),
                icon = Icons.Outlined.LibraryBooks,
                onClick = { navigate(Destinations.Libraries) },
            )
        }
        Title(stringResource(id = R.string.collaborators))
        SimpleCard(
            addPadding = false,
        ) {
            PreferenceItem(
                title = "VegaBobo",
                description = stringResource(id = R.string.role_developer),
                icon = Icons.Outlined.DeveloperMode,
                onClick = { uriHandler.openUri(AboutLinks.VEGABOBO_GITHUB) },
            )
            PreferenceItem(
                title = "WSTxda",
                description = stringResource(id = R.string.role_design_icon),
                icon = Icons.Outlined.Brush,
                onClick = { uriHandler.openUri(AboutLinks.WSTXDA_GITHUB) },
            )
            val translators = stringResource(id = R.string.translators_list)
            if (translators.isNotEmpty() && translators != "translators_list") {
                PreferenceItem(
                    title = stringResource(id = R.string.translators_title),
                    description = stringResource(id = R.string.translators_list),
                    icon = Icons.Outlined.Translate,
                )
            }
            PreferenceItem(
                title = stringResource(id = R.string.contributors_title),
                description = stringResource(id = R.string.contributors_text),
                icon = Icons.Outlined.Group,
                onClick = { uriHandler.openUri(AboutLinks.CONTRIBUTORS_URL) },
            )
        }
    }
}