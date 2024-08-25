package aether.killergram.neo

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
            "localpremium" to { hooks.localPremium() },
            "sponsored" to { hooks.killSponsoredMessages() },
            "forward" to { hooks.forceAllowForwards() },
            "accountlimit" to { hooks.overrideAccountCount() },
            "stories" to { hooks.killStories() },
            "volume" to { hooks.killAutoAudio() },
            "deleted" to { hooks.keepDeletedMessages() },
            "thanos" to { hooks.disableThanosEffect() }
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