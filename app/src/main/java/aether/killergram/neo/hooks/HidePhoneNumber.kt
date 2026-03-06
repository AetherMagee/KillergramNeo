package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

private const val PHONE_PLACEHOLDER = "+0 (000) 000-00-00"

fun Hooks.hidePhoneNumber() {
    log("Hiding phone number from UI...")

    val drawerProfileCellClass = loadClass("org.telegram.ui.Cells.DrawerProfileCell") ?: return
    val listAdapterClass = loadClass("org.telegram.ui.ProfileActivity\$ListAdapter") ?: return

    // Hook DrawerProfileCell.setUser — replaces the phone text in the navigation drawer header
    XposedBridge.hookAllMethods(
        drawerProfileCellClass,
        "setUser",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val cell = param.thisObject ?: return
                val phoneTextView = runCatching {
                    XposedHelpers.getObjectField(cell, "phoneTextView") as? TextView
                }.getOrNull() ?: return
                phoneTextView.text = PHONE_PLACEHOLDER
            }
        }
    )

    // Hook ProfileActivity$ListAdapter.onBindViewHolder — replaces phone on own profile
    XposedBridge.hookAllMethods(
        listAdapterClass,
        "onBindViewHolder",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val position = param.args.getOrNull(1) as? Int ?: return
                val profileActivity = resolveOuterProfileActivity(param.thisObject) ?: return

                val numberRow = runCatching {
                    XposedHelpers.getIntField(profileActivity, "numberRow")
                }.getOrDefault(-1)

                val phoneRow = runCatching {
                    XposedHelpers.getIntField(profileActivity, "phoneRow")
                }.getOrDefault(-1)

                val isNumberRow = numberRow >= 0 && position == numberRow
                val isPhoneRow = phoneRow >= 0 && position == phoneRow

                if (!isNumberRow && !isPhoneRow) return

                if (isPhoneRow && !isOwnProfile(profileActivity)) return

                val holder = param.args.firstOrNull() ?: return
                val itemView = runCatching {
                    XposedHelpers.getObjectField(holder, "itemView")
                }.getOrNull() ?: return

                // TextDetailCell has a public `textView` field (may be on the superclass)
                val textView = runCatching {
                    XposedHelpers.getObjectField(itemView, "textView") as? TextView
                }.getOrNull()

                if (textView != null) {
                    textView.text = PHONE_PLACEHOLDER
                }
            }
        }
    )
}

private fun isOwnProfile(profileActivity: Any): Boolean {
    val userId = runCatching {
        XposedHelpers.getLongField(profileActivity, "userId")
    }.getOrDefault(0L)

    if (userId == 0L) return true

    val myProfile = runCatching {
        XposedHelpers.getBooleanField(profileActivity, "myProfile")
    }.getOrDefault(false)
    if (myProfile) return true

    val clientUserId = runCatching {
        val userConfig = XposedHelpers.callMethod(profileActivity, "getUserConfig")
        (XposedHelpers.callMethod(userConfig, "getClientUserId") as Number).toLong()
    }.getOrDefault(0L)

    return userId != 0L && userId == clientUserId
}

private fun resolveOuterProfileActivity(listAdapter: Any?): Any? {
    if (listAdapter == null) return null
    return runCatching {
        XposedHelpers.getObjectField(listAdapter, "this\$0")
    }.getOrNull()
}
