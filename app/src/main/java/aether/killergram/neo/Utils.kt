package aether.killergram.neo

import android.util.Log
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
    val debugLogging = PreferencesUtils().getPrefsInstance().getBoolean("debug", false)
    val tag = "[KG Neo]"
    if (level == "") {
        XposedBridge.log("$tag $message")
        Log.i(tag, message)
    } else if ((level == "DEBUG" || level == "SOLAR") && debugLogging) {
        XposedBridge.log("$tag-[DEBUG] $message")
        Log.d(tag, message)
    } else if (level == "ERROR") {
        XposedBridge.log("$tag-[ERROR] $message")
        Log.e(tag, message)
    } else if (level != "DEBUG") {
        XposedBridge.log("$tag-[$level] $message")
        Log.i(tag, "[$level] $message")
    }
}
