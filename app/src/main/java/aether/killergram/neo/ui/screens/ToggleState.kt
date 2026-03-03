package aether.killergram.neo.ui.screens

import aether.killergram.neo.data.ModulePrefsStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap

@Composable
internal fun rememberToggleStates(
    keys: List<String>,
    store: ModulePrefsStore?
): SnapshotStateMap<String, Boolean> {
    return remember(keys, store) {
        val persistedStates = store?.getStates(keys).orEmpty()
        mutableStateMapOf<String, Boolean>().apply {
            keys.forEach { key ->
                this[key] = persistedStates[key] ?: false
            }
        }
    }
}
