package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

fun Hooks.forceAllowForwards() {
    log("Enabling forwarding anywhere...")

    val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
    val chatUIActivityClass = loadClass("org.telegram.ui.ChatActivity") ?: return
    val messageObjectClass = loadClass("org.telegram.messenger.MessageObject") ?: return

    XposedBridge.hookAllMethods(
        messagesControllerClass,
        "isChatNoForwards",
        XC_MethodReplacement.returnConstant(false)
    )
    XposedBridge.hookAllMethods(
        messageObjectClass,
        "canForwardMessage",
        XC_MethodReplacement.returnConstant(true)
    )
    XposedBridge.hookAllMethods(
        chatUIActivityClass,
        "hasSelectedNoforwardsMessage",
        XC_MethodReplacement.returnConstant(false)
    )
}