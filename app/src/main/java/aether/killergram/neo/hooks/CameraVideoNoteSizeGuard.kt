package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.File

private const val MAX_VIDEO_NOTE_SIZE = 10L * 1024 * 1024 // 10 MB

fun Hooks.videoNoteSizeGuard() {
    log("Enabling video note size guard (10MB)...")

    val sendHelperClass = loadClass("org.telegram.messenger.SendMessagesHelper") ?: return

    runCatching {
        XposedBridge.hookAllMethods(
            sendHelperClass,
            "prepareSendingVideo",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val videoEditedInfo = param.args.getOrNull(2) ?: return

                    val isRound = runCatching {
                        XposedHelpers.getBooleanField(videoEditedInfo, "roundVideo")
                    }.getOrDefault(false)

                    if (!isRound) return

                    val videoPath = param.args.getOrNull(1) as? String ?: return
                    val fileSize = runCatching { File(videoPath).length() }.getOrDefault(0L)

                    if (fileSize > MAX_VIDEO_NOTE_SIZE) {
                        XposedHelpers.setBooleanField(videoEditedInfo, "roundVideo", false)
                        log("Video note ${fileSize / 1024 / 1024}MB exceeds 10MB, sending as regular video")
                    }
                }
            }
        )
    }.onFailure {
        log("Failed to hook SendMessagesHelper.prepareSendingVideo: ${it.message}", "ERROR")
    }
}
