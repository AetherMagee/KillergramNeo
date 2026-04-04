package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.content.Context
import android.graphics.Paint
import android.text.SpannableStringBuilder
import android.text.Spanned
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import kotlin.math.abs
import kotlin.math.roundToInt

private const val EDIT_ICON_PLACEHOLDER = "d"
private const val EDIT_ICON_FALLBACK = "\u270e"
private const val EDIT_ICON_SCALE = 0.75f
private const val EDIT_ICON_TOP_OFFSET = 0

private val editedLabelCache = HashMap<String, String>()
private val editedIconCache = HashMap<String, Int?>()

fun Hooks.replaceEditedLabelWithIcon() {
    log("Replacing edited labels with icon...")

    val chatMessageCellClass = loadClass("org.telegram.ui.Cells.ChatMessageCell") ?: return
    val themeClass = loadClass("org.telegram.ui.ActionBar.Theme") ?: return
    val coloredImageSpanClass = loadClass("org.telegram.ui.Components.ColoredImageSpan")

    XposedBridge.hookAllMethods(
        chatMessageCellClass,
        "measureTime",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val cell = param.thisObject ?: return
                val edited = runCatching {
                    XposedHelpers.getBooleanField(cell, "edited")
                }.getOrDefault(false)
                if (!edited) {
                    return
                }

                val currentTimeString = runCatching {
                    XposedHelpers.getObjectField(cell, "currentTimeString") as? CharSequence
                }.getOrNull() ?: return

                val editedLabel = resolveEditedLabel(cell) ?: return
                if (editedLabel.isBlank()) {
                    return
                }

                val editedIndex = currentTimeString.toString().indexOf(editedLabel)
                if (editedIndex < 0) {
                    return
                }
                val editedEnd = editedIndex + editedLabel.length

                val chatTimePaint = runCatching {
                    XposedHelpers.getStaticObjectField(themeClass, "chat_timePaint") as? Paint
                }.getOrNull() ?: return

                val iconDrawableId = resolveEditedIconDrawableId(cell)
                val iconSpan = if (iconDrawableId != null && coloredImageSpanClass != null) {
                    createEditedIconSpan(coloredImageSpanClass, iconDrawableId, chatTimePaint)
                } else {
                    null
                }

                val replacement = if (iconSpan != null) EDIT_ICON_PLACEHOLDER else EDIT_ICON_FALLBACK
                val updated = SpannableStringBuilder(currentTimeString).apply {
                    replace(editedIndex, editedEnd, replacement)
                    if (iconSpan != null) {
                        setSpan(
                            iconSpan,
                            editedIndex,
                            editedIndex + replacement.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                XposedHelpers.setObjectField(cell, "currentTimeString", updated)

                val replacementWidth = if (iconSpan != null) {
                    estimateIconWidth(chatTimePaint)
                } else {
                    chatTimePaint.measureText(EDIT_ICON_FALLBACK)
                }
                shrinkTimeWidths(cell, chatTimePaint.measureText(editedLabel), replacementWidth)
            }
        }
    )
}

private fun resolveEditedLabel(cell: Any): String? {
    val context = getViewContext(cell) ?: return null
    val localeTag = context.resources.configuration.locales.get(0)?.toLanguageTag() ?: "default"
    val cacheKey = "${context.packageName}:$localeTag"
    editedLabelCache[cacheKey]?.let { return it }

    val resourceId = context.resources.getIdentifier("EditedMessage", "string", context.packageName)
    if (resourceId != 0) {
        val value = runCatching { context.getString(resourceId) }.getOrNull()
        if (!value.isNullOrBlank()) {
            editedLabelCache[cacheKey] = value
        }
        return value
    }
    return null
}

private fun resolveEditedIconDrawableId(cell: Any): Int? {
    val context = getViewContext(cell) ?: return null
    val packageName = context.packageName
    if (editedIconCache.containsKey(packageName)) {
        return editedIconCache[packageName]
    }

    val resources = context.resources

    val candidates = listOf(
        "group_edit_profile", // Matches the profile header pencil icon style.
        "group_edit",
        "filled_profile_edit_24", // Telegram's filled pencil icon.
        "msg_edit",
        "menu_edit_appearance"
    )
    for (name in candidates) {
        val iconId = resources.getIdentifier(name, "drawable", packageName)
        if (iconId != 0) {
            editedIconCache[packageName] = iconId
            return iconId
        }
    }

    editedIconCache[packageName] = null
    return null
}

private fun getViewContext(cell: Any): Context? {
    return runCatching {
        XposedHelpers.callMethod(cell, "getContext") as? Context
    }.getOrNull()
}

private fun createEditedIconSpan(coloredImageSpanClass: Class<*>, iconResId: Int, paint: Paint): Any? {
    val span = runCatching {
        coloredImageSpanClass.getConstructor(Int::class.javaPrimitiveType).newInstance(iconResId)
    }.getOrNull() ?: return null

    runCatching { XposedHelpers.callMethod(span, "setRelativeSize", paint.fontMetricsInt) }
    runCatching { XposedHelpers.callMethod(span, "setScale", EDIT_ICON_SCALE, EDIT_ICON_SCALE) }
    runCatching { XposedHelpers.callMethod(span, "setTopOffset", EDIT_ICON_TOP_OFFSET) }

    return span
}

private fun estimateIconWidth(paint: Paint): Float {
    val fontMetrics = paint.fontMetricsInt
    val base = abs(fontMetrics.ascent) + abs(fontMetrics.descent)
    return base * EDIT_ICON_SCALE
}

private fun shrinkTimeWidths(cell: Any, editedLabelWidth: Float, replacementWidth: Float) {
    val delta = (editedLabelWidth - replacementWidth).roundToInt()
    if (delta <= 0) {
        return
    }

    runCatching {
        val current = XposedHelpers.getIntField(cell, "timeTextWidth")
        XposedHelpers.setIntField(cell, "timeTextWidth", (current - delta).coerceAtLeast(0))
    }
    runCatching {
        val current = XposedHelpers.getIntField(cell, "timeWidth")
        XposedHelpers.setIntField(cell, "timeWidth", (current - delta).coerceAtLeast(0))
    }
}
