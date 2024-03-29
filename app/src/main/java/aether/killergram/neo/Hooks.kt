package aether.killergram.neo

import android.content.res.XResources
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge


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

    fun forceAllowForwards(messagesControllerClass: Class<*>, messageObject: Class<*>) {
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
}