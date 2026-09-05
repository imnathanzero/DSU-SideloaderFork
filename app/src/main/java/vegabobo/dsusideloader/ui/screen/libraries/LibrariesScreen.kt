package vegabobo.dsusideloader.ui.screen.libraries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.ApplicationScreen
import vegabobo.dsusideloader.ui.components.DynamicListItem
import vegabobo.dsusideloader.ui.components.PreferenceItem
import vegabobo.dsusideloader.ui.components.TopBar
import vegabobo.dsusideloader.ui.screen.Destinations
import vegabobo.dsusideloader.ui.theme.GroupedItemSpacing
import vegabobo.dsusideloader.ui.theme.ScreenHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrariesScreen(
    navigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    // Parsing the bundled library metadata is expensive, so keep it out of recomposition.
    val libraries = remember(context) { Libs.Builder().withContext(context).build().libraries }

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)

    ApplicationScreen(
        enableDefaultScrollBehavior = false,
        columnContent = false,
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .padding(horizontal = ScreenHorizontalPadding),
        topBar = {
            TopBar(
                barTitle = stringResource(id = R.string.libraries_title),
                scrollBehavior = scrollBehavior,
                onClickBackButton = { navigate(Destinations.Up) },
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(GroupedItemSpacing),
        ) {
            items(libraries.size) {
                val thisLibrary = libraries[it]
                val name = thisLibrary.name
                // Separate the license names, otherwise multi-licensed entries read as "MITApache-2.0".
                val licenses = thisLibrary.licenses.joinToString(", ") { license -> license.name }
                val urlToOpen = thisLibrary.website ?: ""
                DynamicListItem(listLength = libraries.size - 1, currentValue = it) {
                    PreferenceItem(
                        title = name,
                        description = licenses,
                        onClick = {
                            if (urlToOpen.isNotEmpty()) {
                                uriHandler.openUri(urlToOpen)
                            }
                        },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(26.dp)) }
        }
    }
}
