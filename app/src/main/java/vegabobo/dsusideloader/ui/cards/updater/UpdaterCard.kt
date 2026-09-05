package vegabobo.dsusideloader.ui.cards.updater

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.BuildConfig
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.PreferenceItem
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.components.buttons.SecondaryButton
import vegabobo.dsusideloader.ui.screen.about.UpdateStatus
import vegabobo.dsusideloader.ui.screen.about.UpdaterCardState
import vegabobo.dsusideloader.ui.theme.accentHalo
import vegabobo.dsusideloader.ui.theme.cardContainerHighest

@Composable
fun UpdaterCard(
    uiState: UpdaterCardState,
    isUpdaterAvailable: Boolean,
    onClickImage: () -> Unit,
    onClickCheckUpdates: () -> Unit,
    onClickDownloadUpdate: () -> Unit,
    onClickViewChangelog: () -> Unit,
) {
    fun isDownloading(): Boolean =
        uiState.isDownloading || uiState.updateStatus == UpdateStatus.CHECKING_FOR_UPDATES

    fun isCheckingForUpdates(): Boolean =
        uiState.updateStatus == UpdateStatus.CHECKING_FOR_UPDATES

    fun isUpdateFound(): Boolean =
        uiState.updateStatus == UpdateStatus.UPDATE_FOUND

    SimpleCard(
        addPadding = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 24.dp, bottom = 8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.cardContainerHighest,
            ) {
                Box(
                    // A wash of the two accent hues the wallpaper produced, so the one
                    // decorative surface in the app actually shows the extracted color
                    // instead of being a third neutral container. The disc is wider than
                    // the icon on purpose, otherwise the icon covers the whole halo.
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = MaterialTheme.colorScheme.accentHalo,
                            shape = CircleShape,
                        ),
                ) {
                    val progressBarModifier = Modifier
                        .size(104.dp)
                        .align(Alignment.Center)
                    if (isCheckingForUpdates()) {
                        CircularProgressIndicator(modifier = progressBarModifier)
                    }
                    if (uiState.isDownloading) {
                        CircularProgressIndicator(
                            progress = uiState.progressBar,
                            modifier = progressBarModifier,
                        )
                    }

                    // Derived straight from the state — no mutable holder written during composition.
                    val scale = animateFloatAsState(if (isDownloading()) 0.75f else 1f)
                    Image(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(scale.value)
                            .clip(CircleShape)
                            .align(Alignment.Center)
                            .clickable { onClickImage() },
                        painter = painterResource(id = R.drawable.app_icon_mini),
                        contentDescription = stringResource(id = R.string.app_name),
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(
                    id = R.string.version_info,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
        if (isUpdaterAvailable) {
            PreferenceItem(
                title = stringResource(id = R.string.check_updates_title),
                description =
                when (uiState.updateStatus) {
                    UpdateStatus.NO_UPDATE_FOUND ->
                        stringResource(id = R.string.check_updates_text_updated)

                    UpdateStatus.UPDATE_FOUND ->
                        stringResource(R.string.check_updates_text_found, uiState.updateVersion)

                    UpdateStatus.FAILED ->
                        stringResource(id = R.string.check_updates_text_failed)

                    else ->
                        stringResource(id = R.string.check_updates_text_idle)
                },
                icon = Icons.Outlined.Update,
                isEnabled = !isDownloading(),
                onClick = { onClickCheckUpdates() },
            )
            AnimatedVisibility(visible = isUpdateFound()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                ) {
                    Spacer(modifier = Modifier.weight(1F))
                    SecondaryButton(
                        text = stringResource(id = R.string.changelog),
                        onClick = { onClickViewChangelog() },
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    PrimaryButton(
                        text = stringResource(id = R.string.download),
                        onClick = { onClickDownloadUpdate() },
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}
