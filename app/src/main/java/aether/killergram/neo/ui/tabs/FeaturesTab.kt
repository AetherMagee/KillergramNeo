package aether.killergram.neo.ui.tabs

import aether.killergram.neo.NotEnabledWarning
import aether.killergram.neo.RestartReminder
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun FeaturesTab() {
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
    val anySwitchToggled = remember { mutableStateOf(false) }

    Column {
        AnimatedVisibility(
            visible = anySwitchToggled.value,
            enter = expandVertically(),
        ) {
            RestartReminder()
        }
        LazyColumn() {
            item {
                SwitchGroup(title = "Visuals", switches = listOf(
                    "Inject Solar icons" to "solar",
                    "Hide Stories" to "stories",
                    "Disable \"Thanos\" deletion effect" to "thanos"
                ), anySwitchToggled)
            }
            item {
                SwitchGroup(title = "Premium-related", switches = listOf(
                    "Remove sponsored messages" to "sponsored",
                    "Allow forwarding from anywhere" to "forward",
                    "Override local account limit" to "accountlimit"
                ), anySwitchToggled)
            }
            item {
                SwitchGroup(title = "Miscellaneous", switches = listOf(
                    "Disable audio playback on volume button press" to "volume"
                ), anySwitchToggled)
            }
            item {
                SwitchGroup(title = "Testing", switches = listOf(
                    "Force local Premium" to "localpremium",
                    "Keep ALL deleted messages" to "deleted"
                ), anySwitchToggled)
            }
        }
    }

}