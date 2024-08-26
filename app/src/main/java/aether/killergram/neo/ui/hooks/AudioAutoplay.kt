package aether.killergram.neo.ui.hooks

import aether.killergram.neo.log
import android.view.KeyEvent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.killAutoAudio() {
    log("Disabling auto-enable audio on vol+/-...")

    val launchActivityClass = loadClass("org.telegram.ui.LaunchActivity") ?: return
    val photoViewerClass = loadClass("org.telegram.ui.PhotoViewer") ?: return

    XposedBridge.hookAllMethods(
        launchActivityClass,
        "dispatchKeyEvent",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.args[0]?.let { event ->
                    if (event is KeyEvent &&
                        event.action == KeyEvent.ACTION_DOWN &&
                        (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {

                        val mainFragmentsStackField = XposedHelpers.findField(param.thisObject.javaClass, "mainFragmentsStack")
                        val mainFragmentsStack = mainFragmentsStackField.get(param.thisObject) as? List<*>
                        val hasInstance = XposedHelpers.callStaticMethod(photoViewerClass, "hasInstance") as Boolean
                        val isVisible = if (hasInstance) {
                            XposedHelpers.callStaticMethod(photoViewerClass, "getInstance").let { instance ->
                                XposedHelpers.callMethod(instance, "isVisible") as Boolean
                            }
                        } else false
                        val repeatCount = event.repeatCount == 0

                        try {
                            if (!mainFragmentsStack.isNullOrEmpty() && (!hasInstance || !isVisible) && repeatCount) {
                                val fragment = mainFragmentsStack.lastOrNull()
                                if (fragment!!::class.java.simpleName == "ChatActivity") {
                                    log("Nullified volume button press")
                                    param.result = false
                                }
                            }
                        } catch (e: Exception) {
                            log("Failed to handle volume button press. Are you on a tablet?", "ERROR")
                        }
                    }
                }
                super.beforeHookedMethod(param)
            }
        }
    )
}