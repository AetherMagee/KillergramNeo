package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

fun Hooks.localPremium() {
    log("Spoofing local premium...")

    val userConfigClass = loadClass("org.telegram.messenger.UserConfig") ?: return

    XposedBridge.hookAllMethods(
        userConfigClass,
        "hasPremiumOnAccounts",
        XC_MethodReplacement.returnConstant(true)
    )
    XposedBridge.hookAllMethods(
        userConfigClass,
        "isPremium",
        XC_MethodReplacement.returnConstant(true)
    )
}