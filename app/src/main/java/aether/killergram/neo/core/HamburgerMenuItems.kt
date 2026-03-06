package aether.killergram.neo.core

data class HamburgerMenuItemDefinition(
    val value: String,
    val fallbackTitle: String,
    val resourceNames: List<String>,
    val description: String
)

object HamburgerMenuItems {
    val all = listOf(
        HamburgerMenuItemDefinition(
            value = "my_profile",
            fallbackTitle = "My Profile",
            resourceNames = listOf("MyProfile"),
            description = "Hide the My Profile entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "change_status",
            fallbackTitle = "Change Status",
            resourceNames = listOf("ChangeEmojiStatus", "SetEmojiStatus"),
            description = "Hide the Change Status entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "wallet",
            fallbackTitle = "Wallet",
            resourceNames = listOf("Wallet"),
            description = "Hide the Wallet entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "new_group",
            fallbackTitle = "New Group",
            resourceNames = listOf("NewGroup"),
            description = "Hide the New Group entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "contacts",
            fallbackTitle = "Contacts",
            resourceNames = listOf("Contacts"),
            description = "Hide the Contacts entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "calls",
            fallbackTitle = "Calls",
            resourceNames = listOf("Calls"),
            description = "Hide the Calls entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "saved_messages",
            fallbackTitle = "Saved Messages",
            resourceNames = listOf("SavedMessages"),
            description = "Hide the Saved Messages entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "settings",
            fallbackTitle = "Settings",
            resourceNames = listOf("Settings"),
            description = "Hide the Settings entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "invite_friends",
            fallbackTitle = "Invite Friends",
            resourceNames = listOf("InviteFriends"),
            description = "Hide the Invite Friends entry from the left menu."
        ),
        HamburgerMenuItemDefinition(
            value = "telegram_features",
            fallbackTitle = "Telegram Features",
            resourceNames = listOf("TelegramFeatures"),
            description = "Hide the Telegram Features entry from the left menu."
        )
    )

    val byValue = all.associateBy(HamburgerMenuItemDefinition::value)
}
