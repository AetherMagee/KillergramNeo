package aether.killergram.neo.ui.model

enum class ToggleIcon {
    PALETTE,
    VISIBILITY_OFF,
    TIMER,
    EDIT,
    VERTICAL_SPLIT,
    ADD_BOX_OFF,
    PINCH,
    ADS_OFF,
    FORWARD,
    ACCOUNT_TREE,
    STAR_OFF,
    VOLUME_OFF,
    HD,
    BLUR_ON,
    KEYBOARD_HIDE,
    FONT_DOWNLOAD,
    SYSTEM_UPDATE_ALT,
    LABS,
    RESTORE_PAGE,
    BUG_REPORT
}

enum class ToggleParameterType {
    BOOLEAN,
    NUMBER,
    CHOICE,
    TEXT
}

data class ToggleParameter(
    val key: String,
    val title: String,
    val description: String,
    val type: ToggleParameterType
)

data class ModuleToggle(
    val title: String,
    val key: String,
    val description: String,
    val icon: ToggleIcon,
    val parameters: List<ToggleParameter> = emptyList()
)

data class ToggleSection(
    val title: String,
    val toggles: List<ModuleToggle>
)
