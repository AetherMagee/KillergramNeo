package aether.killergram.neo.ui.tabs

import aether.killergram.neo.isLsposedAvailable
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@SuppressLint("WorldReadableFiles")
@Composable
fun SwitchGroup(title: String, switches: List<Pair<String, String>>, anySwitchToggled: MutableState<Boolean>? = null) {
    val context = LocalContext.current
    if (!isLsposedAvailable(context)) {
        return
    }
    val prefs = context.getSharedPreferences("function_switches", Context.MODE_WORLD_READABLE)

    val switchStates = switches.map { switch ->
        remember { mutableStateOf(prefs.getBoolean(switch.second, false)) }
    }

    Box(modifier = Modifier
        .padding(16.dp)
        .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(20.dp)),
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
                    modifier = Modifier
                        .padding(8.dp, bottom = 10.dp)
                        .align(alignment = Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge
                )

                switches.zip(switchStates).forEach { (switch, state) ->
                    SwitchComposable(switch = switch, state = state, prefs = prefs, anyState = anySwitchToggled)
                }
            }
        }
    }
}

@Composable
fun SwitchComposable(switch: Pair<String, String>, state: MutableState<Boolean>, prefs: SharedPreferences, anyState: MutableState<Boolean>?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = switch.first,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = state.value,
            onCheckedChange = { newValue ->
                state.value = newValue
                prefs.edit().putBoolean(switch.second, newValue).commit()
                if (anyState != null) {
                    anyState.value = true
                }
            }
        )
    }
}