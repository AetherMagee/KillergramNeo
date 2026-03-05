package aether.killergram.neo.ui.screens

import aether.killergram.neo.isLsposedAvailable
import aether.killergram.neo.ui.navigation.TopLevelDestination
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var isModuleActive by remember(context) { mutableStateOf(isLsposedAvailable(context)) }
    val tabs = TopLevelDestination.entries
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedTab = tabs[selectedTabIndex]

    LifecycleResumeEffect(context) {
        isModuleActive = isLsposedAvailable(context)
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = selectedTab.label) }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = tab.ordinal == selectedTabIndex
                    NavigationBarItem(
                        selected = selected,
                        onClick = { selectedTabIndex = tab.ordinal },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            TopLevelDestination.FEATURES -> FeaturesScreen(
                isModuleActive = isModuleActive,
                modifier = Modifier.padding(padding)
            )

            TopLevelDestination.SETTINGS -> SettingsScreen(
                isModuleActive = isModuleActive,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
