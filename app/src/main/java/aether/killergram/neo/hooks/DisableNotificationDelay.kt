package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.os.PowerManager
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.disableNotificationDelay() {
    log("Disabling stock notification delay...")

    val notificationsControllerClass = loadClass("org.telegram.messenger.NotificationsController") ?: return

    XposedBridge.hookAllMethods(
        notificationsControllerClass,
        "scheduleNotificationDelay",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                cancelPendingDelay(notificationsControllerClass, param.thisObject)
                flushDelayedNotifications(param.thisObject)
                releaseDelayWakelock(param.thisObject)
                param.result = null
            }
        }
    )
}

private fun cancelPendingDelay(notificationsControllerClass: Class<*>, controller: Any) {
    runCatching {
        val notificationsQueue = XposedHelpers.getStaticObjectField(
            notificationsControllerClass,
            "notificationsQueue"
        )
        val notificationDelayRunnable = XposedHelpers.getObjectField(
            controller,
            "notificationDelayRunnable"
        ) as? Runnable

        if (notificationsQueue != null && notificationDelayRunnable != null) {
            XposedHelpers.callMethod(notificationsQueue, "cancelRunnable", notificationDelayRunnable)
        }
    }.onFailure {
        log("Failed cancelling pending notification delay: ${it.message}", "DEBUG")
    }
}

private fun flushDelayedNotifications(controller: Any) {
    runCatching {
        val delayedPushMessages = XposedHelpers.getObjectField(
            controller,
            "delayedPushMessages"
        ) as? MutableCollection<*>

        if (!delayedPushMessages.isNullOrEmpty()) {
            XposedHelpers.callMethod(controller, "showOrUpdateNotification", true)
            delayedPushMessages.clear()
        }
    }.onFailure {
        log("Failed flushing delayed notifications: ${it.message}", "DEBUG")
    }
}

private fun releaseDelayWakelock(controller: Any) {
    runCatching {
        val wakelock = XposedHelpers.getObjectField(
            controller,
            "notificationDelayWakelock"
        ) as? PowerManager.WakeLock

        if (wakelock?.isHeld == true) {
            wakelock.release()
        }
    }.onFailure {
        log("Failed releasing notification delay wakelock: ${it.message}", "DEBUG")
    }
}
