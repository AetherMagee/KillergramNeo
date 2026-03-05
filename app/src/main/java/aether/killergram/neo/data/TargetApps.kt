package aether.killergram.neo.data

import aether.killergram.neo.R
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

data class TargetApp(
    val packageName: String,
    val label: String
)

object TargetApps {
    fun getInstalledScopeTargets(context: Context): List<TargetApp> {
        val scopePackages = context.resources.getStringArray(R.array.xposedscope).toList()
        val packageManager = context.packageManager

        return scopePackages.mapNotNull { packageName ->
            val appInfo = packageManager.findApplicationInfo(packageName) ?: return@mapNotNull null
            val label = packageManager.getApplicationLabel(appInfo).toString()
            TargetApp(
                packageName = packageName,
                label = if (label.isBlank()) packageName else label
            )
        }
    }
}

private fun PackageManager.findApplicationInfo(packageName: String): ApplicationInfo? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            getApplicationInfo(packageName, 0)
        }
    }.getOrNull()
}
