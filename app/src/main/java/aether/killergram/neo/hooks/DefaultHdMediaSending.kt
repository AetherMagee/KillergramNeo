package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView
import android.widget.TextView
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

private fun applyDefaultHdToSendingMedia(mediaArg: Any?, forceDocumentArg: Any?) {
    val forceDocument = forceDocumentArg as? Boolean ?: return
    if (forceDocument) {
        return
    }

    val media = mediaArg as? List<*> ?: return
    media.forEach { info ->
        if (info == null) {
            return@forEach
        }

        val isVideo = runCatching {
            XposedHelpers.getBooleanField(info, "isVideo")
        }.getOrDefault(true)
        if (isVideo) {
            return@forEach
        }

        runCatching {
            XposedHelpers.setBooleanField(info, "highQuality", true)
        }.onFailure {
            log("Failed to set HD on SendingMediaInfo: ${it.message}", "DEBUG")
        }
    }
}

private fun updateBadgeContrast(cell: Any, themeClass: Class<*>) {
    val photoEntry = runCatching {
        XposedHelpers.getObjectField(cell, "photoEntry")
    }.getOrNull() ?: return

    val isVideo = runCatching {
        XposedHelpers.getBooleanField(photoEntry, "isVideo")
    }.getOrDefault(true)
    val highQuality = runCatching {
        XposedHelpers.getBooleanField(photoEntry, "highQuality")
    }.getOrDefault(false)

    val textView = runCatching {
        XposedHelpers.getObjectField(cell, "videoTextView") as? TextView
    }.getOrNull() ?: return
    val playView = runCatching {
        XposedHelpers.getObjectField(cell, "videoPlayImageView") as? ImageView
    }.getOrNull()

    val backgroundColor = runCatching {
        val paint = XposedHelpers.getStaticObjectField(themeClass, "chat_timeBackgroundPaint") as? Paint
        paint?.color
    }.getOrNull() ?: Color.BLACK

    val luminance = (
        0.299 * Color.red(backgroundColor) +
        0.587 * Color.green(backgroundColor) +
        0.114 * Color.blue(backgroundColor)
    ) / 255.0
    val foregroundColor = if (luminance > 0.72) Color.BLACK else Color.WHITE

    textView.setTextColor(foregroundColor)
    if (!isVideo && highQuality) {
        playView?.clearColorFilter()
    } else {
        playView?.setColorFilter(foregroundColor)
    }
}

fun Hooks.defaultHdMediaSending() {
    log("Enabling default HD media sending...")

    val themeClass = loadClass("org.telegram.ui.ActionBar.Theme")

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

    loadClass("org.telegram.messenger.SendMessagesHelper")?.let { sendMessagesHelperClass ->
        XposedBridge.hookAllMethods(
            sendMessagesHelperClass,
            "prepareSendingMedia",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    applyDefaultHdToSendingMedia(
                        param.args.getOrNull(1),
                        param.args.getOrNull(7)
                    )
                }
            }
        )
    }

    if (themeClass != null) {
        loadClass("org.telegram.ui.Cells.PhotoAttachPhotoCell")?.let { photoAttachCellClass ->
            listOf("setHighQuality", "setPhotoEntry").forEach { methodName ->
                XposedBridge.hookAllMethods(
                    photoAttachCellClass,
                    methodName,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            updateBadgeContrast(param.thisObject, themeClass)
                        }
                    }
                )
            }
        }
    }
}
