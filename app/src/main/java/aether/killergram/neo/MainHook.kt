package aether.killergram.neo

import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam
import de.robv.android.xposed.XposedHelpers
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
        val hooks = Hooks()

        val userConfigClass = XposedHelpers.findClassIfExists("org.telegram.messenger.UserConfig", lpparam.classLoader)
        val messageControllerClass = XposedHelpers.findClassIfExists("org.telegram.messenger.MessagesController", lpparam.classLoader)
        val chatUIActivityClass = XposedHelpers.findClassIfExists("org.telegram.ui.ChatActivity", lpparam.classLoader)
        val storiesControllerClass = XposedHelpers.findClassIfExists("org.telegram.ui.Stories.StoriesController", lpparam.classLoader)
        val messageObjectClass = XposedHelpers.findClassIfExists("org.telegram.messenger.MessageObject", lpparam.classLoader)
        val launchActivityClass = XposedHelpers.findClassIfExists("org.telegram.ui.LaunchActivity", lpparam.classLoader)

        // Local Premium
        if (prefs.getBoolean("localpremium", false)) {
            hooks.localPremium(userConfigClass)
        }

        // Sponsored messages
        if (prefs.getBoolean("sponsored", false)) {
            hooks.killSponsoredMessages(messageControllerClass, chatUIActivityClass)
        }

        // Forwarding
        if (prefs.getBoolean("forward", false)) {
            hooks.forceAllowForwards(messageControllerClass, messageObjectClass)
        }

        // Account limit
        if (prefs.getBoolean("accountlimit", false)) {
            hooks.overrideAccountCount(userConfigClass)
        }

        // Hide stories
        if (prefs.getBoolean("stories", false)) {
            hooks.killStories(storiesControllerClass)
        }

        // Volume button
        if (prefs.getBoolean("volume", false)) {
            hooks.killAutoAudio(launchActivityClass)
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

        if (prefs.getBoolean("solar", false)) {
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
//                        log("SUCCESS: ${field.name}", "DRAWABLE")
                        successfulReplacements++
                    }
                } catch (e: Exception) {
//                    log("FAIL: ${field.name}", "DRAWABLE")
                    continue
                }
            }
            log("Succeeded $successfulReplacements, failed ${totalAttempts - successfulReplacements}", "SOLAR")
        }
    }
}