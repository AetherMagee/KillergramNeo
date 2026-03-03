package aether.killergram.neo.ui.screens

import aether.killergram.neo.data.ModulePrefsStore
import aether.killergram.neo.data.RestartTargetResolver
import aether.killergram.neo.data.RootActions
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay

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
    val preferredTarget = remember(context) { RestartTargetResolver.resolvePreferredTarget(context) }
    val hasRootAccess by produceState<Boolean?>(initialValue = null, context) {
        value = RootActions.hasRootAccess()
    }
    val scope = rememberCoroutineScope()
    var isApplyingChanges by remember { mutableStateOf(false) }
    var applyStatus by remember { mutableStateOf<String?>(null) }

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
                    RestartReminderCard(
                        appName = preferredTarget?.label,
                        hasRootAccess = hasRootAccess,
                        isApplying = isApplyingChanges,
                        statusText = applyStatus,
                        onForceStopClick = if (hasRootAccess == true && preferredTarget != null) {
                            {
                                scope.launch {
                                    isApplyingChanges = true
                                    applyStatus = null
                                    val target = preferredTarget!!

                                    val success = RootActions.forceStopPackage(target.packageName)
                                    if (success) {
                                        delay(250)
                                    }
                                    val relaunched = if (success) {
                                        RestartTargetResolver.launchTarget(context, target.packageName)
                                    } else {
                                        false
                                    }
                                    applyStatus = if (success && relaunched) {
                                        RestartTargetResolver.rememberTargetPackage(
                                            context,
                                            target.packageName
                                        )
                                        "Done. ${target.label} was restarted."
                                    } else if (success) {
                                        "Force-stopped ${target.label}, but auto-launch failed."
                                    } else {
                                        "Failed to force-stop via root. Restart manually."
                                    }
                                    isApplyingChanges = false
                                }
                            }
                        } else {
                            null
                        }
                    )
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
