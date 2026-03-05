package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.hidePostShareButton() {
    log("Hiding share button for channel posts and reposts in groups...")

    val chatMessageCellClass = loadClass("org.telegram.ui.Cells.ChatMessageCell") ?: return

    XposedBridge.hookAllMethods(
        chatMessageCellClass,
        "checkNeedDrawShareButton",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val shouldDraw = (param.result as? Boolean) ?: return
                if (!shouldDraw) {
                    return
                }

                val messageObject = param.args.getOrNull(0) ?: return
                if (shouldHideShareForMessage(messageObject)) {
                    param.result = false
                }
            }
        }
    )
}

private fun shouldHideShareForMessage(messageObject: Any): Boolean {
    val messageOwner = runCatching {
        XposedHelpers.getObjectField(messageObject, "messageOwner")
    }.getOrNull() ?: return false

    val fromId = runCatching {
        XposedHelpers.getObjectField(messageOwner, "from_id")
    }.getOrNull()
    val isChannelPost = runCatching {
        XposedHelpers.getBooleanField(messageOwner, "post")
    }.getOrDefault(false) || isPeerChannel(fromId)

    val forwardHeader = runCatching {
        XposedHelpers.getObjectField(messageOwner, "fwd_from")
    }.getOrNull()
    val forwardFromId = runCatching {
        forwardHeader?.let { XposedHelpers.getObjectField(it, "from_id") }
    }.getOrNull()
    val isChannelRepost = isPeerChannel(forwardFromId)
    val isGroupChat = runCatching {
        XposedHelpers.callMethod(messageObject, "isSupergroup") as? Boolean
    }.getOrNull() == true

    return isChannelPost || (isChannelRepost && isGroupChat)
}

private fun isPeerChannel(peer: Any?): Boolean {
    return peer?.javaClass?.name?.endsWith("TL_peerChannel") == true
}
