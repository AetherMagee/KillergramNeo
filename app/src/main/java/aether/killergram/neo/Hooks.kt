package aether.killergram.neo

import android.view.KeyEvent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam


class Hooks(private val lpparam: LoadPackageParam) {
    private fun loadClass(className: String): Class<*>? {
        // This function is here purely to log any failed class loads
        val loadedClass = XposedHelpers.findClassIfExists(className, this.lpparam.classLoader)
        if (loadedClass == null) {
            log("Unable to load class $className from ${lpparam.packageName}", "ERROR")
        }
        return loadedClass
    }

    fun localPremium() {
        log("Spoofing local premium...")

        val userConfigClass = loadClass("org.telegram.messenger.UserConfig") ?: return

        XposedBridge.hookAllMethods(
            userConfigClass,
            "hasPremiumOnAccounts",
            XC_MethodReplacement.returnConstant(true)
        )
        XposedBridge.hookAllMethods(
            userConfigClass,
            "isPremium",
            XC_MethodReplacement.returnConstant(true)
        )
    }

    fun killSponsoredMessages() {
        log("Killing sponsored messages...")

        val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
        val chatUIActivityClass = loadClass("org.telegram.ui.ChatActivity") ?: return

        XposedBridge.hookAllMethods(
            messagesControllerClass,
            "getSponsoredMessages",
            XC_MethodReplacement.returnConstant(null)
        )
        XposedBridge.hookAllMethods(
            chatUIActivityClass,
            "addSponsoredMessages",
            XC_MethodReplacement.returnConstant(null)
        )
    }

    fun overrideAccountCount() {
        log("Bypassing account limit...")

        val userConfigClass = loadClass("org.telegram.messenger.UserConfig") ?: return

        XposedBridge.hookAllMethods(
            userConfigClass,
            "getMaxAccountCount",
            XC_MethodReplacement.returnConstant(999)
        )
    }

    fun forceAllowForwards() {
        log("Enabling forwarding anywhere...")

        val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
        val chatUIActivityClass = loadClass("org.telegram.ui.ChatActivity") ?: return
        val messageObjectClass = loadClass("org.telegram.messenger.MessageObject") ?: return

        XposedBridge.hookAllMethods(
            messagesControllerClass,
            "isChatNoForwards",
            XC_MethodReplacement.returnConstant(false)
        )
        XposedBridge.hookAllMethods(
            messageObjectClass,
            "canForwardMessage",
            XC_MethodReplacement.returnConstant(true)
        )
        XposedBridge.hookAllMethods(
            chatUIActivityClass,
            "hasSelectedNoforwardsMessage",
            XC_MethodReplacement.returnConstant(false)
        )
    }

    fun killStories() {
        log("Disabling stories...")

        val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
        val storiesControllerClass = loadClass("org.telegram.ui.Stories.StoriesController") ?: return

        XposedBridge.hookAllMethods(
            storiesControllerClass,
            "hasStories",
            XC_MethodReplacement.returnConstant(false)
        )
        XposedBridge.hookAllMethods(
            messagesControllerClass,
            "storiesEnabled",
            XC_MethodReplacement.returnConstant(false)
        )

        // Doubt that those do anything, but they'll stay for now
        XposedBridge.hookAllMethods(
            storiesControllerClass,
            "loadStories",
            XC_MethodReplacement.returnConstant(null)
        )
        XposedBridge.hookAllMethods(
            storiesControllerClass,
            "loadHiddenStories",
            XC_MethodReplacement.returnConstant(null)
        )
    }

    fun killAutoAudio() {
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

    fun keepDeletedMessages() {
        log("Forcing TG to keep deleted messages...")

        val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return
        val messagesStorageClass = loadClass("org.telegram.messenger.MessagesStorage") ?: return

        XposedBridge.hookAllMethods(
            messagesStorageClass,
            "markMessagesAsDeleted",
            XC_MethodReplacement.returnConstant(null)
        )
        XposedBridge.hookAllMethods(
            messagesStorageClass,
            "markMessagesAsDeletedInternal",
            XC_MethodReplacement.returnConstant(null)
        )
        XposedBridge.hookAllMethods(
            messagesControllerClass,
            "markDialogMessageAsDeleted",
            XC_MethodReplacement.returnConstant(null)
        )
        XposedBridge.hookAllMethods(
            messagesControllerClass,
            "deleteMessages",
            XC_MethodReplacement.returnConstant(null)
        )
    }

    fun disableThanosEffect() {
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
}