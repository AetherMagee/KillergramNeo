package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

fun Hooks.killSponsoredMessages() {
    log("Killing sponsored messages...")

    val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
    val chatUIActivityClass = loadClass("org.telegram.ui.ChatActivity") ?: return

    XposedBridge.hookAllMethods(
        messagesControllerClass,
        "getSponsoredMessages",
        XC_MethodReplacement.returnConstant(null)
    )
    XposedBridge.hookAllMethods(
        chatUIActivityClass,
        "addSponsoredMessages",
        XC_MethodReplacement.returnConstant(null)
    )
}