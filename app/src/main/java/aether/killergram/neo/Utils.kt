package aether.killergram.neo

import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge

class PreferencesUtils {
    private lateinit var preferencesInstance: XSharedPreferences

    fun getPrefsInstance(prefsName: String = "function_switches"): XSharedPreferences {
        return if (!this::preferencesInstance.isInitialized) {
            val instance = XSharedPreferences("aether.killergram.neo", prefsName)
            instance.makeWorldReadable()
            this.preferencesInstance = instance
            instance
        } else {
            val instance = this.preferencesInstance
            instance.reload()
            instance
        }
    }
}

fun log(message: String, level: String = "") {
    // TODO: Implement toggleable debug logging
    if (level == "") {
        XposedBridge.log("[KG Neo] $message")
    } else {
        XposedBridge.log("[KG Neo]-[$level] $message")
    }
}
