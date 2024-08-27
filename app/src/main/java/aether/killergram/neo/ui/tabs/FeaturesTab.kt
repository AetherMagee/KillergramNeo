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
                SwitchGroup(
                    title = "Visuals",
                    switches = listOf(
                        Triple(
                            "Inject Solar icons",
                            "solar",
                            "Replaces default icons with the Solar pack by @Design480"
                        ),
                        Triple(
                            "Hide Stories",
                            "stories",
                            "Attempts to hide all Stories from the app"
                        ),
                        Triple(
                            "Disable sub count rounding",
                            "norounding",
                            "Shows exact subscriber count\n\"1.2K\" -> \"1,204\""
                        )
                    ),
                    anySwitchToggled
                )
            }
            item {
                SwitchGroup(
                    title = "Premium-related",
                    switches = listOf(
                        Triple(
                            "Remove sponsored messages",
                            "sponsored",
                            "Remove all ads from channels"
                        ),
                        Triple(
                            "Allow forwarding from anywhere",
                            "forward",
                            "Disables checks for \"Protected chats\" and allows you to forward from anywhere"
                        ),
                        Triple(
                            "Override local account limit",
                            "accountlimit",
                            "Sets the maximum amount of accounts in the app to 999.\nYou aren't gonna add that much... right?"
                        )
                    ),
                    anySwitchToggled
                )
            }
            item {
                SwitchGroup(
                    title = "Miscellaneous",
                    switches = listOf(
                        Triple(
                            "Disable audio playback on volume button press",
                            "volume",
                            "Turn off that annoying behaviour of the app playing the audio from the video on the screen instead of lowering your volume"
                        )
                    ),
                    anySwitchToggled
                )
            }
            item {
                SwitchGroup(
                    title = "Testing",
                    switches = listOf(
                        Triple(
                            "Force local Premium",
                            "localpremium",
                            "Forces all the checks for premium to succeed. Doesn't bypass serverside checks."
                        ),
                        Triple(
                            "Keep ALL deleted messages",
                            "deleted",
                            "Forces Telegram's internal database to keep ALL messages. Even yours."
                        )
                    ),
                    anySwitchToggled
                )
            }
        }
    }
}

