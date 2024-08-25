package aether.killergram.neo.ui.tabs

import aether.killergram.neo.NotEnabledWarning
import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsTab() {
    val context = LocalContext.current
    val prefs = try {
        @Suppress("DEPRECATION")
        context.getSharedPreferences("function_switches", Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
        NotEnabledWarning()
        return
    }
    SwitchGroupList()
}

@Composable
private fun SwitchGroupList() {
    LazyColumn {
        item {
            SwitchGroup(
                title = "Module settings",
                switches = listOf(
                    "Debug logging" to "debug"
                )
            )
        }
    }
}