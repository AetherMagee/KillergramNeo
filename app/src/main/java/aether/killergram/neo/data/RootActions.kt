package aether.killergram.neo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object RootActions {
    suspend fun hasRootAccess(): Boolean = withContext(Dispatchers.IO) {
        runRootCommand("id")
    }

    suspend fun forceStopPackage(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (packageName.isBlank()) {
            return@withContext false
        }
        runRootCommand("am force-stop $packageName")
    }

    private fun runRootCommand(command: String): Boolean {
        val process = runCatching { ProcessBuilder("su", "-c", command).start() }.getOrNull() ?: return false

        return try {
            val finished = process.waitFor(10, TimeUnit.SECONDS)
            finished && process.exitValue() == 0
        } catch (_: Exception) {
            false
        } finally {
            process.destroy()
        }
    }
}
