package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

fun Hooks.killStories() {
    log("Disabling stories...")

    val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
    val storiesControllerClass = loadClass("org.telegram.ui.Stories.StoriesController") ?: return

    XposedBridge.hookAllMethods(
        storiesControllerClass,
        "hasStories",
        XC_MethodReplacement.returnConstant(false)
    )
    XposedBridge.hookAllMethods(
        messagesControllerClass,
        "storiesEnabled",
        XC_MethodReplacement.returnConstant(false)
    )

    // Doubt that those do anything, but they'll stay for now
    XposedBridge.hookAllMethods(
        storiesControllerClass,
        "loadStories",
        XC_MethodReplacement.returnConstant(null)
    )
    XposedBridge.hookAllMethods(
        storiesControllerClass,
        "loadHiddenStories",
        XC_MethodReplacement.returnConstant(null)
    )
}