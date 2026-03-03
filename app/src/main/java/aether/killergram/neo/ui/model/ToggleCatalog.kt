package aether.killergram.neo.ui.model

import aether.killergram.neo.core.PreferenceKeys

object ToggleCatalog {
    val featureSections = listOf(
        ToggleSection(
            title = "Visuals",
            toggles = listOf(
                ModuleToggle(
                    title = "Inject Solar icons",
                    key = PreferenceKeys.SOLAR_ICONS,
                    description = "Replaces default icons with the Solar pack by @Design480."
                ),
                ModuleToggle(
                    title = "Hide stories",
                    key = PreferenceKeys.HIDE_STORIES,
                    description = "Attempts to hide all stories from the app."
                ),
                ModuleToggle(
                    title = "Show seconds in timestamps",
                    key = PreferenceKeys.SHOW_SECONDS,
                    description = "Displays time as HH:mm:ss instead of HH:mm across chats and posts."
                ),
                ModuleToggle(
                    title = "Hide channel action bar",
                    key = PreferenceKeys.HIDE_CHANNEL_BOTTOM_BAR,
                    description = "Hides the bottom channel action bar (mute/discussion/gifts/join)."
                ),
                ModuleToggle(
                    title = "Disable subscriber count rounding",
                    key = PreferenceKeys.DISABLE_ROUNDING,
                    description = "Shows exact count: \"1.2K\" becomes \"1,204\"."
                )
            )
        ),
        ToggleSection(
            title = "Premium-related",
            toggles = listOf(
                ModuleToggle(
                    title = "Remove sponsored messages",
                    key = PreferenceKeys.REMOVE_SPONSORED,
                    description = "Removes ads from channels."
                ),
                ModuleToggle(
                    title = "Allow forwarding from anywhere",
                    key = PreferenceKeys.FORCE_FORWARD,
                    description = "Disables protected-chat checks for forwarding."
                ),
                ModuleToggle(
                    title = "Override local account limit",
                    key = PreferenceKeys.OVERRIDE_ACCOUNT_LIMIT,
                    description = "Raises in-app account limit to 999."
                )
            )
        ),
        ToggleSection(
            title = "Miscellaneous",
            toggles = listOf(
                ModuleToggle(
                    title = "Disable audio playback on volume buttons",
                    key = PreferenceKeys.DISABLE_AUTO_AUDIO,
                    description = "Prevents Telegram from playing media when volume keys are pressed."
                ),
                ModuleToggle(
                    title = "Force system fonts",
                    key = PreferenceKeys.FORCE_SYSTEM_TYPEFACE,
                    description = "Uses your system typeface instead of Telegram defaults."
                ),
                ModuleToggle(
                    title = "Hide app update prompts",
                    key = PreferenceKeys.HIDE_APP_UPDATES,
                    description = "Suppresses Telegram's in-app update availability, banners, and forced update screens."
                )
            )
        ),
        ToggleSection(
            title = "Testing",
            toggles = listOf(
                ModuleToggle(
                    title = "Force local Premium",
                    key = PreferenceKeys.FORCE_LOCAL_PREMIUM,
                    description = "Bypasses local Premium checks only (server checks still apply)."
                ),
                ModuleToggle(
                    title = "Keep all deleted messages",
                    key = PreferenceKeys.KEEP_DELETED_MESSAGES,
                    description = "Forces Telegram DB to retain deleted messages. Highly unstable."
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
                    description = "Enable additional Xposed and Android log output."
                )
            )
        )
    )
}
