package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.defaultHdMediaSending() {
    log("Enabling default HD media sending...")

    val photoLayoutClass = loadClass("org.telegram.ui.Components.ChatAttachAlertPhotoLayout") ?: return

    XposedBridge.hookAllMethods(
        photoLayoutClass,
        "addToSelectedPhotos",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val photoEntry = param.args.getOrNull(0) ?: return

                val isVideo = runCatching {
                    XposedHelpers.getBooleanField(photoEntry, "isVideo")
                }.getOrDefault(false)
                if (isVideo) {
                    return
                }

                // Only set the initial state. The user can still switch back to SD manually.
                runCatching {
                    XposedHelpers.setBooleanField(photoEntry, "highQuality", true)
                }
            }
        }
    )
}
