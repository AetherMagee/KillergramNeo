package aether.killergram.neo.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import java.util.concurrent.TimeUnit

object RestartTargetResolver {
    private const val PREFS_NAME = "kgneo_ui_state"
    private const val KEY_LAST_TARGET_PACKAGE = "last_target_package"

    private val officialPriority = listOf(
        "org.telegram.messenger",
        "org.telegram.messenger.web"
    )

    fun resolvePreferredTarget(context: Context): TargetApp? {
        val installedTargets = TargetApps.getInstalledScopeTargets(context)
        if (installedTargets.isEmpty()) {
            return null
        }

        officialPriority.forEach { officialPackage ->
            installedTargets.firstOrNull { it.packageName == officialPackage }?.let { return it }
        }

        val customPackages = installedTargets.map { it.packageName }.toSet()
        val lastOpenedCustomPackage = findLastOpenedPackage(context, customPackages)
        if (lastOpenedCustomPackage != null) {
            installedTargets.firstOrNull { it.packageName == lastOpenedCustomPackage }?.let { return it }
        }

        val rememberedPackage = getRememberedTargetPackage(context)
        if (rememberedPackage != null) {
            installedTargets.firstOrNull { it.packageName == rememberedPackage }?.let { return it }
        }

        return installedTargets.first()
    }

    fun rememberTargetPackage(context: Context, packageName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_TARGET_PACKAGE, packageName)
            .apply()
    }

    fun launchTarget(context: Context, packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(launchIntent)
            true
        }.getOrDefault(false)
    }

    private fun getRememberedTargetPackage(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LAST_TARGET_PACKAGE, null)
    }

    private fun findLastOpenedPackage(context: Context, candidatePackages: Set<String>): String? {
        if (candidatePackages.isEmpty()) {
            return null
        }

        val usageStats = context.getSystemService(UsageStatsManager::class.java) ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(7)
        val usageEvents = runCatching { usageStats.queryEvents(startTime, endTime) }.getOrNull() ?: return null

        val event = UsageEvents.Event()
        var resultPackage: String? = null
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val packageName = event.packageName ?: continue
            if (packageName !in candidatePackages) {
                continue
            }

            if (isForegroundEvent(event.eventType)) {
                resultPackage = packageName
            }
        }

        return resultPackage
    }

    @Suppress("DEPRECATION")
    private fun isForegroundEvent(eventType: Int): Boolean {
        return eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
            eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
    }
}
