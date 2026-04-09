package aether.killergram.neo.hooks

import aether.killergram.neo.PreferencesUtils
import aether.killergram.neo.core.InputBoxButtons
import aether.killergram.neo.core.PreferenceKeys
import aether.killergram.neo.log
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import kotlin.math.max

private const val INPUT_BUTTON_BOT_MENU = "bot_menu"
private const val INPUT_BUTTON_BOT_KEYBOARD = "bot_keyboard"
private const val INPUT_BUTTON_GIFT = "gift"
private const val INPUT_BUTTON_SCHEDULED = "scheduled"
private const val INPUT_BUTTON_SILENT_NOTIFY = "silent_notify"
private const val INPUT_BUTTON_SUGGEST_POST = "suggest_post"

fun Hooks.hideInputBoxButtons() {
    val hiddenButtons = PreferencesUtils()
        .getPrefsInstance()
        .getStringSet(PreferenceKeys.HIDDEN_INPUT_BOX_BUTTONS, emptySet())
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.toSet()
        .orEmpty()

    if (hiddenButtons.isEmpty()) {
        log("Input box customization enabled without hidden buttons; skipping hook.", "DEBUG")
        return
    }

    val hiddenTitles = hiddenButtons.map { InputBoxButtons.byValue[it]?.fallbackTitle ?: it }
    log("Customizing input box buttons: ${hiddenTitles.joinToString()}", "DEBUG")

    val chatActivityEnterViewClass = loadClass("org.telegram.ui.Components.ChatActivityEnterView") ?: return
    val androidUtilitiesClass = loadClass("org.telegram.messenger.AndroidUtilities")

    val applyBeforeLayoutHook = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            applyHiddenInputBoxButtons(param.thisObject, hiddenButtons, androidUtilitiesClass)
        }
    }

    val applyAfterStateChangeHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            applyHiddenInputBoxButtons(param.thisObject, hiddenButtons, androidUtilitiesClass)
        }
    }

    XposedBridge.hookAllMethods(chatActivityEnterViewClass, "onMeasure", applyBeforeLayoutHook)
    XposedBridge.hookAllMethods(chatActivityEnterViewClass, "updateFieldRight", applyBeforeLayoutHook)

    XposedBridge.hookAllMethods(chatActivityEnterViewClass, "checkSendButton", applyAfterStateChangeHook)
    XposedBridge.hookAllMethods(chatActivityEnterViewClass, "updateBotButton", applyAfterStateChangeHook)
    XposedBridge.hookAllMethods(chatActivityEnterViewClass, "updateBotWebView", applyAfterStateChangeHook)
    XposedBridge.hookAllMethods(chatActivityEnterViewClass, "updateGiftButton", applyAfterStateChangeHook)
    XposedBridge.hookAllMethods(chatActivityEnterViewClass, "updateScheduleButton", applyAfterStateChangeHook)
}

private fun applyHiddenInputBoxButtons(
    chatActivityEnterView: Any?,
    hiddenButtons: Set<String>,
    androidUtilitiesClass: Class<*>?
) {
    if (chatActivityEnterView == null) {
        return
    }

    if (INPUT_BUTTON_BOT_MENU in hiddenButtons) {
        runCatching {
            XposedHelpers.callMethod(chatActivityEnterView, "hideBotCommands")
        }
        hideViewField(chatActivityEnterView, "botCommandsMenuButton", clearTag = true)
        hideViewField(chatActivityEnterView, "botWebViewButton")
    }

    if (INPUT_BUTTON_BOT_KEYBOARD in hiddenButtons) {
        hideViewField(chatActivityEnterView, "botButton")
    }

    if (INPUT_BUTTON_GIFT in hiddenButtons) {
        hideViewField(chatActivityEnterView, "giftButton")
    }

    if (INPUT_BUTTON_SCHEDULED in hiddenButtons) {
        hideViewField(chatActivityEnterView, "scheduledButton", clearTag = true)
    }

    if (INPUT_BUTTON_SILENT_NOTIFY in hiddenButtons) {
        hideViewField(chatActivityEnterView, "notifyButton")
    }

    if (INPUT_BUTTON_SUGGEST_POST in hiddenButtons) {
        hideViewField(chatActivityEnterView, "suggestButton")
    }

    if (INPUT_BUTTON_GIFT in hiddenButtons || INPUT_BUTTON_SUGGEST_POST in hiddenButtons) {
        runCatching {
            XposedHelpers.callMethod(chatActivityEnterView, "hideHints")
        }
    }

    updateComposerLeftMargins(chatActivityEnterView, androidUtilitiesClass)
    updateComposerRightMargin(chatActivityEnterView, androidUtilitiesClass)
    refreshScheduledButtonTranslation(chatActivityEnterView)
}

private fun updateComposerLeftMargins(
    chatActivityEnterView: Any,
    androidUtilitiesClass: Class<*>?
) {
    val emojiButton = getViewField(chatActivityEnterView, "emojiButton") ?: return
    val messageEditText = getViewField(chatActivityEnterView, "messageEditText")
    val senderSelectView = getViewField(chatActivityEnterView, "senderSelectView")
    val botCommandsMenuButton = getViewField(chatActivityEnterView, "botCommandsMenuButton")

    val emojiParams = emojiButton.layoutParams as? MarginLayoutParams ?: return
    val messageParams = messageEditText?.layoutParams as? MarginLayoutParams

    when {
        botCommandsMenuButton != null &&
            botCommandsMenuButton.tag != null &&
            botCommandsMenuButton.visibility == View.VISIBLE -> {
            val width = botCommandsMenuButton.measuredWidth
                .takeIf { it > 0 }
                ?: botCommandsMenuButton.layoutParams?.width
                ?: 0
            emojiParams.leftMargin = dp(androidUtilitiesClass, 10f) + width
            messageParams?.leftMargin = dp(androidUtilitiesClass, 57f) + width
        }

        senderSelectView?.visibility == View.VISIBLE -> {
            val width = senderSelectView.layoutParams?.width
                ?.takeIf { it > 0 }
                ?: senderSelectView.measuredWidth
            emojiParams.leftMargin = dp(androidUtilitiesClass, 16f) + width
            messageParams?.leftMargin = dp(androidUtilitiesClass, 63f) + width
        }

        else -> {
            emojiParams.leftMargin = dp(androidUtilitiesClass, 3f)
            messageParams?.leftMargin = dp(androidUtilitiesClass, 50f)
        }
    }
}

private fun updateComposerRightMargin(
    chatActivityEnterView: Any,
    androidUtilitiesClass: Class<*>?
) {
    val messageEditText = getViewField(chatActivityEnterView, "messageEditText") ?: return

    val editingMessageObject = runCatching {
        XposedHelpers.getObjectField(chatActivityEnterView, "editingMessageObject")
    }.getOrNull()
    if (editingMessageObject != null) {
        val needsResend = runCatching {
            XposedHelpers.callMethod(editingMessageObject, "needResendWhenEdit") as? Boolean
        }.getOrNull()
        if (needsResend == false) {
            return
        }
    }

    val params = messageEditText.layoutParams as? MarginLayoutParams ?: return
    val sendButton = getViewField(chatActivityEnterView, "sendButton")
    val doneButton = getViewField(chatActivityEnterView, "doneButton")
    val botButton = getViewField(chatActivityEnterView, "botButton")
    val notifyButton = getViewField(chatActivityEnterView, "notifyButton")
    val scheduledButton = getViewField(chatActivityEnterView, "scheduledButton")
    val attachButton = getViewField(chatActivityEnterView, "attachButton")
    val suggestButton = getViewField(chatActivityEnterView, "suggestButton")

    val isStories = runCatching {
        XposedHelpers.getBooleanField(chatActivityEnterView, "isStories")
    }.getOrDefault(false)
    val isLiveComment = runCatching {
        XposedHelpers.getBooleanField(chatActivityEnterView, "isLiveComment")
    }.getOrDefault(false)
    val suggestButtonVisible = runCatching {
        XposedHelpers.getBooleanField(chatActivityEnterView, "suggestButtonVisible")
    }.getOrDefault(false)
    val lastAttachVisible = runCatching {
        XposedHelpers.getIntField(chatActivityEnterView, "lastAttachVisible")
    }.getOrDefault(0)

    val defaultHeight = runCatching {
        XposedHelpers.getStaticIntField(chatActivityEnterView.javaClass, "DEFAULT_HEIGHT")
    }.getOrDefault(44)

    var rightMargin = when {
        isStories && isLiveComment -> dp(
            androidUtilitiesClass,
            if (suggestButtonVisible && isVisible(suggestButton)) 50f else 2f
        )

        lastAttachVisible == 1 || lastAttachVisible == 2 -> when {
            isVisible(botButton) && isScheduledShown(scheduledButton) && isVisible(attachButton) -> dp(androidUtilitiesClass, 146f)
            isVisible(botButton) || isVisible(notifyButton) || isScheduledShown(scheduledButton) -> dp(androidUtilitiesClass, 98f)
            else -> dp(androidUtilitiesClass, 50f)
        }

        isScheduledShown(scheduledButton) -> dp(androidUtilitiesClass, 50f)
        else -> dp(androidUtilitiesClass, 2f)
    }

    val sendButtonExtraWidth = max(0, (sendButton?.width ?: 0) - dp(androidUtilitiesClass, defaultHeight.toFloat()))
    rightMargin = max(rightMargin, sendButtonExtraWidth)

    if (doneButton?.visibility == View.VISIBLE) {
        rightMargin = max(rightMargin, max(0, doneButton.width - dp(androidUtilitiesClass, defaultHeight.toFloat())))
    }

    if (params.rightMargin != rightMargin) {
        params.rightMargin = rightMargin
        messageEditText.layoutParams = params
    }
}

private fun refreshScheduledButtonTranslation(chatActivityEnterView: Any) {
    val scheduledButton = getViewField(chatActivityEnterView, "scheduledButton") ?: return
    scheduledButton.translationX = scheduledButton.translationX
}

private fun hideViewField(
    instance: Any,
    fieldName: String,
    clearTag: Boolean = false
) {
    runCatching {
        val view = XposedHelpers.getObjectField(instance, fieldName) as? View ?: return
        view.visibility = View.GONE
        view.alpha = 0f
        view.scaleX = 0f
        view.scaleY = 0f
        if (clearTag) {
            view.tag = null
        }
    }.onFailure {
        if (it.message?.contains("NoSuchFieldError") != true &&
            it.message?.contains("NoSuchFieldException") != true) {
            log("Failed to hide $fieldName: ${it.message}", "DEBUG")
        }
    }
}

private fun getViewField(instance: Any, fieldName: String): View? {
    return runCatching {
        XposedHelpers.getObjectField(instance, fieldName) as? View
    }.getOrNull()
}

private fun isVisible(view: View?): Boolean {
    return view?.visibility == View.VISIBLE
}

private fun isScheduledShown(view: View?): Boolean {
    return view?.visibility == View.VISIBLE && view.tag != null
}

private fun dp(androidUtilitiesClass: Class<*>?, value: Float): Int {
    if (androidUtilitiesClass == null) {
        return value.toInt()
    }

    return runCatching {
        XposedHelpers.callStaticMethod(androidUtilitiesClass, "dp", value) as? Int
    }.getOrNull() ?: value.toInt()
}
