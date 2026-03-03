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
                if (newState == 0) { // RecyclerView.SCROLL_STATE_IDLE
                    return
                }

                val recyclerView = param.args.getOrNull(0) as? View ?: return
                hideKeyboard(recyclerView, androidUtilitiesClass)
            }
        }
    )
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
