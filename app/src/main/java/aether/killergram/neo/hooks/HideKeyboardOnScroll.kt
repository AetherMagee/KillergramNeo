package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

private val hookedScrollListenerClasses = HashSet<String>()

fun Hooks.hideKeyboardOnScroll() {
    log("Enabling keyboard hide on chat scroll...")

    val recyclerListViewClass = loadClass("org.telegram.ui.Components.RecyclerListView") ?: return
    val androidUtilitiesClass = loadClass("org.telegram.messenger.AndroidUtilities")

    XposedBridge.hookAllMethods(
        recyclerListViewClass,
        "setOnScrollListener",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val listener = param.args.getOrNull(0) ?: return
                if (!isCalledFromChatActivity()) {
                    return
                }
                hookListenerOnScrollStateChanged(listener.javaClass, androidUtilitiesClass)
            }
        }
    )
}

private fun hookListenerOnScrollStateChanged(listenerClass: Class<*>, androidUtilitiesClass: Class<*>?) {
    val className = listenerClass.name
    synchronized(hookedScrollListenerClasses) {
        if (!hookedScrollListenerClasses.add(className)) {
            return
        }
    }

    XposedBridge.hookAllMethods(
        listenerClass,
        "onScrollStateChanged",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val newState = param.args.getOrNull(1) as? Int ?: return
                if (newState != 1) { // Only act on SCROLL_STATE_DRAGGING (user touch)
                    return
                }

                closeActivePicker(param.thisObject)

                val recyclerView = param.args.getOrNull(0) as? View ?: return
                hideKeyboard(recyclerView, androidUtilitiesClass)
            }
        }
    )
}

private fun closeActivePicker(scrollListener: Any?) {
    val listener = scrollListener ?: return
    val chatActivity = findChatActivity(listener) ?: return
    val enterView = runCatching {
        XposedHelpers.getObjectField(chatActivity, "chatActivityEnterView")
    }.getOrNull() ?: return

    val popupShowing = runCatching {
        XposedHelpers.callMethod(enterView, "isPopupShowing") as? Boolean
    }.getOrDefault(false)
    if (!popupShowing!!) {
        return
    }

    val hidden = runCatching {
        XposedHelpers.callMethod(enterView, "hidePopup", false)
    }.recoverCatching {
        XposedHelpers.callMethod(enterView, "hidePopup", false, false)
    }.isSuccess

    if (!hidden) {
        log("Failed to hide chat popup on scroll", "DEBUG")
    }
}

private fun findChatActivity(listener: Any): Any? {
    val direct = runCatching {
        XposedHelpers.getObjectField(listener, "this$0")
    }.getOrNull()
    if (direct?.javaClass?.name == "org.telegram.ui.ChatActivity") {
        return direct
    }

    return runCatching {
        listener.javaClass.declaredFields.firstNotNullOfOrNull { field ->
            if (field.type.name != "org.telegram.ui.ChatActivity") {
                return@firstNotNullOfOrNull null
            }
            field.isAccessible = true
            field.get(listener)
        }
    }.getOrNull()
}

private fun hideKeyboard(view: View, androidUtilitiesClass: Class<*>?) {
    val target = view.rootView?.findFocus() ?: view.findFocus() ?: view

    if (androidUtilitiesClass != null) {
        val hidden = runCatching {
            XposedHelpers.callStaticMethod(androidUtilitiesClass, "hideKeyboard", target)
        }.isSuccess
        if (hidden) {
            return
        }
    }

    runCatching {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(target.windowToken, 0)
    }
}

private fun isCalledFromChatActivity(): Boolean {
    return Throwable().stackTrace.any { it.className == "org.telegram.ui.ChatActivity" }
}
