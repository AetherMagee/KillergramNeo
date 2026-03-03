package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.view.View
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.hideDialogsFloatingButton() {
    log("Hiding dialogs floating action button...")

    val dialogsActivityClass = loadClass("org.telegram.ui.DialogsActivity") ?: return

    val enforceHiddenStateHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            forceHideDialogsFloatingButtons(param.thisObject)
        }
    }

    XposedBridge.hookAllMethods(dialogsActivityClass, "createView", enforceHiddenStateHook)
    XposedBridge.hookAllMethods(dialogsActivityClass, "updateFloatingButtonVisibility", enforceHiddenStateHook)
    XposedBridge.hookAllMethods(dialogsActivityClass, "updateFloatingButtonOffset", enforceHiddenStateHook)
    XposedBridge.hookAllMethods(dialogsActivityClass, "updateStoriesPosting", enforceHiddenStateHook)
    XposedBridge.hookAllMethods(dialogsActivityClass, "hideFloatingButton", enforceHiddenStateHook)
    XposedBridge.hookAllMethods(dialogsActivityClass, "checkUi_floatingButton", enforceHiddenStateHook)
}

private fun forceHideDialogsFloatingButtons(dialogsActivity: Any?) {
    if (dialogsActivity == null) {
        return
    }
    if (!isDefaultDialogsScreen(dialogsActivity)) {
        return
    }

    // Telegram 12.4+
    hideFloatingButtonField(dialogsActivity, "floatingButton3")
    hideFloatingButtonField(dialogsActivity, "floatingButtonStories")

    // Telegram 12.1.x and earlier branch
    hideFloatingButtonField(dialogsActivity, "floatingButtonContainer")
    hideFloatingButtonField(dialogsActivity, "floatingButton2Container")
    hideFloatingButtonField(dialogsActivity, "floatingButton")
    hideFloatingButtonField(dialogsActivity, "floatingButton2")

    runCatching {
        val storyHint = XposedHelpers.getObjectField(dialogsActivity, "storyHint") as? View
        storyHint?.visibility = View.GONE
        storyHint?.alpha = 0f
    }
    runCatching {
        val storyPremiumHint = XposedHelpers.getObjectField(dialogsActivity, "storyPremiumHint") as? View
        storyPremiumHint?.visibility = View.GONE
        storyPremiumHint?.alpha = 0f
    }
}

private fun isDefaultDialogsScreen(dialogsActivity: Any): Boolean {
    val dialogsTypeDefault = runCatching {
        XposedHelpers.getStaticIntField(dialogsActivity.javaClass, "DIALOGS_TYPE_DEFAULT")
    }.getOrDefault(0)

    val initialDialogsType = runCatching {
        XposedHelpers.getIntField(dialogsActivity, "initialDialogsType")
    }.getOrNull() ?: return false

    return initialDialogsType == dialogsTypeDefault
}

private fun hideFloatingButtonField(instance: Any, fieldName: String) {
    val fieldValue = runCatching {
        XposedHelpers.getObjectField(instance, fieldName)
    }.getOrNull() ?: return

    runCatching {
        XposedHelpers.callMethod(fieldValue, "setButtonVisible", false, false)
    }

    val view = fieldValue as? View ?: return
    view.visibility = View.GONE
    view.alpha = 0f
    view.isClickable = false
    view.isEnabled = false
}
