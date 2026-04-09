package aether.killergram.neo

import aether.killergram.neo.core.PreferenceKeys
import aether.killergram.neo.hooks.Hooks
import aether.killergram.neo.hooks.cameraDefaultBack
import aether.killergram.neo.hooks.cameraHigherBitrate
import aether.killergram.neo.hooks.cameraHigherResolution
import aether.killergram.neo.hooks.cameraKeepZoom
import aether.killergram.neo.hooks.forceAllowForwards
import aether.killergram.neo.hooks.forceSystemTypeface
import aether.killergram.neo.hooks.hideChannelBottomBar
import aether.killergram.neo.hooks.hideAppUpdates
import aether.killergram.neo.hooks.hideDialogsFloatingButton
import aether.killergram.neo.hooks.hideHamburgerMenuButtons
import aether.killergram.neo.hooks.hideInputBoxButtons
import aether.killergram.neo.hooks.hideKeyboardOnScroll
import aether.killergram.neo.hooks.hidePaidStarReactions
import aether.killergram.neo.hooks.hidePhoneNumber
import aether.killergram.neo.hooks.hidePostShareButton
import aether.killergram.neo.hooks.showProfileUserId
import aether.killergram.neo.hooks.replaceEditedLabelWithIcon
import aether.killergram.neo.hooks.injectAdaptiveMonetThemes
import aether.killergram.neo.hooks.keepDeletedMessages
import aether.killergram.neo.hooks.killAutoAudio
import aether.killergram.neo.hooks.killSponsoredMessages
import aether.killergram.neo.hooks.killStories
import aether.killergram.neo.hooks.localPremium
import aether.killergram.neo.hooks.noRounding
import aether.killergram.neo.hooks.defaultHdMediaSending
import aether.killergram.neo.hooks.disableNotificationDelay
import aether.killergram.neo.hooks.disableAttachCameraPreview
import aether.killergram.neo.hooks.folderIcons
import aether.killergram.neo.hooks.replaceAppTitle
import aether.killergram.neo.hooks.unlimitedRecents
import aether.killergram.neo.hooks.showTimestampSeconds
import aether.killergram.neo.hooks.videoNoteSizeGuard
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
            PreferenceKeys.HIDE_STORIES to { hooks.killStories() },
            PreferenceKeys.EDITED_ICON to { hooks.replaceEditedLabelWithIcon() },
            PreferenceKeys.SHOW_SECONDS to { hooks.showTimestampSeconds() },
            PreferenceKeys.SHOW_USER_ID to { hooks.showProfileUserId() },
            PreferenceKeys.DEFAULT_HD_MEDIA to { hooks.defaultHdMediaSending() },
            PreferenceKeys.DISABLE_ATTACH_CAMERA_PREVIEW to { hooks.disableAttachCameraPreview() },
            PreferenceKeys.HIDE_KEYBOARD_ON_SCROLL to { hooks.hideKeyboardOnScroll() },
            PreferenceKeys.HIDE_DIALOGS_FAB to { hooks.hideDialogsFloatingButton() },
            PreferenceKeys.HIDE_PAID_STAR_REACTIONS to { hooks.hidePaidStarReactions() },
            PreferenceKeys.HIDE_CHANNEL_BOTTOM_BAR to { hooks.hideChannelBottomBar() },
            PreferenceKeys.HIDE_POST_SHARE_BUTTON to { hooks.hidePostShareButton() },
            PreferenceKeys.DISABLE_NOTIFICATION_DELAY to { hooks.disableNotificationDelay() },
            PreferenceKeys.DISABLE_AUTO_AUDIO to { hooks.killAutoAudio() },
            PreferenceKeys.HIDE_APP_UPDATES to { hooks.hideAppUpdates() },
            PreferenceKeys.ADAPTIVE_MONET_THEMES to { hooks.injectAdaptiveMonetThemes() },
            PreferenceKeys.CUSTOMIZE_HAMBURGER_MENU to { hooks.hideHamburgerMenuButtons() },
            PreferenceKeys.CUSTOMIZE_INPUT_BOX_BUTTONS to { hooks.hideInputBoxButtons() },
            PreferenceKeys.KEEP_DELETED_MESSAGES to { hooks.keepDeletedMessages() },
            PreferenceKeys.DISABLE_ROUNDING to { hooks.noRounding() },
            PreferenceKeys.FORCE_SYSTEM_TYPEFACE to { hooks.forceSystemTypeface() },
            PreferenceKeys.HIDE_PHONE_NUMBER to { hooks.hidePhoneNumber() },
            PreferenceKeys.CAMERA_DEFAULT_BACK to { hooks.cameraDefaultBack() },
            PreferenceKeys.CAMERA_KEEP_ZOOM to { hooks.cameraKeepZoom() }
        )

        hooksMap.forEach { (key, action) ->
            if (prefs.getBoolean(key, false)) {
                action.invoke()
            }
        }

        if (prefs.getBoolean(PreferenceKeys.UNLIMITED_RECENTS, false)) {
            val stickersLimit = prefs.getInt(PreferenceKeys.RECENT_STICKERS_LIMIT, 120)
            val emojiLimit = prefs.getInt(PreferenceKeys.RECENT_EMOJI_LIMIT, 120)
            hooks.unlimitedRecents(stickersLimit, emojiLimit)
        }

        val higherBitrate = prefs.getBoolean(PreferenceKeys.CAMERA_HIGHER_BITRATE, false)
        val higherResolution = prefs.getBoolean(PreferenceKeys.CAMERA_HIGHER_RESOLUTION, false)

        if (higherBitrate) {
            val bitrate = prefs.getStringSet(PreferenceKeys.CAMERA_BITRATE_VALUE, setOf("1200"))
                ?.firstOrNull()?.toIntOrNull() ?: 1200
            hooks.cameraHigherBitrate(bitrate)
        }

        if (higherResolution) {
            val resolution = prefs.getStringSet(PreferenceKeys.CAMERA_RESOLUTION_VALUE, setOf("512"))
                ?.firstOrNull()?.toIntOrNull() ?: 512
            hooks.cameraHigherResolution(resolution)
        }

        if (higherBitrate || higherResolution) {
            hooks.videoNoteSizeGuard()
        }

        if (prefs.getBoolean(PreferenceKeys.FOLDER_ICONS, false) && moduleResources != null) {
            val displayMode = prefs.getStringSet(PreferenceKeys.FOLDER_TAB_DISPLAY_MODE, setOf("icon"))
                ?.firstOrNull() ?: "icon"
            val useSmallerIcons = prefs.getBoolean(PreferenceKeys.FOLDER_TAB_SMALLER_ICONS, false)
            hooks.folderIcons(moduleResources!!, displayMode, useSmallerIcons)
        }

        if (prefs.getBoolean(PreferenceKeys.REPLACE_APP_TITLE, false)) {
            val mode = prefs.getStringSet(PreferenceKeys.APP_TITLE_MODE, setOf("firstname"))
                ?.firstOrNull() ?: "firstname"
            val customText = prefs.getString(PreferenceKeys.APP_TITLE_CUSTOM_TEXT, "")
            val centerTitle = prefs.getBoolean(PreferenceKeys.APP_TITLE_CENTER, false)
            hooks.replaceAppTitle(mode, customText ?: "", centerTitle)
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
