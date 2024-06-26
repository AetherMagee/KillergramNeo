package aether.killergram.neo

import android.view.KeyEvent
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers


class Hooks {
    fun localPremium(userConfigClass: Class<*>) {
        log("Spoofing local premium...")

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

    fun killSponsoredMessages(messagesControllerClass: Class<*>, chatUIActivityClass: Class<*>) {
        log("Killing sponsored messages...")

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

    fun overrideAccountCount(userConfigClass: Class<*>) {
        log("Bypassing account limit...")

        XposedBridge.hookAllMethods(
            userConfigClass,
            "getMaxAccountCount",
            XC_MethodReplacement.returnConstant(999)
        )
    }

    fun forceAllowForwards(messagesControllerClass: Class<*>, messageObject: Class<*>, chatUIActivityClass: Class<*>) {
        log("Enabling forwarding anywhere...")

        XposedBridge.hookAllMethods(
            messagesControllerClass,
            "isChatNoForwards",
            XC_MethodReplacement.returnConstant(false)
        )
        XposedBridge.hookAllMethods(
            messageObject,
            "canForwardMessage",
            XC_MethodReplacement.returnConstant(true)
        )
        XposedBridge.hookAllMethods(
            chatUIActivityClass,
            "hasSelectedNoforwardsMessage",
            XC_MethodReplacement.returnConstant(false)
        )
    }

    fun killStories(storiesControllerClass: Class<*>) {
        log("Disabling stories...")

        XposedBridge.hookAllMethods(
            storiesControllerClass,
            "hasStories",
            XC_MethodReplacement.returnConstant(false)
        )
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

    fun killAutoAudio(launchActivityClass: Class<*>, photoViewerClass: Class<*>) {
        log("Disabling auto-enable audio on vol+/-...")

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

    // TODO: Create logic for storing messages in our module
    // instead of forcing TG to store them
    // Perhaps a local database?
    fun keepDeletedMessages(messagesStorageClass: Class<*>, messagesControllerClass: Class<*>) {
        log("Forcing TG to keep deleted messages...")

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

    fun disableThanosEffect(chatUIActivityClass: Class<*>, thanosEffectClass: Class<*>) {
        log("Disabling Thanos effect...")

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