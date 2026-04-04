package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

fun Hooks.killStories() {
    log("Disabling stories...")

    val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
    val storiesControllerClass = loadClass("org.telegram.ui.Stories.StoriesController") ?: return

    // Gate: always report no stories anywhere
    XposedBridge.hookAllMethods(
        storiesControllerClass,
        "hasStories",
        XC_MethodReplacement.returnConstant(false)
    )
    XposedBridge.hookAllMethods(
        storiesControllerClass,
        "hasSelfStories",
        XC_MethodReplacement.returnConstant(false)
    )
    XposedBridge.hookAllMethods(
        storiesControllerClass,
        "hasHiddenStories",
        XC_MethodReplacement.returnConstant(false)
    )
    XposedBridge.hookAllMethods(
        messagesControllerClass,
        "storiesEnabled",
        XC_MethodReplacement.returnConstant(false)
    )

    // Block initial loading
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

    // Block live story updates — prevents stories from appearing when posted while the app is open
    XposedBridge.hookAllMethods(
        storiesControllerClass,
        "processUpdate",
        XC_MethodReplacement.returnConstant(null)
    )
}