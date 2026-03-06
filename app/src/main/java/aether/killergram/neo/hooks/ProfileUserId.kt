package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.content.Context
import android.view.View
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Modifier
import kotlin.math.abs

private const val USER_ID_ROW_FIELD = "kg_user_id_row"
private const val USER_ID_SELF_SECTION_FIELD = "kg_user_id_self_section"
private const val USER_ID_LABEL = "ID"
private const val CHANNEL_ID_SHIFT = 1_000_000_000_000L
private const val TEXT_COPIED_KEY = "TextCopied"
private const val TEXT_COPIED_FALLBACK = "Text copied to clipboard"

private val PROFILE_POSITION_EXTRAS = setOf(
    "rowCount",
    "helpSectionCell",
    "botPermissionsHeader",
    "botPermissionBiometry",
    "botPermissionEmojiStatus",
    "botPermissionLocation",
    "botPermissionsDivider"
)

fun Hooks.showProfileUserId() {
    log("Adding copyable profile ID row to profile screens...")

    val profileActivityClass = loadClass("org.telegram.ui.ProfileActivity") ?: return
    val listAdapterClass = loadClass("org.telegram.ui.ProfileActivity\$ListAdapter") ?: return
    val androidUtilitiesClass = loadClass("org.telegram.messenger.AndroidUtilities")
    val bulletinFactoryClass = loadClass("org.telegram.ui.Components.BulletinFactory")
    val textDetailViewType = runCatching {
        XposedHelpers.getStaticIntField(listAdapterClass, "VIEW_TYPE_TEXT_DETAIL")
    }.getOrNull() ?: 2
    val shadowViewType = runCatching {
        XposedHelpers.getStaticIntField(listAdapterClass, "VIEW_TYPE_SHADOW")
    }.getOrNull() ?: 7
    val shadowTextViewType = runCatching {
        XposedHelpers.getStaticIntField(listAdapterClass, "VIEW_TYPE_SHADOW_TEXT")
    }.getOrNull() ?: 26
    val headerEmptyViewType = runCatching {
        XposedHelpers.getStaticIntField(listAdapterClass, "VIEW_TYPE_HEADER_EMPTY")
    }.getOrNull() ?: 28

    XposedBridge.hookAllMethods(
        profileActivityClass,
        "updateRowsIds",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val profileActivity = param.thisObject ?: return
                installSyntheticUserIdRow(profileActivity)
            }
        }
    )

    XposedBridge.hookAllMethods(
        listAdapterClass,
        "getItemViewType",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val position = param.args.firstOrNull() as? Int ?: return
                val profileActivity = resolveProfileActivity(param.thisObject) ?: return
                if (position == getSyntheticUserIdRow(profileActivity)) {
                    param.result = textDetailViewType
                    return
                }

                val resolvedType = param.result as? Int ?: return
                if (resolvedType == 0 && shouldUseSyntheticSectionFallback(profileActivity, position)) {
                    param.result = shadowTextViewType
                }
            }
        }
    )

    XposedBridge.hookAllMethods(
        listAdapterClass,
        "onBindViewHolder",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val holder = param.args.firstOrNull() ?: return
                val position = param.args.getOrNull(1) as? Int ?: return
                val profileActivity = resolveProfileActivity(param.thisObject) ?: return
                if (position != getSyntheticUserIdRow(profileActivity)) {
                    return
                }

                val detailCell = runCatching {
                    XposedHelpers.getObjectField(holder, "itemView")
                }.getOrNull() ?: return

                val divider = if (usesSelfAccountSection(profileActivity)) {
                    true
                } else {
                    val itemCount = runCatching {
                        XposedHelpers.callMethod(param.thisObject, "getItemCount") as Int
                    }.getOrDefault(0)
                    val nextType = if (position + 1 < itemCount) {
                        runCatching {
                            XposedHelpers.callMethod(param.thisObject, "getItemViewType", position + 1) as Int
                        }.getOrNull()
                    } else {
                        null
                    }
                    nextType != shadowViewType &&
                        nextType != shadowTextViewType &&
                        nextType != headerEmptyViewType
                }

                runCatching {
                    XposedHelpers.callMethod(
                        detailCell,
                        "setTextAndValue",
                        resolveProfileIdValue(profileActivity).toString(),
                        USER_ID_LABEL,
                        divider
                    )
                    if (!divider) {
                        XposedHelpers.setBooleanField(detailCell, "needDivider", false)
                        XposedHelpers.callMethod(detailCell, "setWillNotDraw", true)
                    }
                    XposedHelpers.callMethod(detailCell, "setContentDescriptionValueFirst", false)
                    XposedHelpers.callMethod(detailCell, "requestLayout")
                    XposedHelpers.callMethod(detailCell, "invalidate")
                    (detailCell as? View)?.tag = position
                }.onFailure {
                    log("Failed to bind profile user ID row: ${it.message}", "DEBUG")
                }
            }
        }
    )

    XposedBridge.hookAllMethods(
        listAdapterClass,
        "isEnabled",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val holder = param.args.firstOrNull() ?: return
                val profileActivity = resolveProfileActivity(param.thisObject) ?: return
                val position = runCatching {
                    XposedHelpers.callMethod(holder, "getAdapterPosition") as Int
                }.getOrNull() ?: return
                if (position == getSyntheticUserIdRow(profileActivity)) {
                    param.result = true
                }
            }
        }
    )

    XposedBridge.hookAllMethods(
        profileActivityClass,
        "processOnClickOrPress",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val profileActivity = param.thisObject ?: return
                val position = param.args.firstOrNull() as? Int ?: return
                if (position != getSyntheticUserIdRow(profileActivity)) {
                    return
                }

                val copied = androidUtilitiesClass?.let {
                    runCatching {
                        XposedHelpers.callStaticMethod(
                            it,
                            "addToClipboard",
                            resolveProfileIdValue(profileActivity).toString()
                        ) as? Boolean
                    }.getOrNull()
                } ?: false

                if (copied == true) {
                    showCopyBulletin(
                        profileActivity,
                        bulletinFactoryClass,
                        resolveCopiedMessage(param.args.getOrNull(1) as? View, profileActivity)
                    )
                }
                param.result = true
            }
        }
    )
}

private fun installSyntheticUserIdRow(profileActivity: Any) {
    val userId = readLongField(profileActivity, "userId")
    val profileId = resolveProfileIdValue(profileActivity)
    if (profileId == 0L) {
        setSyntheticUserIdRow(profileActivity, -1, false)
        return
    }

    val myProfile = readBooleanField(profileActivity, "myProfile")
    val clientUserId = if (userId != 0L) {
        runCatching {
            val userConfig = XposedHelpers.callMethod(profileActivity, "getUserConfig")
            (XposedHelpers.callMethod(userConfig, "getClientUserId") as Number).toLong()
        }.getOrDefault(0L)
    } else {
        0L
    }

    val selfSettingsProfile = userId != 0L && !myProfile && userId == clientUserId
    val originalInfoStartRow = readIntField(profileActivity, "infoStartRow")
    val originalInfoEndRow = readIntField(profileActivity, "infoEndRow")
    val insertPosition = if (selfSettingsProfile) {
        readIntField(profileActivity, "setUsernameRow").takeIf { it >= 0 }?.plus(1)
    } else {
        when {
            readIntField(profileActivity, "usernameRow") >= 0 -> readIntField(profileActivity, "usernameRow") + 1
            readIntField(profileActivity, "noteRow") >= 0 -> readIntField(profileActivity, "noteRow") + 1
            readIntField(profileActivity, "bizLocationRow") >= 0 -> readIntField(profileActivity, "bizLocationRow") + 1
            readIntField(profileActivity, "bizHoursRow") >= 0 -> readIntField(profileActivity, "bizHoursRow") + 1
            readIntField(profileActivity, "birthdayRow") >= 0 -> readIntField(profileActivity, "birthdayRow") + 1
            readIntField(profileActivity, "locationRow") >= 0 -> readIntField(profileActivity, "locationRow") + 1
            readIntField(profileActivity, "channelInfoRow") >= 0 -> readIntField(profileActivity, "channelInfoRow") + 1
            readIntField(profileActivity, "userInfoRow") >= 0 -> readIntField(profileActivity, "userInfoRow") + 1
            readIntField(profileActivity, "phoneRow") >= 0 -> readIntField(profileActivity, "phoneRow") + 1
            readIntField(profileActivity, "infoStartRow") >= 0 -> readIntField(profileActivity, "infoStartRow")
            readIntField(profileActivity, "infoHeaderRow") >= 0 -> readIntField(profileActivity, "infoHeaderRow") + 1
            readIntField(profileActivity, "infoSectionRow") >= 0 -> readIntField(profileActivity, "infoSectionRow")
            else -> null
        }
    }

    if (insertPosition == null) {
        setSyntheticUserIdRow(profileActivity, -1, false)
        return
    }

    shiftProfilePositions(profileActivity, insertPosition)
    if (!selfSettingsProfile) {
        if (originalInfoStartRow >= 0 && insertPosition == originalInfoStartRow) {
            XposedHelpers.setIntField(profileActivity, "infoStartRow", originalInfoStartRow)
        }
        if (originalInfoEndRow >= 0) {
            XposedHelpers.setIntField(profileActivity, "infoEndRow", originalInfoEndRow + 1)
        }
    }
    setSyntheticUserIdRow(profileActivity, insertPosition, selfSettingsProfile)
}

private fun shiftProfilePositions(profileActivity: Any, insertPosition: Int) {
    profileActivity.javaClass.declaredFields.forEach { field ->
        if (Modifier.isStatic(field.modifiers) || field.type != Int::class.javaPrimitiveType) {
            return@forEach
        }
        if (!isProfilePositionField(field.name)) {
            return@forEach
        }

        field.isAccessible = true
        val currentValue = runCatching { field.getInt(profileActivity) }.getOrNull() ?: return@forEach
        when {
            field.name == "rowCount" -> field.setInt(profileActivity, currentValue + 1)
            currentValue >= insertPosition && currentValue != -1 -> field.setInt(profileActivity, currentValue + 1)
        }
    }
}

private fun isProfilePositionField(fieldName: String): Boolean {
    return fieldName.contains("Row") || fieldName in PROFILE_POSITION_EXTRAS
}

private fun resolveProfileActivity(instance: Any?): Any? {
    if (instance == null) {
        return null
    }
    return runCatching {
        XposedHelpers.getObjectField(instance, "this\$0")
    }.getOrElse { instance }
}

private fun setSyntheticUserIdRow(profileActivity: Any, row: Int, selfSection: Boolean) {
    XposedHelpers.setAdditionalInstanceField(profileActivity, USER_ID_ROW_FIELD, row)
    XposedHelpers.setAdditionalInstanceField(profileActivity, USER_ID_SELF_SECTION_FIELD, selfSection)
}

private fun getSyntheticUserIdRow(profileActivity: Any): Int {
    return (XposedHelpers.getAdditionalInstanceField(profileActivity, USER_ID_ROW_FIELD) as? Int) ?: -1
}

private fun usesSelfAccountSection(profileActivity: Any): Boolean {
    return (XposedHelpers.getAdditionalInstanceField(profileActivity, USER_ID_SELF_SECTION_FIELD) as? Boolean) == true
}

private fun shouldUseSyntheticSectionFallback(profileActivity: Any, position: Int): Boolean {
    if (usesSelfAccountSection(profileActivity)) {
        return false
    }

    val syntheticRow = getSyntheticUserIdRow(profileActivity)
    val infoSectionRow = readIntField(profileActivity, "infoSectionRow")
    if (syntheticRow < 0 || infoSectionRow < 0) {
        return false
    }

    // Telegram falls back to the build-version footer for unknown positions.
    // If our injected row leaves a temporary hole before the info section break,
    // coerce it into the usual blank section cell instead.
    return position > syntheticRow && position <= infoSectionRow
}

private fun readIntField(instance: Any, fieldName: String): Int {
    return runCatching { XposedHelpers.getIntField(instance, fieldName) }.getOrDefault(-1)
}

private fun resolveProfileIdValue(instance: Any): Long {
    val userId = readLongField(instance, "userId")
    if (userId != 0L) {
        return userId
    }

    val chatId = readLongField(instance, "chatId")
    if (chatId != 0L) {
        return resolveChatIdValue(instance, chatId)
    }

    return 0L
}

private fun resolveChatIdValue(instance: Any, chatId: Long): Long {
    val normalizedId = abs(chatId)
    val currentChat = runCatching {
        XposedHelpers.getObjectField(instance, "currentChat")
    }.getOrNull()
    val isChannelLike = currentChat?.javaClass?.name?.contains("TL_channel") == true

    return if (isChannelLike) {
        -(CHANNEL_ID_SHIFT + normalizedId)
    } else {
        -normalizedId
    }
}

private fun readLongField(instance: Any, fieldName: String): Long {
    return runCatching { XposedHelpers.getLongField(instance, fieldName) }.getOrDefault(0L)
}

private fun readBooleanField(instance: Any, fieldName: String): Boolean {
    return runCatching { XposedHelpers.getBooleanField(instance, fieldName) }.getOrDefault(false)
}

private fun resolveCopiedMessage(view: View?, profileActivity: Any): String {
    val context = view?.context ?: runCatching {
        XposedHelpers.callMethod(profileActivity, "getContext") as? Context
    }.getOrNull()
    if (context != null) {
        val resId = context.resources.getIdentifier(TEXT_COPIED_KEY, "string", context.packageName)
        if (resId != 0) {
            return context.getString(resId)
        }
    }
    return TEXT_COPIED_FALLBACK
}

private fun showCopyBulletin(profileActivity: Any, bulletinFactoryClass: Class<*>?, message: String) {
    if (bulletinFactoryClass == null) {
        return
    }
    runCatching {
        val canShow = XposedHelpers.callStaticMethod(
            bulletinFactoryClass,
            "canShowBulletin",
            profileActivity
        ) as? Boolean ?: false
        if (!canShow) {
            return
        }
        val factory = XposedHelpers.callStaticMethod(bulletinFactoryClass, "of", profileActivity)
        val bulletin = XposedHelpers.callMethod(factory, "createCopyBulletin", message)
        XposedHelpers.callMethod(bulletin, "show")
    }.onFailure {
        log("Failed to show profile user ID copy bulletin: ${it.message}", "DEBUG")
    }
}
