package aether.killergram.neo.ui.model

import aether.killergram.neo.core.HamburgerMenuItems
import aether.killergram.neo.core.PreferenceKeys

object ToggleCatalog {
    private val hamburgerMenuChoices = HamburgerMenuItems.all.map { item ->
        ToggleChoiceOption(
            value = item.value,
            title = item.fallbackTitle,
            description = item.description,
            resourceNames = item.resourceNames
        )
    }

    val featureSections = listOf(
        ToggleSection(
            title = "Visuals",
            toggles = listOf(
                ModuleToggle(
                    title = "Inject Solar icons",
                    key = PreferenceKeys.SOLAR_ICONS,
                    description = "Replaces default icons with the Solar pack by @Design480.",
                    icon = ToggleIcon.PALETTE
                ),
                ModuleToggle(
                    title = "Add adaptive Monet themes",
                    key = PreferenceKeys.ADAPTIVE_MONET_THEMES,
                    description = "Adds Android 12+ Monet Light and Monet Dark to Telegram's own theme picker and refreshes them when the system palette changes.",
                    icon = ToggleIcon.PALETTE
                ),
                ModuleToggle(
                    title = "Hide stories",
                    key = PreferenceKeys.HIDE_STORIES,
                    description = "Attempts to hide all stories from the app.",
                    icon = ToggleIcon.VISIBILITY_OFF
                ),
                ModuleToggle(
                    title = "Show seconds in timestamps",
                    key = PreferenceKeys.SHOW_SECONDS,
                    description = "Displays time as HH:mm:ss instead of HH:mm across chats and posts.",
                    icon = ToggleIcon.TIMER
                ),
                ModuleToggle(
                    title = "Replace edited text with icon",
                    key = PreferenceKeys.EDITED_ICON,
                    description = "Replaces the \"edited\" label beside message timestamps with a monochrome pencil icon.",
                    icon = ToggleIcon.EDIT
                ),
                ModuleToggle(
                    title = "Hide channel action bar",
                    key = PreferenceKeys.HIDE_CHANNEL_BOTTOM_BAR,
                    description = "Hides the bottom channel action bar (mute/discussion/gifts/join).",
                    icon = ToggleIcon.VERTICAL_SPLIT
                ),
                ModuleToggle(
                    title = "Hide post share button",
                    key = PreferenceKeys.HIDE_POST_SHARE_BUTTON,
                    description = "Hides the side share button on channel posts and channel reposts in group chats.",
                    icon = ToggleIcon.FORWARD
                ),
                ModuleToggle(
                    title = "Hide chat list create button",
                    key = PreferenceKeys.HIDE_DIALOGS_FAB,
                    description = "Hides floating compose/story buttons on the main chat list screen.",
                    icon = ToggleIcon.ADD_BOX_OFF
                ),
                ModuleToggle(
                    title = "Disable subscriber count rounding",
                    key = PreferenceKeys.DISABLE_ROUNDING,
                    description = "Shows exact count: \"1.2K\" becomes \"1,204\".",
                    icon = ToggleIcon.PINCH
                ),
                ModuleToggle(
                    title = "Hide phone number",
                    key = PreferenceKeys.HIDE_PHONE_NUMBER,
                    description = "Replaces your phone number with a placeholder in the drawer and settings.",
                    icon = ToggleIcon.PHONE_OFF
                ),
                ModuleToggle(
                    title = "Customize hamburger menu buttons",
                    key = PreferenceKeys.CUSTOMIZE_HAMBURGER_MENU,
                    description = "Choose which entries Telegram should hide from the left hamburger menu.",
                    icon = ToggleIcon.MENU,
                    parameters = listOf(
                        ToggleParameter(
                            key = PreferenceKeys.HIDDEN_HAMBURGER_MENU_ITEMS,
                            title = "Hidden menu buttons",
                            description = "Names are loaded from the target app when possible so they match Telegram exactly.",
                            type = ToggleParameterType.CHOICE,
                            choiceOptions = hamburgerMenuChoices,
                            allowMultiple = true
                        )
                    )
                )
            )
        ),
        ToggleSection(
            title = "Premium-related",
            toggles = listOf(
                ModuleToggle(
                    title = "Remove sponsored messages",
                    key = PreferenceKeys.REMOVE_SPONSORED,
                    description = "Removes ads from channels.",
                    icon = ToggleIcon.ADS_OFF
                ),
                ModuleToggle(
                    title = "Allow forwarding from anywhere",
                    key = PreferenceKeys.FORCE_FORWARD,
                    description = "Disables protected-chat checks for forwarding.",
                    icon = ToggleIcon.FORWARD
                ),
                ModuleToggle(
                    title = "Override local account limit",
                    key = PreferenceKeys.OVERRIDE_ACCOUNT_LIMIT,
                    description = "Raises in-app account limit to 999.",
                    icon = ToggleIcon.ACCOUNT_TREE
                ),
                ModuleToggle(
                    title = "Hide paid star reactions",
                    key = PreferenceKeys.HIDE_PAID_STAR_REACTIONS,
                    description = "Removes paid star reactions from message bars and reaction selectors.",
                    icon = ToggleIcon.STAR_OFF
                )
            )
        ),
        ToggleSection(
            title = "Miscellaneous",
            toggles = listOf(
                ModuleToggle(
                    title = "Disable audio playback on volume buttons",
                    key = PreferenceKeys.DISABLE_AUTO_AUDIO,
                    description = "Prevents Telegram from playing media when volume keys are pressed.",
                    icon = ToggleIcon.VOLUME_OFF
                ),
                ModuleToggle(
                    title = "Default media send to HD",
                    key = PreferenceKeys.DEFAULT_HD_MEDIA,
                    description = "Defaults photo sending to HD in the media attach menu (not send-as-file).",
                    icon = ToggleIcon.HD
                ),
                ModuleToggle(
                    title = "Blur attach camera tile",
                    key = PreferenceKeys.DISABLE_ATTACH_CAMERA_PREVIEW,
                    description = "Keeps the attach-menu camera tile blurred and opens camera only when tapped.",
                    icon = ToggleIcon.BLUR_ON
                ),
                ModuleToggle(
                    title = "Hide keyboard on chat scroll",
                    key = PreferenceKeys.HIDE_KEYBOARD_ON_SCROLL,
                    description = "Dismisses the keyboard when you start scrolling message history.",
                    icon = ToggleIcon.KEYBOARD_HIDE
                ),
                ModuleToggle(
                    title = "Show profile user ID",
                    key = PreferenceKeys.SHOW_USER_ID,
                    description = "Adds a copyable User ID row to user profile screens.",
                    icon = ToggleIcon.ACCOUNT_TREE
                ),
                ModuleToggle(
                    title = "Force system fonts",
                    key = PreferenceKeys.FORCE_SYSTEM_TYPEFACE,
                    description = "Uses your system typeface instead of Telegram defaults.",
                    icon = ToggleIcon.FONT_DOWNLOAD
                ),
                ModuleToggle(
                    title = "Unlimited recent stickers & emoji",
                    key = PreferenceKeys.UNLIMITED_RECENTS,
                    description = "Raises the recent stickers and emoji limits (server max is 120).",
                    icon = ToggleIcon.HISTORY,
                    parameters = listOf(
                        ToggleParameter(
                            key = PreferenceKeys.RECENT_STICKERS_LIMIT,
                            title = "Recent stickers limit",
                            description = "Maximum number of recent stickers to keep.",
                            type = ToggleParameterType.NUMBER,
                            minValue = 5,
                            maxValue = 120,
                            defaultValue = 120
                        ),
                        ToggleParameter(
                            key = PreferenceKeys.RECENT_EMOJI_LIMIT,
                            title = "Recent emoji limit",
                            description = "Maximum number of recent emoji to keep.",
                            type = ToggleParameterType.NUMBER,
                            minValue = 5,
                            maxValue = 120,
                            defaultValue = 120
                        )
                    )
                ),
                ModuleToggle(
                    title = "Hide app update prompts",
                    key = PreferenceKeys.HIDE_APP_UPDATES,
                    description = "Suppresses Telegram's in-app update availability, banners, and forced update screens.",
                    icon = ToggleIcon.SYSTEM_UPDATE_ALT
                )
            )
        ),
        ToggleSection(
            title = "Testing",
            toggles = listOf(
                ModuleToggle(
                    title = "Force local Premium",
                    key = PreferenceKeys.FORCE_LOCAL_PREMIUM,
                    description = "Bypasses local Premium checks only (server checks still apply).",
                    icon = ToggleIcon.LABS
                ),
                ModuleToggle(
                    title = "Keep all deleted messages",
                    key = PreferenceKeys.KEEP_DELETED_MESSAGES,
                    description = "Forces Telegram DB to retain deleted messages. Highly unstable.",
                    icon = ToggleIcon.RESTORE_PAGE
                )
            )
        )
    )

    val settingsSections = listOf(
        ToggleSection(
            title = "Module settings",
            toggles = listOf(
                ModuleToggle(
                    title = "Debug logging",
                    key = PreferenceKeys.DEBUG_LOGGING,
                    description = "Enable additional Xposed and Android log output.",
                    icon = ToggleIcon.BUG_REPORT
                )
            )
        )
    )
}
