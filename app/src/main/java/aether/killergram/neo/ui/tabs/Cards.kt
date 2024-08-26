package aether.killergram.neo.ui.tabs

import aether.killergram.neo.isLsposedAvailable
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun StatusCard() {
    val status: String
    val description: String
    val icon: ImageVector?

    if (isLsposedAvailable(LocalContext.current)) {
        val appVer = LocalContext.current.packageManager.getPackageInfo(LocalContext.current.packageName, 0).versionName



        status = "Killergram Neo is active!"
        description = "Version: $appVer"
        icon = Icons.Filled.CheckCircle
    } else {
        status = "Not connected to LSPosed!"
        description = "Please enable the module, then restart this app."
        icon = Icons.Filled.Warning
    }

    ElevatedCard(
        onClick = { },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, status)

            Column(
                Modifier
                    .weight(2f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@SuppressLint("WorldReadableFiles")
@Composable
fun RestartReminder() {
    ElevatedCard(
        onClick = { },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Tip"
            )
            Text(
                text = "Don't forget to restart your target app after making changes!",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 16.dp)
            )
        }
    }
}