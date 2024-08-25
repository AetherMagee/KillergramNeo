package aether.killergram.neo.ui.tabs

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@SuppressLint("WorldReadableFiles")
@Composable
fun SwitchGroup(title: String, switches: List<Pair<String, String>>, anySwitchToggled: MutableState<Boolean>? = null) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("function_switches", Context.MODE_WORLD_READABLE)

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
                if (anyState != null) {
                    anyState.value = true
                }
            }
        )
    }
}