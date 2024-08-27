package aether.killergram.neo.ui.tabs

import aether.killergram.neo.isLsposedAvailable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsTab() {
    if (isLsposedAvailable(LocalContext.current)) {
        SwitchGroupList()
    } else {
        StatusCard()
    }
}

@Composable
private fun SwitchGroupList() {
    LazyColumn {
        item {
            SwitchGroup(
                title = "Module settings",
                switches = listOf(
                    Triple("Debug logging", "debug", "Enable additional logging")
                )
            )
        }
    }
}