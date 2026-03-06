package aether.killergram.neo.hooks

import aether.killergram.neo.PreferencesUtils
import aether.killergram.neo.core.HamburgerMenuItems
import aether.killergram.neo.core.PreferenceKeys
import aether.killergram.neo.log
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

private class HamburgerMenuHookState(
    val hiddenItems: Set<String>
) {
    private val buildDepth = ThreadLocal.withInitial { 0 }

    fun enterMenuBuild() {
        buildDepth.set(currentDepth() + 1)
    }

    fun exitMenuBuild() {
        buildDepth.set((currentDepth() - 1).coerceAtLeast(0))
    }

    fun isBuildingMenu(): Boolean = currentDepth() > 0

    private fun currentDepth(): Int = buildDepth.get() ?: 0
}

private val legacyDrawerItemIds = mapOf(
    16 to "my_profile",
    15 to "change_status",
    2 to "new_group",
    6 to "contacts",
    10 to "calls",
    11 to "saved_messages",
    8 to "settings",
    7 to "invite_friends",
    13 to "telegram_features",
)

fun Hooks.hideHamburgerMenuButtons() {
    val hiddenItems = PreferencesUtils()
        .getPrefsInstance()
        .getStringSet(PreferenceKeys.HIDDEN_HAMBURGER_MENU_ITEMS, emptySet())
        ?.map(String::trim)
        ?.filter(String::isNotEmpty)
        ?.toSet()
        .orEmpty()

    if (hiddenItems.isEmpty()) {
        log("Hamburger menu customization enabled without hidden items; skipping hook.", "DEBUG")
        return
    }

    log("Customizing hamburger menu buttons: ${hiddenItems.joinToString()}", "DEBUG")

    telegramLocaleControllerClass = loadClass("org.telegram.messenger.LocaleController")

    val dialogsActivityClass = loadClass("org.telegram.ui.DialogsActivity")
    val itemOptionsClass = loadClass("org.telegram.ui.Components.ItemOptions")
    if (dialogsActivityClass != null && itemOptionsClass != null) {
        hookItemOptionsMenu(dialogsActivityClass, itemOptionsClass, HamburgerMenuHookState(hiddenItems))
    }

    val drawerLayoutAdapterClass = loadClass("org.telegram.ui.Adapters.DrawerLayoutAdapter")
    if (drawerLayoutAdapterClass != null) {
        hookLegacyDrawerAdapter(drawerLayoutAdapterClass, hiddenItems)
    }
}

private fun hookLegacyDrawerAdapter(
    drawerLayoutAdapterClass: Class<*>,
    hiddenItems: Set<String>
) {
    val filterHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            filterLegacyDrawerItems(param.thisObject, hiddenItems)
        }
    }

    XposedBridge.hookAllConstructors(drawerLayoutAdapterClass, filterHook)
    XposedBridge.hookAllMethods(drawerLayoutAdapterClass, "resetItems", filterHook)
}

@Suppress("UNCHECKED_CAST")
private fun filterLegacyDrawerItems(
    adapter: Any?,
    hiddenItems: Set<String>
) {
    if (adapter == null) {
        return
    }

    val items = runCatching {
        XposedHelpers.getObjectField(adapter, "items") as? ArrayList<Any?>
    }.getOrNull() ?: return

    val context = runCatching {
        XposedHelpers.getObjectField(adapter, "mContext") as? Context
    }.getOrNull()
    val hiddenTitles = context?.let { resolveMenuTitles(it, hiddenItems) }.orEmpty()

    val filteredItems = ArrayList<Any?>(items.size)
    items.forEach { item ->
        if (!shouldHideLegacyDrawerItem(item, hiddenItems, hiddenTitles)) {
            filteredItems += item
        }
    }

    val normalizedItems = collapseDividers(filteredItems)
    if (items.size == normalizedItems.size && items.zip(normalizedItems).all { (left, right) -> left === right }) {
        return
    }

    items.clear()
    items.addAll(normalizedItems)
}

private fun shouldHideLegacyDrawerItem(
    item: Any?,
    hiddenItems: Set<String>,
    hiddenTitles: Set<String>
): Boolean {
    if (item == null) {
        return false
    }

    val itemId = runCatching {
        XposedHelpers.getIntField(item, "id")
    }.getOrDefault(Int.MIN_VALUE)

    if (legacyDrawerItemIds[itemId]?.let(hiddenItems::contains) == true) {
        return true
    }

    val itemText = runCatching {
        XposedHelpers.getObjectField(item, "text") as? CharSequence
    }.getOrNull()
    if (itemText != null && matchesMenuTitle(itemText, hiddenTitles)) {
        return true
    }

    val bot = runCatching {
        XposedHelpers.getObjectField(item, "bot")
    }.getOrNull() ?: return false

    val botTitle = runCatching {
        XposedHelpers.getObjectField(bot, "short_name") as? CharSequence
    }.getOrNull()
        ?: runCatching {
            XposedHelpers.getObjectField(bot, "bot_name") as? CharSequence
        }.getOrNull()

    return botTitle != null && matchesMenuTitle(botTitle, hiddenTitles)
}

private fun collapseDividers(items: List<Any?>): List<Any?> {
    val normalizedItems = ArrayList<Any?>(items.size)
    var previousWasDivider = true

    items.forEach { item ->
        val isDivider = item == null
        if (isDivider) {
            if (!previousWasDivider) {
                normalizedItems += null
            }
        } else {
            normalizedItems += item
        }
        previousWasDivider = isDivider
    }

    while (normalizedItems.lastOrNull() == null) {
        normalizedItems.removeAt(normalizedItems.lastIndex)
    }

    return normalizedItems
}

private fun hookItemOptionsMenu(
    dialogsActivityClass: Class<*>,
    itemOptionsClass: Class<*>,
    hookState: HamburgerMenuHookState
) {
    XposedBridge.hookAllMethods(
        dialogsActivityClass,
        "showItemOptions",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                hookState.enterMenuBuild()
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                hookState.exitMenuBuild()
            }
        }
    )

    val itemFilterHook = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            if (!hookState.isBuildingMenu()) {
                return
            }

            val itemText = extractMenuItemText(param.args) ?: return
            val context = resolveItemOptionsContext(param.thisObject) ?: return
            if (shouldHideMenuText(itemText, context, hookState.hiddenItems)) {
                param.result = param.thisObject
            }
        }
    }

    XposedBridge.hookAllMethods(itemOptionsClass, "add", itemFilterHook)
    XposedBridge.hookAllMethods(itemOptionsClass, "addIfNotNull", itemFilterHook)

    XposedBridge.hookAllMethods(
        itemOptionsClass,
        "show",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!hookState.isBuildingMenu()) {
                    return
                }
                pruneGapViews(param.thisObject)
            }
        }
    )
}

private fun resolveItemOptionsContext(itemOptions: Any?): Context? {
    if (itemOptions == null) return null
    return runCatching {
        XposedHelpers.callMethod(itemOptions, "getContext") as? Context
    }.getOrNull()
}

private fun extractMenuItemText(args: Array<out Any?>): CharSequence? {
    if (args.isEmpty()) {
        return null
    }

    val directText = args.firstNotNullOfOrNull { it as? CharSequence }
    if (directText != null) {
        return directText
    }

    return args.firstNotNullOfOrNull { argument ->
        extractTextFromObject(argument)
    }
}

private fun extractTextFromObject(target: Any?): CharSequence? {
    if (target == null) {
        return null
    }

    listOf("text", "title", "label", "short_name", "bot_name").forEach { fieldName ->
        val value = runCatching {
            XposedHelpers.getObjectField(target, fieldName) as? CharSequence
        }.getOrNull()
        if (!value.isNullOrBlank()) {
            return value
        }
    }

    val textView = runCatching {
        XposedHelpers.getObjectField(target, "textView") as? TextView
    }.getOrNull()

    return textView?.text
}

private fun shouldHideMenuText(
    itemText: CharSequence,
    context: Context,
    hiddenItems: Set<String>
): Boolean {
    val hiddenTitles = resolveMenuTitles(context, hiddenItems)
    return matchesMenuTitle(itemText, hiddenTitles)
}

private fun resolveMenuTitles(
    context: Context,
    itemValues: Set<String>
): Set<String> {
    val resources = context.resources
    val packageName = context.packageName
    val resolvedTitles = linkedSetOf<String>()

    itemValues.forEach { itemValue ->
        val definition = HamburgerMenuItems.byValue[itemValue] ?: return@forEach
        resolvedTitles += definition.fallbackTitle.trim()

        definition.resourceNames.forEach { resourceName ->
            val resourceId = resources.getIdentifier(resourceName, "string", packageName)
            if (resourceId != 0) {
                resolveTelegramString(resourceName, resourceId, resources.getString(resourceId))
                    ?.trim()
                    ?.takeIf(String::isNotEmpty)
                    ?.let(resolvedTitles::add)
            }
        }
    }

    return resolvedTitles
}

private fun matchesMenuTitle(
    text: CharSequence?,
    candidateTitles: Set<String>
): Boolean {
    val normalizedText = normalizeMenuText(text)
    if (normalizedText.isEmpty()) {
        return false
    }

    return candidateTitles.any { candidate ->
        val normalizedCandidate = normalizeMenuText(candidate)
        normalizedText == normalizedCandidate ||
            (normalizedText.startsWith("$normalizedCandidate ") &&
                normalizedText.removePrefix("$normalizedCandidate ").length <= 8)
    }
}

private fun normalizeMenuText(text: CharSequence?): String {
    return text
        ?.toString()
        ?.replace('\u00A0', ' ')
        ?.replace(Regex("\\s+"), " ")
        ?.trim()
        .orEmpty()
}

private var telegramLocaleControllerClass: Class<*>? = null

private fun resolveTelegramString(
    resourceName: String,
    resourceId: Int,
    fallback: String
): String? {
    val localeControllerClass = telegramLocaleControllerClass ?: return fallback

    return runCatching {
        XposedHelpers.callStaticMethod(
            localeControllerClass,
            "getString",
            resourceName,
            resourceId
        ) as? String
    }.getOrNull() ?: fallback
}

private fun pruneGapViews(itemOptions: Any?) {
    if (itemOptions == null) {
        return
    }

    val layout = runCatching {
        XposedHelpers.getObjectField(itemOptions, "layout") as? ViewGroup
    }.getOrNull() ?: return

    pruneGapViewsRecursively(layout)
}

private fun pruneGapViewsRecursively(group: ViewGroup) {
    var childIndex = 0
    while (childIndex < group.childCount) {
        val child = group.getChildAt(childIndex)
        if (child is ViewGroup) {
            pruneGapViewsRecursively(child)
        }
        childIndex++
    }

    var index = 0
    var previousWasGap = true
    while (index < group.childCount) {
        val isGap = isGapView(group.getChildAt(index))
        if (isGap && previousWasGap) {
            group.removeViewAt(index)
            continue
        }
        previousWasGap = isGap
        index++
    }

    while (group.childCount > 0 && isGapView(group.getChildAt(group.childCount - 1))) {
        group.removeViewAt(group.childCount - 1)
    }
}

private fun isGapView(view: View): Boolean {
    return view.javaClass.name.endsWith("ActionBarPopupWindow\$GapView")
}
