package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.hideAppUpdates() {
    log("Disabling in-app update prompts...")

    val sharedConfigClass = loadClass("org.telegram.messenger.SharedConfig") ?: return
    val applicationLoaderClass = loadClass("org.telegram.messenger.ApplicationLoader")
    val launchActivityClass = loadClass("org.telegram.ui.LaunchActivity")

    // Blocks update badges/buttons and prevents new update state from being accepted.
    XposedBridge.hookAllMethods(
        sharedConfigClass,
        "isAppUpdateAvailable",
        XC_MethodReplacement.returnConstant(false)
    )
    XposedBridge.hookAllMethods(
        sharedConfigClass,
        "setNewAppVersionAvailable",
        XC_MethodReplacement.returnConstant(false)
    )

    // Blocks the hard update screen route used for non-skippable updates.
    if (launchActivityClass != null) {
        XposedBridge.hookAllMethods(
            launchActivityClass,
            "showUpdateActivity",
            XC_MethodReplacement.returnConstant(null)
        )
    }

    // FileRefController may set pendingAppUpdate directly; null it before each save.
    XposedBridge.hookAllMethods(
        sharedConfigClass,
        "saveConfig",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                clearPendingUpdateState(sharedConfigClass)
            }
        }
    )

    clearPendingUpdateState(sharedConfigClass)
    clearPersistedUpdateState(applicationLoaderClass)

    if (applicationLoaderClass != null) {
        XposedBridge.hookAllMethods(
            applicationLoaderClass,
            "postInitApplication",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    clearPersistedUpdateState(applicationLoaderClass)
                }
            }
        )
    }
}

private fun clearPendingUpdateState(sharedConfigClass: Class<*>) {
    runCatching {
        XposedHelpers.setStaticObjectField(sharedConfigClass, "pendingAppUpdate", null)
    }.onFailure {
        log("Failed to clear pendingAppUpdate: ${it.message}", "DEBUG")
    }

    runCatching {
        XposedHelpers.setStaticIntField(sharedConfigClass, "pendingAppUpdateBuildVersion", 0)
    }.onFailure {
        log("Failed to clear pendingAppUpdateBuildVersion: ${it.message}", "DEBUG")
    }
}

private fun clearPersistedUpdateState(applicationLoaderClass: Class<*>?) {
    if (applicationLoaderClass == null) {
        return
    }

    val context = runCatching {
        XposedHelpers.getStaticObjectField(applicationLoaderClass, "applicationContext") as? Context
    }.getOrNull() ?: return

    listOf("userconfing", "mainconfig").forEach { prefsName ->
        runCatching {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
                .edit()
                .remove("appUpdate")
                .remove("appUpdateCheckTime")
                .remove("appUpdateBuild")
                .apply()
        }.onFailure {
            log("Failed clearing $prefsName update keys: ${it.message}", "DEBUG")
        }
    }
}
