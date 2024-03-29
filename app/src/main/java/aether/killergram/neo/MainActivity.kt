package aether.killergram.neo

import aether.killergram.neo.ui.theme.KillergramNeoTheme
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KillergramNeoTheme {
                Column {
                    SwitchScreen(
                        switches = listOf(
                            "Remove sponsored messages" to "sponsored",
                            "Allow forwarding from anywhere" to "forward",
                            "Override account limit" to "accountlimit",
                            "Hide Stories" to "stories",
                            "Inject Solar icons by @Design480" to "solar",
                            "Force local Premium" to "localpremium"
                        )
                    )
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

@SuppressLint("WorldReadableFiles")
@Composable
fun SwitchScreen(switches: List<Pair<String, String>>) {
    val context = LocalContext.current
    val prefs = try {
        @Suppress("DEPRECATION")
        context.getSharedPreferences("function_switches", Context.MODE_WORLD_READABLE)
    } catch (e: SecurityException) {
        NotEnabledWarning()
        return
    }

    val switchStates = switches.map { switch ->
        remember { mutableStateOf(prefs.getBoolean(switch.second, false)) }
    }

    Box(modifier = Modifier
        .padding(16.dp)
        .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(20.dp)),
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            switches.zip(switchStates).forEach { (switch, state) ->
                SwitchComposable(switch = switch, state = state, prefs = prefs)
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