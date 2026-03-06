package aether.killergram.neo.data

import aether.killergram.neo.core.PreferenceKeys
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class ModulePrefsStore private constructor(
    private val preferences: SharedPreferences
) {
    fun isEnabled(key: String): Boolean = preferences.getBoolean(key, false)

    fun getStates(keys: Collection<String>): Map<String, Boolean> {
        return keys.associateWith(::isEnabled)
    }

    fun setEnabled(key: String, enabled: Boolean) {
        preferences.edit().putBoolean(key, enabled).apply()
    }

    fun getInt(key: String, default: Int): Int = preferences.getInt(key, default)

    fun setInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    companion object {
        @Suppress("DEPRECATION")
        @SuppressLint("WorldReadableFiles")
        fun create(context: Context): ModulePrefsStore? {
            return try {
                val prefs = context.getSharedPreferences(
                    PreferenceKeys.PREFS_NAME,
                    Context.MODE_WORLD_READABLE
                )
                ModulePrefsStore(prefs)
            } catch (_: SecurityException) {
                null
            }
        }
    }
}
