package aether.killergram.neo.ui.screens

import aether.killergram.neo.data.ModulePrefsStore
import aether.killergram.neo.data.RestartTargetResolver
import aether.killergram.neo.data.RootActions
import aether.killergram.neo.ui.components.ModuleStatusCard
import aether.killergram.neo.ui.components.ModuleUnavailableCard
import aether.killergram.neo.ui.components.RestartReminderCard
import aether.killergram.neo.ui.components.ToggleSectionCard
import aether.killergram.neo.ui.model.ModuleToggle
import aether.killergram.neo.ui.model.ToggleParameterType
import aether.killergram.neo.ui.model.ToggleSection
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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

    var searchQuery by rememberSaveable { mutableStateOf("") }
    val normalizedQuery = remember(searchQuery) { searchQuery.trim().lowercase() }
    val sectionFilters = remember(sections) { listOf("All") + sections.map { it.title } }
    var selectedSection by rememberSaveable { mutableStateOf("All") }
    var selectedToggleForParameters by remember { mutableStateOf<ModuleToggle?>(null) }

    LaunchedEffect(sectionFilters) {
        if (selectedSection !in sectionFilters) {
            selectedSection = "All"
        }
    }

    val filteredSections by remember(sections, normalizedQuery, selectedSection) {
        derivedStateOf {
            sections.mapNotNull { section ->
                if (selectedSection != "All" && selectedSection != section.title) {
                    return@mapNotNull null
                }

                val matchedToggles = if (normalizedQuery.isBlank()) {
                    section.toggles
                } else {
                    section.toggles.filter { toggle ->
                        toggle.title.lowercase().contains(normalizedQuery) ||
                                toggle.description.lowercase().contains(normalizedQuery)
                    }
                }

                if (matchedToggles.isEmpty()) {
                    null
                } else {
                    section.copy(toggles = matchedToggles)
                }
            }
        }
    }

    selectedToggleForParameters?.let { currentToggle ->
        ModalBottomSheet(
            onDismissRequest = { selectedToggleForParameters = null }
        ) {
            Text(
                text = currentToggle.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            Text(
                text = "Hook parameters",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (currentToggle.parameters.isEmpty()) {
                Text(
                    text = "No parameters are defined yet for this hook.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
            } else {
                currentToggle.parameters.forEach { parameter ->
                    when (parameter.type) {
                        ToggleParameterType.NUMBER -> {
                            NumberParameterRow(
                                parameter = parameter,
                                store = store,
                                onChanged = {
                                    hasChanges = true
                                    applyStatus = null
                                }
                            )
                        }
                        else -> {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = parameter.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = parameter.type.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Changes require a Telegram restart to take effect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModuleStatusCard(
                isModuleActive = isModuleActive
            )
        }

        if (!isModuleActive || store == null) {
            item {
                ModuleUnavailableCard()
            }
            return@LazyColumn
        }

        if (hasChanges) {
            item {
                RestartReminderCard(
                    appName = preferredTarget?.label,
                    hasRootAccess = hasRootAccess,
                    isApplying = isApplyingChanges,
                    statusText = applyStatus,
                    onForceStopClick = if (hasRootAccess == true) {
                        preferredTarget?.let { target ->
                            {
                                scope.launch {
                                    isApplyingChanges = true
                                    applyStatus = null
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
                                        hasChanges = false
                                        "Done. ${target.label} restarted successfully."
                                    } else if (success) {
                                        "Force-stopped ${target.label}, but auto-launch failed."
                                    } else {
                                        "Failed to force-stop via root. Restart manually."
                                    }
                                    isApplyingChanges = false
                                }
                            }
                        }
                    } else {
                        null
                    }
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search toggles") },
                placeholder = { Text("Type feature name or behavior") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sectionFilters.forEach { sectionTitle ->
                    AssistChip(
                        onClick = { selectedSection = sectionTitle },
                        label = { Text(sectionTitle) },
                        leadingIcon = if (selectedSection == sectionTitle) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        if (filteredSections.isEmpty()) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No toggles match your current filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(items = filteredSections, key = { it.title }) { section ->
                ToggleSectionCard(
                    section = section,
                    states = states,
                    onToggleChanged = { key, enabled ->
                        if (states[key] != enabled) {
                            states[key] = enabled
                            store.setEnabled(key, enabled)
                            hasChanges = true
                            applyStatus = null
                        }
                    },
                    onToggleParametersClick = { toggle ->
                        selectedToggleForParameters = toggle
                    }
                )
            }
        }
    }
}

private val ToggleParameterType.label: String
    get() = when (this) {
        ToggleParameterType.BOOLEAN -> "Boolean"
        ToggleParameterType.NUMBER -> "Number"
        ToggleParameterType.CHOICE -> "Choice"
        ToggleParameterType.TEXT -> "Text"
    }

@Composable
private fun NumberParameterRow(
    parameter: aether.killergram.neo.ui.model.ToggleParameter,
    store: ModulePrefsStore?,
    onChanged: () -> Unit
) {
    val currentValue = remember(parameter.key, store) {
        store?.getInt(parameter.key, parameter.defaultValue) ?: parameter.defaultValue
    }
    var sliderValue by remember(parameter.key) { mutableFloatStateOf(currentValue.toFloat()) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = parameter.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sliderValue.roundToInt().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = parameter.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = {
                    val intValue = sliderValue.roundToInt()
                    store?.setInt(parameter.key, intValue)
                    onChanged()
                },
                valueRange = parameter.minValue.toFloat()..parameter.maxValue.toFloat()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = parameter.minValue.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = parameter.maxValue.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
