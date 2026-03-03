package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

private val hookedAttachCameraDrawClasses = HashSet<String>()
private val drawInDecorationFieldSupport = HashMap<String, Boolean>()

fun Hooks.disableAttachCameraPreview() {
    log("Disabling live camera preview in attach tile...")

    val chatAttachClass = loadClass("org.telegram.ui.Components.ChatAttachAlertPhotoLayout") ?: return
    val cameraViewClass = loadClass("org.telegram.messenger.camera.CameraView")

    if (cameraViewClass != null) {
        runCatching {
            XposedHelpers.findAndHookConstructor(
                cameraViewClass,
                Context::class.java,
                java.lang.Boolean.TYPE,
                java.lang.Boolean.TYPE,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val lazyArg = param.args.getOrNull(2) as? Boolean ?: return
                        if (lazyArg || !isCalledFromAttachShowCamera()) {
                            return
                        }
                        // Keep attach camera lazy so it doesn't initialize until user taps.
                        param.args[2] = true
                    }
                }
            )
        }.onFailure {
            log("Failed to hook CameraView constructor: ${it.message}", "DEBUG")
        }
    }

    XposedBridge.hookAllMethods(
        chatAttachClass,
        "showCamera",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val cameraView = runCatching {
                    XposedHelpers.getObjectField(param.thisObject, "cameraView")
                }.getOrNull() ?: return

                hookAttachCameraDispatchDraw(cameraView.javaClass)
            }
        }
    )
}

private fun hookAttachCameraDispatchDraw(cameraViewClass: Class<*>) {
    val className = cameraViewClass.name
    synchronized(hookedAttachCameraDrawClasses) {
        if (!hookedAttachCameraDrawClasses.add(className)) {
            return
        }
    }

    XposedBridge.hookAllMethods(
        cameraViewClass,
        "dispatchDraw",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val instance = param.thisObject ?: return
                val instanceClass = instance.javaClass

                val hasDrawInDecoration = synchronized(drawInDecorationFieldSupport) {
                    drawInDecorationFieldSupport.getOrPut(instanceClass.name) {
                        runCatching {
                            XposedHelpers.findField(instanceClass, "drawInDecoration")
                        }.isSuccess
                    }
                }

                if (hasDrawInDecoration) {
                    val inDecoration = runCatching {
                        XposedHelpers.getBooleanField(instance, "drawInDecoration")
                    }.getOrDefault(false)
                    if (inDecoration) {
                        param.result = null
                    }
                    return
                }

                if (isAttachDecorationDrawCall()) {
                    param.result = null
                }
            }
        }
    )
}

private fun isCalledFromAttachShowCamera(): Boolean {
    return Throwable().stackTrace.any {
        it.className == "org.telegram.ui.Components.ChatAttachAlertPhotoLayout" &&
            it.methodName == "showCamera"
    }
}

private fun isAttachDecorationDrawCall(): Boolean {
    return Throwable().stackTrace.any {
        it.className.contains("org.telegram.ui.Components.ChatAttachAlertPhotoLayout\$CameraViewItemDecoration")
    }
}

