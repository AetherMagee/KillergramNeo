package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.Locale

fun Hooks.showTimestampSeconds() {
    log("Enabling seconds in timestamps...")

    val localeControllerClass = loadClass("org.telegram.messenger.LocaleController") ?: return
    val fastDateFormatClass = loadClass("org.telegram.messenger.time.FastDateFormat") ?: return

    val formatterHook = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val localeController = param.thisObject ?: return
            val formatter = buildSecondsFormatter(localeControllerClass, fastDateFormatClass, localeController) ?: return
            val fieldName = if (param.method.name == "getFormatterConstDay") "formatterConstDay" else "formatterDay"

            runCatching {
                XposedHelpers.setObjectField(localeController, fieldName, formatter)
            }
            param.result = formatter
        }
    }

    XposedBridge.hookAllMethods(localeControllerClass, "getFormatterDay", formatterHook)
    XposedBridge.hookAllMethods(localeControllerClass, "getFormatterConstDay", formatterHook)
}

private fun buildSecondsFormatter(localeControllerClass: Class<*>, fastDateFormatClass: Class<*>, localeController: Any): Any? {
    val is24HourFormat = runCatching {
        XposedHelpers.getStaticBooleanField(localeControllerClass, "is24HourFormat")
    }.getOrDefault(true)

    val locale = runCatching {
        XposedHelpers.getObjectField(localeController, "currentLocale") as? Locale
    }.getOrNull() ?: Locale.getDefault()

    val lang = locale.language.lowercase(Locale.ROOT)
    val formatterLocale = if (lang == "ar" || lang == "ko") locale else Locale.US
    val pattern = if (is24HourFormat) "HH:mm:ss" else "h:mm:ss a"

    return runCatching {
        XposedHelpers.callStaticMethod(fastDateFormatClass, "getInstance", pattern, formatterLocale)
    }.getOrNull()
}
