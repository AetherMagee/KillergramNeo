package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

private fun applyDefaultHd(mediaEntry: Any?) {
    if (mediaEntry == null) {
        return
    }

    val isVideo = runCatching {
        XposedHelpers.getBooleanField(mediaEntry, "isVideo")
    }.getOrDefault(true)
    if (isVideo) {
        return
    }

    runCatching {
        XposedHelpers.setBooleanField(mediaEntry, "highQuality", true)
    }.onFailure {
        log("Failed to set default HD media state: ${it.message}", "DEBUG")
    }
}

private fun getPhotoViewerEntry(photoViewer: Any, indexArg: Any?): Any? {
    val index = indexArg as? Int ?: return null
    val localImages = runCatching {
        XposedHelpers.getObjectField(photoViewer, "imagesArrLocals") as? List<*>
    }.getOrNull() ?: return null

    if (index !in localImages.indices) {
        return null
    }

    return localImages[index]
}

fun Hooks.defaultHdMediaSending() {
    log("Enabling default HD media sending...")

    loadClass("org.telegram.messenger.MediaController\$PhotoEntry")?.let { photoEntryClass ->
        XposedBridge.hookAllConstructors(
            photoEntryClass,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    applyDefaultHd(param.thisObject)
                }
            }
        )

        XposedBridge.hookAllMethods(
            photoEntryClass,
            "reset",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    applyDefaultHd(param.thisObject)
                }
            }
        )
    }

    loadClass("org.telegram.ui.PhotoViewer")?.let { photoViewerClass ->
        XposedBridge.hookAllMethods(
            photoViewerClass,
            "setIsAboutToSwitchToIndex",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    applyDefaultHd(getPhotoViewerEntry(param.thisObject, param.args.getOrNull(0)))
                }
            }
        )
    }

    loadClass("org.telegram.ui.Components.ChatAttachAlertPhotoLayout")?.let { photoLayoutClass ->
        XposedBridge.hookAllMethods(
            photoLayoutClass,
            "addToSelectedPhotos",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    applyDefaultHd(param.args.getOrNull(0))
                }
            }
        )
    }

    loadClass("org.telegram.ui.Cells.PhotoAttachPhotoCell")?.let { photoAttachCellClass ->
        XposedBridge.hookAllMethods(
            photoAttachCellClass,
            "setHighQuality",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val requestedState = param.args.getOrNull(0) as? Boolean ?: return
                    if (!requestedState) {
                        return
                    }

                    val photoEntry = runCatching {
                        XposedHelpers.getObjectField(param.thisObject, "photoEntry")
                    }.getOrNull() ?: return

                    val isVideo = runCatching {
                        XposedHelpers.getBooleanField(photoEntry, "isVideo")
                    }.getOrDefault(true)
                    if (isVideo) {
                        return
                    }

                    param.args[0] = false
                }
            }
        )
    }
}
