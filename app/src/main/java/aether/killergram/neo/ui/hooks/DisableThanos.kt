package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

fun Hooks.disableThanosEffect() {
    log("Disabling Thanos effect...")

    val chatUIActivityClass = loadClass("org.telegram.ui.ChatActivity") ?: return
    val thanosEffectClass = loadClass("org.telegram.ui.Components.ThanosEffect") ?: return

    XposedBridge.hookAllMethods(
        chatUIActivityClass,
        "supportsThanosEffect",
        XC_MethodReplacement.returnConstant(false)
    )
    XposedBridge.hookAllMethods(
        thanosEffectClass,
        "supports",
        XC_MethodReplacement.returnConstant(false)
    )
}