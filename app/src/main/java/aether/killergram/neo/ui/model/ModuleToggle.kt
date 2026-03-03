package aether.killergram.neo.ui.model

data class ModuleToggle(
    val title: String,
    val key: String,
    val description: String
)

data class ToggleSection(
    val title: String,
    val toggles: List<ModuleToggle>
)
