package aether.killergram.neo.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun FeaturesTab() {
    SwitchGroupList()
}

@Composable
private fun SwitchGroupList() {
    val anySwitchToggled = remember { mutableStateOf(false) }

    Column {
        LazyColumn() {
            item {
                StatusCard()
            }
            item {
                AnimatedVisibility(
                visible = anySwitchToggled.value,
                enter = expandVertically(),
            ) {
                RestartReminder()
            }

            }
            item {
                SwitchGroup(title = "Visuals", switches = listOf(
                    "Inject Solar icons" to "solar",
                    "Hide Stories" to "stories",
                    "Disable \"Thanos\" deletion effect" to "thanos",
                    "Disable sub count rounding" to "norounding"
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

