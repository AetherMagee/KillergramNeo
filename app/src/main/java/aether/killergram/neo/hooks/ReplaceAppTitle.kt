package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.view.View
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.Collections
import java.util.WeakHashMap

fun Hooks.replaceAppTitle(mode: String, customText: String, centerTitle: Boolean) {
    log("Setting up app title replacement (mode=$mode, center=$centerTitle)...")

    val dialogsActivityClass = loadClass("org.telegram.ui.DialogsActivity") ?: return
    val actionBarClass = loadClass("org.telegram.ui.ActionBar.ActionBar") ?: return
    val trackedBars: MutableSet<Any> = Collections.newSetFromMap(WeakHashMap())
    var cachedTitle: String? = null

    XposedBridge.hookAllMethods(dialogsActivityClass, "createView", object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            val actionBar = runCatching {
                XposedHelpers.getObjectField(param.thisObject, "actionBar")
            }.getOrNull()
            if (actionBar == null) {
                log("replaceAppTitle: actionBar field not found", "ERROR")
                return
            }

            trackedBars.add(actionBar)
            cachedTitle = resolveTitle(mode, customText, dialogsActivityClass.classLoader)
            applyTitle(actionBar, cachedTitle)
        }
    })

    XposedBridge.hookAllMethods(actionBarClass, "setTitle", object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            if (param.thisObject !in trackedBars) return
            val incoming = param.args.getOrNull(0)?.toString() ?: return
            val replacement = cachedTitle ?: return
            if (incoming == replacement) return
            param.args[0] = replacement
        }
    })

    if (centerTitle) {
        XposedBridge.hookAllMethods(actionBarClass, "onLayout", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.thisObject !in trackedBars) return
                val barWidth = (param.thisObject as View).width
                if (barWidth <= 0) return
                centerWithTranslation(param.thisObject, "titleTextView", 0, barWidth)
                centerWithTranslation(param.thisObject, "titleTextView", 1, barWidth)
                centerFieldWithTranslation(param.thisObject, "subtitleTextView", barWidth)
            }
        })
    }
}

private fun applyTitle(actionBar: Any, replacement: String?) {
    if (replacement == null) {
        log("replaceAppTitle: resolveTitle returned null", "ERROR")
        return
    }

    log("replaceAppTitle: applying title '$replacement'")

    runCatching {
        val titleTextViewArray = XposedHelpers.getObjectField(actionBar, "titleTextView")
        val tv = java.lang.reflect.Array.get(titleTextViewArray, 0)
        if (tv != null) {
            XposedHelpers.callMethod(tv, "setText", replacement as CharSequence)
        }
    }.onFailure {
        log("replaceAppTitle: failed to set text on titleTextView: ${it.message}", "ERROR")
    }

    runCatching {
        XposedHelpers.setObjectField(actionBar, "lastTitle", replacement as CharSequence)
    }.onFailure {
        log("replaceAppTitle: failed to set lastTitle: ${it.message}", "ERROR")
    }
}

private fun resolveTitle(mode: String, customText: String, classLoader: ClassLoader?): String? {
    return when (mode) {
        "custom" -> customText.ifBlank { null }
        else -> runCatching {
            val userConfigClass = XposedHelpers.findClass(
                "org.telegram.messenger.UserConfig", classLoader
            )
            val selectedAccount = XposedHelpers.getStaticIntField(userConfigClass, "selectedAccount")
            val instance = XposedHelpers.callStaticMethod(
                userConfigClass, "getInstance", selectedAccount
            )
            val user = XposedHelpers.callMethod(instance, "getCurrentUser")
            XposedHelpers.getObjectField(user, "first_name") as? String
        }.getOrElse {
            log("replaceAppTitle: failed to get first name: ${it.message}", "ERROR")
            null
        }
    }
}

private fun centerWithTranslation(actionBar: Any, fieldName: String, index: Int, barWidth: Int) {
    val array = runCatching {
        XposedHelpers.getObjectField(actionBar, fieldName)
    }.getOrNull() ?: return
    val view = runCatching {
        java.lang.reflect.Array.get(array, index) as? View
    }.getOrNull() ?: return
    applyCenterTranslation(view, barWidth)
}

private fun centerFieldWithTranslation(actionBar: Any, fieldName: String, barWidth: Int) {
    val view = runCatching {
        XposedHelpers.getObjectField(actionBar, fieldName) as? View
    }.getOrNull() ?: return
    applyCenterTranslation(view, barWidth)
}

private fun applyCenterTranslation(view: View, barWidth: Int) {
    if (view.visibility != View.VISIBLE || view.width <= 0) return

    val textWidth = runCatching {
        XposedHelpers.getIntField(view, "textWidth")
    }.getOrDefault(view.width)

    val drawablePadding = runCatching {
        XposedHelpers.getIntField(view, "drawablePadding")
    }.getOrDefault(0)
    val rightDrawable = runCatching {
        XposedHelpers.getObjectField(view, "rightDrawable") as? android.graphics.drawable.Drawable
    }.getOrNull()
    val rightDrawableWidth = if (rightDrawable != null) {
        drawablePadding + rightDrawable.intrinsicWidth
    } else 0

    val totalWidth = textWidth + rightDrawableWidth
    val textCurrentX = view.left
    val textCenteredX = (barWidth - totalWidth) / 2
    view.translationX = (textCenteredX - textCurrentX).toFloat()
}
