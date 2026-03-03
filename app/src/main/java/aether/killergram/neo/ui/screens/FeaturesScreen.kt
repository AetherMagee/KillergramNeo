package aether.killergram.neo.ui.screens

import aether.killergram.neo.ui.model.ToggleCatalog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FeaturesScreen(
    isModuleActive: Boolean,
    modifier: Modifier = Modifier
) {
    ModuleToggleScreen(
        sections = ToggleCatalog.featureSections,
        isModuleActive = isModuleActive,
        modifier = modifier
    )
}
