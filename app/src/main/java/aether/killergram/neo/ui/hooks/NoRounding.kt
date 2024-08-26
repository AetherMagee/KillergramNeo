package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.util.Locale

fun Hooks.noRounding() {
    log("Disabling rounding...")

    val localeControllerClass = loadClass("org.telegram.messenger.LocaleController") ?: return

    XposedBridge.hookAllMethods(
        localeControllerClass,
        "formatShortNumber",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                val number: Int = param!!.args[0] as Int
                val formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
                formatter.applyPattern("#,###")
                param.result = formatter.format(number)
            }
        }
    )
}