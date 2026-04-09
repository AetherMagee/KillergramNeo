package aether.killergram.neo.core

data class InputBoxButtonDefinition(
    val value: String,
    val fallbackTitle: String,
    val description: String
)

object InputBoxButtons {
    val all = listOf(
        InputBoxButtonDefinition(
            value = "bot_menu",
            fallbackTitle = "Bot menu selector",
            description = "Hide the left-side bot menu button used for commands or bot web apps."
        ),
        InputBoxButtonDefinition(
            value = "bot_keyboard",
            fallbackTitle = "Bot keyboard toggle",
            description = "Hide the right-side button that opens bot commands or reply keyboards."
        ),
        InputBoxButtonDefinition(
            value = "gift",
            fallbackTitle = "Gift button",
            description = "Hide the premium and star gift shortcut shown in eligible private chats."
        ),
        InputBoxButtonDefinition(
            value = "scheduled",
            fallbackTitle = "Scheduled messages",
            description = "Hide the scheduled messages shortcut when the chat has pending scheduled messages."
        ),
        InputBoxButtonDefinition(
            value = "silent_notify",
            fallbackTitle = "Silent notify",
            description = "Hide the silent-post toggle shown for channels you can post in."
        ),
        InputBoxButtonDefinition(
            value = "suggest_post",
            fallbackTitle = "Suggest post",
            description = "Hide the post suggestion shortcut shown in supported comment contexts."
        )
    )

    val byValue = all.associateBy(InputBoxButtonDefinition::value)
}
