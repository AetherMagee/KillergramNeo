package aether.killergram.neo.ui.model

enum class ToggleIcon {
    PALETTE,
    MENU,
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
    BUG_REPORT,
    HISTORY,
    PHONE_OFF,
    VIDEOCAM,
    CAMERA_REAR,
    ZOOM_IN,
    HIGH_QUALITY,
    FOLDER
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
    val type: ToggleParameterType,
    val choiceOptions: List<ToggleChoiceOption> = emptyList(),
    val allowMultiple: Boolean = false,
    val defaultChoiceValues: Set<String> = emptySet(),
    val minValue: Int = 0,
    val maxValue: Int = 100,
    val defaultValue: Int = 0
)

data class ToggleChoiceOption(
    val value: String,
    val title: String,
    val description: String = "",
    val resourceNames: List<String> = emptyList()
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
