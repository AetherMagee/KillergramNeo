package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.cameraDefaultBack() {
    log("Setting default camera to rear...")

    val cameraViewClass = loadClass("org.telegram.messenger.camera.CameraView")
    if (cameraViewClass != null) {
        runCatching {
            XposedBridge.hookAllConstructors(
                cameraViewClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        runCatching {
                            XposedHelpers.setBooleanField(param.thisObject, "isFrontface", false)
                            XposedHelpers.setBooleanField(param.thisObject, "initialFrontface", false)
                        }.onFailure {
                            log("Failed to set CameraView frontface fields: ${it.message}", "DEBUG")
                        }
                    }
                }
            )
        }.onFailure {
            log("Failed to hook CameraView constructors: ${it.message}", "ERROR")
        }
    }

    val instantCameraClass = loadClass("org.telegram.ui.Components.InstantCameraView") ?: return

    // initCamera reads isFrontface to select the camera — override it right before
    runCatching {
        XposedBridge.hookAllMethods(
            instantCameraClass,
            "initCamera",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        XposedHelpers.setBooleanField(param.thisObject, "isFrontface", false)
                    }.onFailure {
                        log("Failed to set InstantCameraView.isFrontface: ${it.message}", "DEBUG")
                    }
                }
            }
        )
    }.onFailure {
        log("Failed to hook InstantCameraView.initCamera: ${it.message}", "ERROR")
    }

    // Camera2 path may bypass initCamera entirely
    runCatching {
        XposedBridge.hookAllMethods(
            instantCameraClass,
            "openCamera2",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    runCatching {
                        XposedHelpers.setBooleanField(param.thisObject, "isFrontface", false)
                    }.onFailure {
                        log("Failed to set InstantCameraView.isFrontface: ${it.message}", "DEBUG")
                    }
                }
            }
        )
    }.onFailure {
        // openCamera2 may not exist on all Telegram versions
    }
}
