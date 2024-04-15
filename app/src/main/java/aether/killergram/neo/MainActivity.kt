package aether.killergram.neo

import aether.killergram.neo.ui.theme.KillergramNeoTheme
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KillergramNeoTheme {
                LazyColumn {
                    item {
                        RestartReminder()
                    }
                    item {
                        SwitchScreen(
                            title = "Module settings",
                            switches = listOf(
                                "Debug logging" to "debug"
                            )
                        )
                    }
                    item {
                        SwitchScreen(
                            title = "Hooks",
                            switches = listOf(
                                "Inject Solar icons" to "solar",
                                "Hide Stories" to "stories",
                                "Disable audio enabling on volume buttons" to "volume",
                                "Remove sponsored messages" to "sponsored",
                                "Allow forwarding from anywhere" to "forward",
                                "Override account limit" to "accountlimit",
                                "[TESTING] Force local Premium" to "localpremium",
                                "[TESTING] Keep deleted messages" to "deleted"
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotEnabledWarning() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp)
            .background(Color.Red, shape = RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
        ) {
            Text(
                text = "Not enabled. Please enable the module in LSPosed first and then restart this app.",
                style = TextStyle(color = Color.White, fontSize = 20.sp),
            )
        }
    }
}

@Composable
fun RestartReminder() {
    // Copied from below just to check
    val context = LocalContext.current
    val prefs = try {
        @Suppress("DEPRECATION")
        context.getSharedPreferences("function_switches", Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
        return
    }
    
    Box(modifier = Modifier
        .padding(16.dp)
        .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(20.dp)),
    ) {
        Box(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "Don't forget to restart the target app after making changes!",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@SuppressLint("WorldReadableFiles")
@Composable
fun SwitchScreen(title: String, switches: List<Pair<String, String>>) {
    val context = LocalContext.current
    val prefs = try {
        @Suppress("DEPRECATION")
        context.getSharedPreferences("function_switches", Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
        if (title == "Hooks") {
            NotEnabledWarning()
        }
        return
    }

    val switchStates = switches.map { switch ->
        remember { mutableStateOf(prefs.getBoolean(switch.second, false)) }
    }

    Box(modifier = Modifier
        .padding(16.dp)
        .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(20.dp)),
        ) {
            // Payload
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .padding(8.dp, bottom = 10.dp)
                            .align(alignment = Alignment.CenterHorizontally),
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    switches.zip(switchStates).forEach { (switch, state) ->
                        SwitchComposable(switch = switch, state = state, prefs = prefs)
                }
            }
        }
    }
}

@Composable
fun SwitchComposable(switch: Pair<String, String>, state: MutableState<Boolean>, prefs: SharedPreferences) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = switch.first,
            fontSize = 18.sp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = state.value,
            onCheckedChange = { newValue ->
                state.value = newValue
                prefs.edit().putBoolean(switch.second, newValue).commit()
            }
        )
    }
}