package vegabobo.dsusideloader.ui.cards.warnings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vegabobo.dsusideloader.R
import vegabobo.dsusideloader.ui.components.SimpleCard
import vegabobo.dsusideloader.ui.components.buttons.PrimaryButton
import vegabobo.dsusideloader.ui.util.launcherAcResult

@Composable
fun SetupStorage(
    onSetupStorageSuccess: (Uri) -> Unit,
) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    val launcherSetupStorage = launcherAcResult {
        onSetupStorageSuccess(it)
    }

    SimpleCard(
        modifier = Modifier.fillMaxWidth(),
        cardIcon = Icons.Outlined.FolderOpen,
        cardTitle = stringResource(id = R.string.setup_storage),
        text = stringResource(id = R.string.setup_storage_description),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Spacer(modifier = Modifier.weight(1F))
            PrimaryButton(
                text = stringResource(id = R.string.setup),
                onClick = { launcherSetupStorage.launch(intent) },
            )
        }
    }
}
