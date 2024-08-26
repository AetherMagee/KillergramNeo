package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

fun Hooks.overrideAccountCount() {
    log("Bypassing account limit...")

    val userConfigClass = loadClass("org.telegram.messenger.UserConfig") ?: return

    XposedBridge.hookAllMethods(
        userConfigClass,
        "getMaxAccountCount",
        XC_MethodReplacement.returnConstant(999)
    )
}