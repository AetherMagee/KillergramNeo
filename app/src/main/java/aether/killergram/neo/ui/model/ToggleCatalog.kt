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
            title = "Appearance",
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
                    title = "Force system fonts",
                    key = PreferenceKeys.FORCE_SYSTEM_TYPEFACE,
                    description = "Uses your system typeface instead of Telegram defaults.",
                    icon = ToggleIcon.FONT_DOWNLOAD
                ),
                ModuleToggle(
                    title = "Replace edited text with icon",
                    key = PreferenceKeys.EDITED_ICON,
                    description = "Replaces the \"edited\" label beside message timestamps with a monochrome pencil icon.",
                    icon = ToggleIcon.EDIT
                ),
                ModuleToggle(
                    title = "Show seconds in timestamps",
                    key = PreferenceKeys.SHOW_SECONDS,
                    description = "Displays time as HH:mm:ss instead of HH:mm across chats and posts.",
                    icon = ToggleIcon.TIMER
                ),
                ModuleToggle(
                    title = "Disable subscriber count rounding",
                    key = PreferenceKeys.DISABLE_ROUNDING,
                    description = "Shows exact count: \"1.2K\" becomes \"1,204\".",
                    icon = ToggleIcon.PINCH
                ),
                ModuleToggle(
                    title = "Replace app title",
                    key = PreferenceKeys.REPLACE_APP_TITLE,
                    description = "Replaces \"Telegram\" in the title bar with your account's first name or custom text.",
                    icon = ToggleIcon.EDIT,
                    parameters = listOf(
                        ToggleParameter(
                            key = PreferenceKeys.APP_TITLE_MODE,
                            title = "Title content",
                            description = "What to show instead of the app name.",
                            type = ToggleParameterType.CHOICE,
                            choiceOptions = listOf(
                                ToggleChoiceOption("firstname", "Account first name", "Uses your Telegram account's first name."),
                                ToggleChoiceOption("custom", "Custom text", "Enter any text you want.")
                            ),
                            defaultChoiceValues = setOf("firstname")
                        ),
                        ToggleParameter(
                            key = PreferenceKeys.APP_TITLE_CUSTOM_TEXT,
                            title = "Custom title text",
                            description = "Only used when mode is set to \"Custom text\".",
                            type = ToggleParameterType.TEXT,
                            defaultTextValue = ""
                        ),
                        ToggleParameter(
                            key = PreferenceKeys.APP_TITLE_CENTER,
                            title = "Center title",
                            description = "Centers the title in the action bar.",
                            type = ToggleParameterType.BOOLEAN
                        )
                    )
                )
            )
        ),
        ToggleSection(
            title = "Navigation",
            toggles = listOf(
                ModuleToggle(
                    title = "Hide stories",
                    key = PreferenceKeys.HIDE_STORIES,
                    description = "Attempts to hide all stories from the app.",
                    icon = ToggleIcon.VISIBILITY_OFF
                ),
                ModuleToggle(
                    title = "Hide chat list create button",
                    key = PreferenceKeys.HIDE_DIALOGS_FAB,
                    description = "Hides floating compose/story buttons on the main chat list screen.",
                    icon = ToggleIcon.ADD_BOX_OFF
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
                    title = "Hide keyboard on chat scroll",
                    key = PreferenceKeys.HIDE_KEYBOARD_ON_SCROLL,
                    description = "Dismisses the keyboard when you start scrolling message history.",
                    icon = ToggleIcon.KEYBOARD_HIDE
                ),
                ModuleToggle(
                    title = "Folder icons",
                    key = PreferenceKeys.FOLDER_ICONS,
                    description = "Adds an icon picker to the folder editor and shows folder icons in the tab bar.",
                    icon = ToggleIcon.FOLDER,
                    parameters = listOf(
                        ToggleParameter(
                            key = PreferenceKeys.FOLDER_TAB_DISPLAY_MODE,
                            title = "Tab display mode",
                            description = "How folders appear in the tab bar.",
                            type = ToggleParameterType.CHOICE,
                            choiceOptions = listOf(
                                ToggleChoiceOption("icon", "Icons only"),
                                ToggleChoiceOption("mix", "Icons with text"),
                                ToggleChoiceOption("text", "Text only")
                            ),
                            defaultChoiceValues = setOf("icon")
                        )
                    )
                )
            )
        ),
        ToggleSection(
            title = "Camera",
            toggles = listOf(
                ModuleToggle(
                    title = "Higher video bitrate",
                    key = PreferenceKeys.CAMERA_HIGHER_BITRATE,
                    description = "Triples camera recording bitrate. Configurable for video notes.",
                    icon = ToggleIcon.VIDEOCAM,
                    parameters = listOf(
                        ToggleParameter(
                            key = PreferenceKeys.CAMERA_BITRATE_VALUE,
                            title = "Video note bitrate (kbps)",
                            description = "Bitrate for round video messages.",
                            type = ToggleParameterType.CHOICE,
                            choiceOptions = listOf(
                                ToggleChoiceOption("600", "600 kbps"),
                                ToggleChoiceOption("800", "800 kbps"),
                                ToggleChoiceOption("1000", "1000 kbps", "Telegram default"),
                                ToggleChoiceOption("1200", "1200 kbps"),
                                ToggleChoiceOption("1400", "1400 kbps")
                            ),
                            defaultChoiceValues = setOf("1200")
                        )
                    )
                ),
                ModuleToggle(
                    title = "Higher video note resolution",
                    key = PreferenceKeys.CAMERA_HIGHER_RESOLUTION,
                    description = "Increases video note capture resolution.",
                    icon = ToggleIcon.HIGH_QUALITY,
                    parameters = listOf(
                        ToggleParameter(
                            key = PreferenceKeys.CAMERA_RESOLUTION_VALUE,
                            title = "Video note resolution (px)",
                            description = "Square resolution for round video messages.",
                            type = ToggleParameterType.CHOICE,
                            choiceOptions = listOf(
                                ToggleChoiceOption("128", "128px"),
                                ToggleChoiceOption("256", "256px"),
                                ToggleChoiceOption("384", "384px", "Telegram default"),
                                ToggleChoiceOption("512", "512px"),
                                ToggleChoiceOption("640", "640px")
                            ),
                            defaultChoiceValues = setOf("512")
                        )
                    )
                ),
                ModuleToggle(
                    title = "Default to rear camera",
                    key = PreferenceKeys.CAMERA_DEFAULT_BACK,
                    description = "Opens camera and video notes with the back camera instead of front.",
                    icon = ToggleIcon.CAMERA_REAR
                ),
                ModuleToggle(
                    title = "Keep video note zoom",
                    key = PreferenceKeys.CAMERA_KEEP_ZOOM,
                    description = "Prevents zoom from resetting to 1x when you lift your fingers during recording.",
                    icon = ToggleIcon.ZOOM_IN
                )
            )
        ),
        ToggleSection(
            title = "Chats & Media",
            toggles = listOf(
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
                    title = "Disable audio playback on volume buttons",
                    key = PreferenceKeys.DISABLE_AUTO_AUDIO,
                    description = "Prevents Telegram from playing media when volume keys are pressed.",
                    icon = ToggleIcon.VOLUME_OFF
                ),
                ModuleToggle(
                    title = "Disable notification delay",
                    key = PreferenceKeys.DISABLE_NOTIFICATION_DELAY,
                    description = "Removes Telegram's built-in 1s or 3s wait before posting new-message notifications.",
                    icon = ToggleIcon.TIMER
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
                )
            )
        ),
        ToggleSection(
            title = "Privacy & Profile",
            toggles = listOf(
                ModuleToggle(
                    title = "Hide phone number",
                    key = PreferenceKeys.HIDE_PHONE_NUMBER,
                    description = "Replaces your phone number with a placeholder in the drawer and settings.",
                    icon = ToggleIcon.PHONE_OFF
                ),
                ModuleToggle(
                    title = "Show profile user ID",
                    key = PreferenceKeys.SHOW_USER_ID,
                    description = "Adds a copyable User ID row to user profile screens.",
                    icon = ToggleIcon.ACCOUNT_TREE
                ),
                ModuleToggle(
                    title = "Allow forwarding from anywhere",
                    key = PreferenceKeys.FORCE_FORWARD,
                    description = "Disables protected-chat checks for forwarding.",
                    icon = ToggleIcon.FORWARD
                )
            )
        ),
        ToggleSection(
            title = "Restrictions & Cleanup",
            toggles = listOf(
                ModuleToggle(
                    title = "Remove sponsored messages",
                    key = PreferenceKeys.REMOVE_SPONSORED,
                    description = "Removes ads from channels.",
                    icon = ToggleIcon.ADS_OFF
                ),
                ModuleToggle(
                    title = "Hide paid star reactions",
                    key = PreferenceKeys.HIDE_PAID_STAR_REACTIONS,
                    description = "Removes paid star reactions from message bars and reaction selectors.",
                    icon = ToggleIcon.STAR_OFF
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
            title = "Experimental",
            toggles = listOf(
                ModuleToggle(
                    title = "Force local Premium",
                    key = PreferenceKeys.FORCE_LOCAL_PREMIUM,
                    description = "Bypasses local Premium checks only (server checks still apply).",
                    icon = ToggleIcon.LABS
                ),
                ModuleToggle(
                    title = "Anti-recall deleted messages",
                    key = PreferenceKeys.KEEP_DELETED_MESSAGES,
                    description = "Stores deleted messages in a separate local sidecar DB, keeps them in history, dims them to 50%, and marks them with a trash icon.",
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
