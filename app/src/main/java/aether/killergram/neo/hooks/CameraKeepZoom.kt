package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.cameraKeepZoom() {
    log("Enabling persistent zoom for video notes...")

    val instantCameraClass = loadClass("org.telegram.ui.Components.InstantCameraView") ?: return

    runCatching {
        XposedBridge.hookAllMethods(
            instantCameraClass,
            "finishZoom",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val recording = runCatching {
                        XposedHelpers.getBooleanField(param.thisObject, "recording")
                    }.getOrDefault(false)

                    if (recording) {
                        param.result = null
                        log("Blocked zoom reset during recording", "DEBUG")
                    }
                }
            }
        )
    }.onFailure {
        log("Failed to hook InstantCameraView.finishZoom: ${it.message}", "ERROR")
    }
}
