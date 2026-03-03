package aether.killergram.neo.ui.screens

import aether.killergram.neo.data.ModulePrefsStore
import aether.killergram.neo.ui.components.ModuleStatusCard
import aether.killergram.neo.ui.components.ModuleUnavailableCard
import aether.killergram.neo.ui.components.RestartReminderCard
import aether.killergram.neo.ui.components.ToggleSectionCard
import aether.killergram.neo.ui.model.ToggleSection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ModuleToggleScreen(
    sections: List<ToggleSection>,
    isModuleActive: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember(isModuleActive, context) {
        if (isModuleActive) ModulePrefsStore.create(context) else null
    }
    val allKeys = remember(sections) {
        sections.flatMap { section -> section.toggles.map { toggle -> toggle.key } }
    }
    val states = rememberToggleStates(keys = allKeys, store = store)
    var hasChanges by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModuleStatusCard(isModuleActive = isModuleActive)
        }

        if (!isModuleActive || store == null) {
            item {
                ModuleUnavailableCard()
            }
        } else {
            if (hasChanges) {
                item {
                    RestartReminderCard()
                }
            }
            items(items = sections, key = { it.title }) { section ->
                ToggleSectionCard(
                    section = section,
                    states = states,
                    onToggleChanged = { key, enabled ->
                        if (states[key] != enabled) {
                            states[key] = enabled
                            store.setEnabled(key, enabled)
                            hasChanges = true
                        }
                    }
                )
            }
        }
    }
}
