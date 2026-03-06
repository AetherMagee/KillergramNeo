package aether.killergram.neo.data

import android.content.Context

object TargetAppStringResolver {
    fun resolve(
        context: Context,
        targetPackageName: String?,
        resourceNames: List<String>,
        fallback: String
    ): String {
        if (targetPackageName.isNullOrBlank() || resourceNames.isEmpty()) {
            return fallback
        }

        val resources = runCatching {
            context.packageManager.getResourcesForApplication(targetPackageName)
        }.getOrNull() ?: return fallback

        resourceNames.forEach { resourceName ->
            val resourceId = resources.getIdentifier(resourceName, "string", targetPackageName)
            if (resourceId != 0) {
                return runCatching { resources.getString(resourceId) }.getOrDefault(fallback)
            }
        }

        return fallback
    }
}
