package aether.killergram.neo

import aether.killergram.neo.core.PreferenceKeys
import aether.killergram.neo.hooks.Hooks
import aether.killergram.neo.hooks.forceAllowForwards
import aether.killergram.neo.hooks.forceSystemTypeface
import aether.killergram.neo.hooks.hideChannelBottomBar
import aether.killergram.neo.hooks.hideAppUpdates
import aether.killergram.neo.hooks.hideDialogsFloatingButton
import aether.killergram.neo.hooks.hideKeyboardOnScroll
import aether.killergram.neo.hooks.keepDeletedMessages
import aether.killergram.neo.hooks.killAutoAudio
import aether.killergram.neo.hooks.killSponsoredMessages
import aether.killergram.neo.hooks.killStories
import aether.killergram.neo.hooks.localPremium
import aether.killergram.neo.hooks.noRounding
import aether.killergram.neo.hooks.overrideAccountCount
import aether.killergram.neo.hooks.defaultHdMediaSending
import aether.killergram.neo.hooks.disableAttachCameraPreview
import aether.killergram.neo.hooks.showTimestampSeconds
import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage


class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private var moduleResources: XModuleResources? = null
    private val prefs = PreferencesUtils().getPrefsInstance()
    private val packageBlocklist = arrayOf(  // Apparently sometimes LSPosed will just hook us into random crap like GMS or ourselves
            "aether.killergram.neo",         // This is supposed to solve this problem
            "com.google.android.gms"
        )
    override fun initZygote(startupParam: StartupParam?) {
        this.moduleResources = XModuleResources.createInstance(startupParam!!.modulePath, null)
    }
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam?.packageName == null || packageBlocklist.contains(lpparam.packageName)) {
            log("Aborting load for ${lpparam?.packageName}", "DEBUG")
            return
        }

        log("Injecting for ${lpparam.packageName}", "DEBUG")
        val hooks = Hooks(lpparam)

        val hooksMap = mapOf(
            PreferenceKeys.FORCE_LOCAL_PREMIUM to { hooks.localPremium() },
            PreferenceKeys.REMOVE_SPONSORED to { hooks.killSponsoredMessages() },
            PreferenceKeys.FORCE_FORWARD to { hooks.forceAllowForwards() },
            PreferenceKeys.OVERRIDE_ACCOUNT_LIMIT to { hooks.overrideAccountCount() },
            PreferenceKeys.HIDE_STORIES to { hooks.killStories() },
            PreferenceKeys.SHOW_SECONDS to { hooks.showTimestampSeconds() },
            PreferenceKeys.DEFAULT_HD_MEDIA to { hooks.defaultHdMediaSending() },
            PreferenceKeys.DISABLE_ATTACH_CAMERA_PREVIEW to { hooks.disableAttachCameraPreview() },
            PreferenceKeys.HIDE_KEYBOARD_ON_SCROLL to { hooks.hideKeyboardOnScroll() },
            PreferenceKeys.HIDE_DIALOGS_FAB to { hooks.hideDialogsFloatingButton() },
            PreferenceKeys.HIDE_CHANNEL_BOTTOM_BAR to { hooks.hideChannelBottomBar() },
            PreferenceKeys.DISABLE_AUTO_AUDIO to { hooks.killAutoAudio() },
            PreferenceKeys.HIDE_APP_UPDATES to { hooks.hideAppUpdates() },
            PreferenceKeys.KEEP_DELETED_MESSAGES to { hooks.keepDeletedMessages() },
            PreferenceKeys.DISABLE_ROUNDING to { hooks.noRounding() },
            PreferenceKeys.FORCE_SYSTEM_TYPEFACE to { hooks.forceSystemTypeface() }
        )

        hooksMap.forEach { (key, action) ->
            if (prefs.getBoolean(key, false)) {
                action.invoke()
            }
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam?) {
        if (resparam?.packageName == null || packageBlocklist.contains(resparam.packageName)) {
            return
        }
        if (moduleResources == null) {
            log("Can't find module resources!", "ERROR")
            return
        }

        if (prefs.getBoolean(PreferenceKeys.SOLAR_ICONS, false)) {
            log("Injecting Solar icons...")

            val drawablesJavaClassSelf = R.drawable::class.java
            val drawablesSelf = drawablesJavaClassSelf.declaredFields
            
            var totalAttempts = 0
            var successfulReplacements = 0
            for (field in drawablesSelf) {
                val resourceIdSelf: Int
                try {
                    resourceIdSelf = field.getInt(drawablesJavaClassSelf)
                } catch (e: Exception) {
                    continue
                }
                try {
                    if (field.name.contains("solar")) {
                        totalAttempts++
                        resparam.res.setReplacement(resparam.packageName,
                            "drawable",
                            field.name.replace("_solar", ""),
                            moduleResources!!.fwd(resourceIdSelf))
                        successfulReplacements++
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            log("Succeeded $successfulReplacements, failed ${totalAttempts - successfulReplacements}", "SOLAR")
        }
    }
}
