package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.media.MediaRecorder
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.cameraHigherBitrate(roundBitrate: Int) {
    log("Applying higher video bitrate (round: ${roundBitrate}kbps)...")

    runCatching {
        XposedBridge.hookAllMethods(
            MediaRecorder::class.java,
            "setVideoEncodingBitRate",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val original = param.args[0] as? Int ?: return
                    param.args[0] = original * 3
                    log("Video bitrate: $original -> ${param.args[0]}", "DEBUG")
                }
            }
        )
    }.onFailure {
        log("Failed to hook MediaRecorder.setVideoEncodingBitRate: ${it.message}", "ERROR")
    }

    hookMessagesControllerField("roundVideoBitrate", roundBitrate)
}

fun Hooks.cameraHigherResolution(resolution: Int) {
    log("Applying higher video note resolution (${resolution}px)...")
    hookMessagesControllerField("roundVideoSize", resolution)
}

private fun Hooks.hookMessagesControllerField(fieldName: String, value: Int) {
    val mcClass = loadClass("org.telegram.messenger.MessagesController") ?: return

    val hook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            runCatching {
                XposedHelpers.setIntField(param.thisObject, fieldName, value)
                log("Set $fieldName to $value", "DEBUG")
            }.onFailure {
                log("Failed to set $fieldName: ${it.message}", "DEBUG")
            }
        }
    }

    runCatching { XposedBridge.hookAllConstructors(mcClass, hook) }
        .onFailure { log("Failed to hook MessagesController constructor: ${it.message}", "ERROR") }

    for (method in listOf("applyAppConfig", "loadAppConfig", "updateConfig")) {
        runCatching { XposedBridge.hookAllMethods(mcClass, method, hook) }
            .onFailure { log("Failed to hook MessagesController.$method: ${it.message}", "DEBUG") }
    }
}
