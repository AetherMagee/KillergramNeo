package aether.killergram.neo.hooks

import aether.killergram.neo.R
import aether.killergram.neo.log
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PatternMatcher
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.File

private data class MonetThemeSpec(
    val name: String,
    val assetName: String,
    val templateResId: Int,
    val sortIndex: Int,
    val previewBackgroundToken: String,
    val previewInToken: String,
    val previewOutToken: String,
    val isDark: Boolean
)

private val monetThemeSpecs = listOf(
    MonetThemeSpec(
        name = "Monet Light",
        assetName = "killergram_monet_light.attheme",
        templateResId = R.raw.monet_light_template,
        sortIndex = 6,
        previewBackgroundToken = "n1_10",
        previewInToken = "n1_50",
        previewOutToken = "a1_600",
        isDark = false
    ),
    MonetThemeSpec(
        name = "Monet Dark",
        assetName = "killergram_monet_dark.attheme",
        templateResId = R.raw.monet_dark_template,
        sortIndex = 7,
        previewBackgroundToken = "n1_900",
        previewInToken = "n1_800",
        previewOutToken = "a1_200",
        isDark = true
    )
)

private val monetThemeSpecsByName = monetThemeSpecs.associateBy(MonetThemeSpec::name)
private val monetThemeSpecsByAsset = monetThemeSpecs.associateBy(MonetThemeSpec::assetName)
private val monetThemeFiles = linkedMapOf<String, File>()
private val monetThemeLock = Any()

private const val overlayChangedAction = "android.intent.action.OVERLAY_CHANGED"
@Volatile
private var monetReceiverRegistered = false

fun Hooks.injectAdaptiveMonetThemes() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        log("Adaptive Monet themes require Android 12 or newer; skipping hook.", "DEBUG")
        return
    }

    val themeClass = loadClass("org.telegram.ui.ActionBar.Theme") ?: return
    val themeInfoClass = loadClass("org.telegram.ui.ActionBar.Theme\$ThemeInfo") ?: return
    val applicationLoaderClass = loadClass("org.telegram.messenger.ApplicationLoader") ?: return
    val launchActivityClass = loadClass("org.telegram.ui.LaunchActivity")
    val themeActivityClass = loadClass("org.telegram.ui.ThemeActivity")
    val themesHorizontalListCellClass = loadClass("org.telegram.ui.Cells.ThemesHorizontalListCell")
    val defaultThemesPreviewCellClass = loadClass("org.telegram.ui.DefaultThemesPreviewCell")

    hookThemeSorting(themeClass, themeInfoClass, applicationLoaderClass)
    hookThemeAssetResolution(themeClass, applicationLoaderClass)
    hookThemeFlags(themeInfoClass)
    if (themeActivityClass != null) {
        hookThemePicker(themeActivityClass, themeClass, themeInfoClass, applicationLoaderClass)
    }
    if (themesHorizontalListCellClass != null) {
        hookThemesHorizontalListCell(themesHorizontalListCellClass, themeClass, themeInfoClass, applicationLoaderClass)
        hookMonetThemeSelection(themesHorizontalListCellClass, applicationLoaderClass)
    }
    if (defaultThemesPreviewCellClass != null) {
        hookMonetThemeSelection(defaultThemesPreviewCellClass, applicationLoaderClass)
    }
    if (launchActivityClass != null) {
        hookOverlayRefresh(launchActivityClass, themeClass, themeInfoClass, applicationLoaderClass)
    }

    getApplicationContext(applicationLoaderClass)?.let { context ->
        ensureMonetThemes(themeClass, themeInfoClass, context)
    }
}

private fun hookThemeSorting(
    themeClass: Class<*>,
    themeInfoClass: Class<*>,
    applicationLoaderClass: Class<*>
) {
    XposedBridge.hookAllMethods(
        themeClass,
        "sortThemes",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val context = getApplicationContext(applicationLoaderClass) ?: return
                ensureMonetThemes(themeClass, themeInfoClass, context)
            }
        }
    )
}

private fun hookThemeAssetResolution(
    themeClass: Class<*>,
    applicationLoaderClass: Class<*>
) {
    XposedHelpers.findAndHookMethod(
        themeClass,
        "getAssetFile",
        String::class.java,
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val assetName = param.args.firstOrNull() as? String ?: return
                val spec = monetThemeSpecsByAsset[assetName] ?: return
                val context = getApplicationContext(applicationLoaderClass) ?: return
                val themeFile = generateMonetThemeFile(context, spec) ?: return
                monetThemeFiles[assetName] = themeFile
                param.result = themeFile
            }
        }
    )
}

private fun hookMonetThemeSelection(
    selectorClass: Class<*>,
    applicationLoaderClass: Class<*>
) {
    XposedBridge.hookAllMethods(
        selectorClass,
        "selectTheme",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                prepareMonetThemeForApply(param, applicationLoaderClass)
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                restoreMonetThemeAfterApply(param)
            }
        }
    )
}

private fun prepareMonetThemeForApply(
    param: XC_MethodHook.MethodHookParam,
    applicationLoaderClass: Class<*>
) {
    val themeInfo = param.args.firstOrNull() ?: return
    val themeName = runCatching { XposedHelpers.getObjectField(themeInfo, "name") as? String }.getOrNull() ?: return
    val spec = monetThemeSpecsByName[themeName] ?: return
    val context = getApplicationContext(applicationLoaderClass) ?: return
    val themeFile = generateMonetThemeFile(context, spec) ?: return

    param.setObjectExtra("killergram_monet_original_asset", runCatching {
        XposedHelpers.getObjectField(themeInfo, "assetName")
    }.getOrNull())
    param.setObjectExtra("killergram_monet_original_path", runCatching {
        XposedHelpers.getObjectField(themeInfo, "pathToFile")
    }.getOrNull())

    XposedHelpers.setObjectField(themeInfo, "assetName", null)
    XposedHelpers.setObjectField(themeInfo, "pathToFile", themeFile.absolutePath)
}

private fun restoreMonetThemeAfterApply(
    param: XC_MethodHook.MethodHookParam
) {
    val themeInfo = param.args.firstOrNull() ?: return
    val themeName = runCatching { XposedHelpers.getObjectField(themeInfo, "name") as? String }.getOrNull() ?: return
    if (themeName !in monetThemeSpecsByName) {
        return
    }

    XposedHelpers.setObjectField(themeInfo, "assetName", param.getObjectExtra("killergram_monet_original_asset"))
    XposedHelpers.setObjectField(themeInfo, "pathToFile", param.getObjectExtra("killergram_monet_original_path"))
}

private fun hookThemeFlags(themeInfoClass: Class<*>) {
    XposedBridge.hookAllMethods(
        themeInfoClass,
        "isDark",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val name = XposedHelpers.getObjectField(param.thisObject, "name") as? String ?: return
                val spec = monetThemeSpecsByName[name] ?: return
                param.result = spec.isDark
            }
        }
    )

    XposedBridge.hookAllMethods(
        themeInfoClass,
        "isLight",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val name = XposedHelpers.getObjectField(param.thisObject, "name") as? String ?: return
                val spec = monetThemeSpecsByName[name] ?: return
                param.result = !spec.isDark
            }
        }
    )
}

private fun hookOverlayRefresh(
    launchActivityClass: Class<*>,
    themeClass: Class<*>,
    themeInfoClass: Class<*>,
    applicationLoaderClass: Class<*>
) {
    XposedBridge.hookAllMethods(
        launchActivityClass,
        "onCreate",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val context = getApplicationContext(applicationLoaderClass) ?: return
                ensureMonetThemes(themeClass, themeInfoClass, context)
                registerMonetOverlayReceiver(context, themeClass, themeInfoClass)
            }
        }
    )
}

private fun hookThemePicker(
    themeActivityClass: Class<*>,
    themeClass: Class<*>,
    themeInfoClass: Class<*>,
    applicationLoaderClass: Class<*>
) {
    XposedBridge.hookAllMethods(
        themeActivityClass,
        "updateRows",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val context = getApplicationContext(applicationLoaderClass) ?: return
                ensureMonetThemes(themeClass, themeInfoClass, context)
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                val context = getApplicationContext(applicationLoaderClass) ?: return
                ensureMonetThemes(themeClass, themeInfoClass, context)
                injectThemesIntoPicker(param.thisObject, themeClass)
            }
        }
    )
}

private fun hookThemesHorizontalListCell(
    themesHorizontalListCellClass: Class<*>,
    themeClass: Class<*>,
    themeInfoClass: Class<*>,
    applicationLoaderClass: Class<*>
) {
    XposedBridge.hookAllConstructors(
        themesHorizontalListCellClass,
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val defaultThemes = param.args.getOrNull(3) as? MutableList<Any> ?: return
                val context = getApplicationContext(applicationLoaderClass) ?: return
                ensureMonetThemes(themeClass, themeInfoClass, context)
                val changed = appendMonetThemes(defaultThemes, themeClass)
                if (changed) {
                    defaultThemes.sortBy { themeInfo ->
                        runCatching { XposedHelpers.getIntField(themeInfo, "sortIndex") }.getOrDefault(Int.MAX_VALUE)
                    }
                }
            }
        }
    )
}

@Suppress("UNCHECKED_CAST")
private fun injectThemesIntoPicker(
    themeActivity: Any?,
    themeClass: Class<*>
) {
    if (themeActivity == null) {
        return
    }

    val defaultThemes = runCatching {
        XposedHelpers.getObjectField(themeActivity, "defaultThemes") as? MutableList<Any>
    }.getOrNull() ?: return
    val themesDict = runCatching {
        XposedHelpers.getStaticObjectField(themeClass, "themesDict") as? Map<String, Any>
    }.getOrNull() ?: return

    val changed = appendMonetThemes(defaultThemes, themesDict)

    if (!changed) {
        return
    }

    defaultThemes.sortBy { themeInfo ->
        runCatching { XposedHelpers.getIntField(themeInfo, "sortIndex") }.getOrDefault(Int.MAX_VALUE)
    }

    val listView = runCatching {
        XposedHelpers.getObjectField(themeActivity, "listView")
    }.getOrNull()
    val themesHorizontalListCell = runCatching {
        XposedHelpers.getObjectField(themeActivity, "themesHorizontalListCell")
    }.getOrNull()
    val listAdapter = runCatching {
        XposedHelpers.getObjectField(themeActivity, "listAdapter")
    }.getOrNull()

    runCatching {
        if (themesHorizontalListCell != null && listView != null) {
            val width = XposedHelpers.callMethod(listView, "getWidth") as? Int ?: 0
            XposedHelpers.callMethod(themesHorizontalListCell, "notifyDataSetChanged", width)
        }
    }

    runCatching {
        if (listAdapter != null) {
            XposedHelpers.callMethod(listAdapter, "notifyDataSetChanged")
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun appendMonetThemes(
    themes: MutableList<Any>,
    themeClass: Class<*>
): Boolean {
    val themesDict = runCatching {
        XposedHelpers.getStaticObjectField(themeClass, "themesDict") as? Map<String, Any>
    }.getOrNull() ?: return false
    return appendMonetThemes(themes, themesDict)
}

private fun appendMonetThemes(
    themes: MutableList<Any>,
    themesDict: Map<String, Any>
): Boolean {
    var changed = false
    monetThemeSpecs.forEach { spec ->
        val themeInfo = themesDict[spec.name] ?: return@forEach
        if (themes.none { existing ->
                runCatching { XposedHelpers.getObjectField(existing, "name") as? String }.getOrNull() == spec.name
            }
        ) {
            themes.add(themeInfo)
            changed = true
        }
    }
    return changed
}

private fun registerMonetOverlayReceiver(
    context: Context,
    themeClass: Class<*>,
    themeInfoClass: Class<*>
) {
    if (monetReceiverRegistered) {
        return
    }

    synchronized(monetThemeLock) {
        if (monetReceiverRegistered) {
            return
        }

        val appContext = context.applicationContext ?: context
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent?.action != overlayChangedAction) {
                    return
                }

                ensureMonetThemes(themeClass, themeInfoClass, appContext)

                val activeTheme = runCatching {
                    XposedHelpers.callStaticMethod(themeClass, "getActiveTheme")
                }.getOrNull() ?: return

                val themeName = runCatching {
                    XposedHelpers.getObjectField(activeTheme, "name") as? String
                }.getOrNull() ?: return

                if (themeName in monetThemeSpecsByName) {
                    runCatching {
                        XposedHelpers.callStaticMethod(themeClass, "applyTheme", activeTheme)
                    }.onFailure { error ->
                        log("Failed to refresh active Monet theme: ${error.message}", "ERROR")
                    }
                }
            }
        }

        val filter = IntentFilter(overlayChangedAction).apply {
            addDataScheme("package")
            addDataSchemeSpecificPart("android", PatternMatcher.PATTERN_LITERAL)
        }

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                appContext.registerReceiver(receiver, filter)
            }
            monetReceiverRegistered = true
        }.onFailure { error ->
            log("Failed to register Monet overlay receiver: ${error.message}", "ERROR")
        }
    }
}

private fun ensureMonetThemes(
    themeClass: Class<*>,
    themeInfoClass: Class<*>,
    context: Context
) {
    synchronized(monetThemeLock) {
        val themes = runCatching {
            @Suppress("UNCHECKED_CAST")
            XposedHelpers.getStaticObjectField(themeClass, "themes") as? MutableList<Any>
        }.getOrNull() ?: return
        val themesDict = runCatching {
            @Suppress("UNCHECKED_CAST")
            XposedHelpers.getStaticObjectField(themeClass, "themesDict") as? MutableMap<String, Any>
        }.getOrNull() ?: return

        monetThemeSpecs.forEach { spec ->
            val themeFile = generateMonetThemeFile(context, spec) ?: return@forEach
            monetThemeFiles[spec.assetName] = themeFile

            val existingTheme = themesDict[spec.name]?.takeIf(themeInfoClass::isInstance)
            val themeInfo = existingTheme ?: XposedHelpers.newInstance(themeInfoClass)

            updateMonetThemeInfo(themeInfo, spec, context)

            themes.removeAll { item ->
                item !== themeInfo &&
                    themeInfoClass.isInstance(item) &&
                    (runCatching { XposedHelpers.getObjectField(item, "name") as? String }.getOrNull() == spec.name)
            }

            if (!themes.contains(themeInfo)) {
                themes.add(themeInfo)
            }
            themesDict[spec.name] = themeInfo
        }
    }
}

private fun updateMonetThemeInfo(
    themeInfo: Any,
    spec: MonetThemeSpec,
    context: Context
) {
    XposedHelpers.setObjectField(themeInfo, "name", spec.name)
    XposedHelpers.setObjectField(themeInfo, "pathToFile", null)
    XposedHelpers.setObjectField(themeInfo, "pathToWallpaper", null)
    XposedHelpers.setObjectField(themeInfo, "assetName", spec.assetName)
    XposedHelpers.setObjectField(themeInfo, "info", null)
    XposedHelpers.setBooleanField(themeInfo, "loaded", true)
    XposedHelpers.setBooleanField(themeInfo, "themeLoaded", true)
    XposedHelpers.setBooleanField(themeInfo, "previewParsed", false)
    XposedHelpers.setIntField(themeInfo, "sortIndex", spec.sortIndex)

    resolveMonetToken(context, spec.previewBackgroundToken)?.let { color ->
        runCatching { XposedHelpers.callMethod(themeInfo, "setPreviewBackgroundColor", color) }
    }
    resolveMonetToken(context, spec.previewInToken)?.let { color ->
        runCatching { XposedHelpers.callMethod(themeInfo, "setPreviewInColor", color) }
    }
    resolveMonetToken(context, spec.previewOutToken)?.let { color ->
        runCatching { XposedHelpers.callMethod(themeInfo, "setPreviewOutColor", color) }
    }
}

private fun generateMonetThemeFile(
    context: Context,
    spec: MonetThemeSpec
): File? {
    val template = when (spec.templateResId) {
        R.raw.monet_light_template -> MonetThemeTemplates.LIGHT
        R.raw.monet_dark_template -> MonetThemeTemplates.DARK
        else -> return null
    }

    val resolvedTheme = buildString(template.length + 256) {
        template.lineSequence().forEach { line ->
            append(resolveMonetTemplateLine(context, line))
            append('\n')
        }
    }

    val outputFile = File(context.filesDir, spec.assetName)
    if (!outputFile.exists() || outputFile.readText() != resolvedTheme) {
        outputFile.writeText(resolvedTheme)
    }
    return outputFile
}

private fun resolveMonetTemplateLine(
    context: Context,
    line: String
): String {
    val separatorIndex = line.indexOf('=')
    if (separatorIndex <= 0) {
        return line
    }

    val rawValue = line.substring(separatorIndex + 1).trim()
    if (rawValue.isEmpty() || rawValue.startsWith("#") || rawValue.first().isDigit() || rawValue.first() == '-') {
        return line
    }

    val resolved = resolveMonetToken(context, rawValue) ?: return line
    return "${line.substring(0, separatorIndex + 1)}$resolved"
}

private fun resolveMonetToken(
    context: Context,
    token: String
): Int? {
    val normalizedToken = token.trim()
    if (normalizedToken.isEmpty()) {
        return null
    }

    val alphaStart = normalizedToken.indexOf('(')
    val alphaEnd = normalizedToken.indexOf(')')
    val alphaPercent = if (alphaStart > 0 && alphaEnd > alphaStart) {
        normalizedToken.substring(alphaStart + 1, alphaEnd).toIntOrNull()
    } else {
        null
    }
    val baseToken = if (alphaStart > 0) normalizedToken.substring(0, alphaStart) else normalizedToken

    val baseColor = when (baseToken) {
        "mBlack" -> 0xFF000000.toInt()
        "mWhite" -> 0xFFFFFFFF.toInt()
        "mRed200" -> 0xFFEF9A9A.toInt()
        "mRed500" -> 0xFFF44336.toInt()
        "mGreen500" -> 0xFF4CAF50.toInt()
        else -> monetTokenResourceMap[baseToken]?.let { context.getColorOnApplication(it) }
    } ?: return null

    if (alphaPercent == null) {
        return baseColor
    }

    val clampedAlpha = alphaPercent.coerceIn(0, 100) * 255 / 100
    return (baseColor and 0x00FFFFFF) or (clampedAlpha shl 24)
}

private fun Context.getColorOnApplication(resId: Int): Int {
    return (applicationContext ?: this).getColor(resId)
}

private fun getApplicationContext(applicationLoaderClass: Class<*>): Context? {
    return runCatching {
        XposedHelpers.getStaticObjectField(applicationLoaderClass, "applicationContext") as? Context
    }.getOrNull()
}

private val monetTokenResourceMap = mapOf(
    "a1_10" to android.R.color.system_accent1_10,
    "a1_50" to android.R.color.system_accent1_50,
    "a1_100" to android.R.color.system_accent1_100,
    "a1_200" to android.R.color.system_accent1_200,
    "a1_300" to android.R.color.system_accent1_300,
    "a1_400" to android.R.color.system_accent1_400,
    "a1_500" to android.R.color.system_accent1_500,
    "a1_600" to android.R.color.system_accent1_600,
    "a1_700" to android.R.color.system_accent1_700,
    "a1_800" to android.R.color.system_accent1_800,
    "a1_900" to android.R.color.system_accent1_900,
    "a2_50" to android.R.color.system_accent2_50,
    "a2_100" to android.R.color.system_accent2_100,
    "a2_200" to android.R.color.system_accent2_200,
    "a2_300" to android.R.color.system_accent2_300,
    "a2_400" to android.R.color.system_accent2_400,
    "a2_500" to android.R.color.system_accent2_500,
    "a2_600" to android.R.color.system_accent2_600,
    "a2_700" to android.R.color.system_accent2_700,
    "a2_800" to android.R.color.system_accent2_800,
    "a2_900" to android.R.color.system_accent2_900,
    "a3_50" to android.R.color.system_accent3_50,
    "a3_100" to android.R.color.system_accent3_100,
    "a3_200" to android.R.color.system_accent3_200,
    "a3_300" to android.R.color.system_accent3_300,
    "a3_400" to android.R.color.system_accent3_400,
    "a3_500" to android.R.color.system_accent3_500,
    "a3_600" to android.R.color.system_accent3_600,
    "a3_700" to android.R.color.system_accent3_700,
    "a3_800" to android.R.color.system_accent3_800,
    "a3_900" to android.R.color.system_accent3_900,
    "n1_10" to android.R.color.system_neutral1_10,
    "n1_50" to android.R.color.system_neutral1_50,
    "n1_100" to android.R.color.system_neutral1_100,
    "n1_200" to android.R.color.system_neutral1_200,
    "n1_300" to android.R.color.system_neutral1_300,
    "n1_400" to android.R.color.system_neutral1_400,
    "n1_500" to android.R.color.system_neutral1_500,
    "n1_600" to android.R.color.system_neutral1_600,
    "n1_700" to android.R.color.system_neutral1_700,
    "n1_800" to android.R.color.system_neutral1_800,
    "n1_900" to android.R.color.system_neutral1_900,
    "n2_50" to android.R.color.system_neutral2_50,
    "n2_100" to android.R.color.system_neutral2_100,
    "n2_200" to android.R.color.system_neutral2_200,
    "n2_300" to android.R.color.system_neutral2_300,
    "n2_400" to android.R.color.system_neutral2_400,
    "n2_500" to android.R.color.system_neutral2_500,
    "n2_600" to android.R.color.system_neutral2_600,
    "n2_700" to android.R.color.system_neutral2_700,
    "n2_800" to android.R.color.system_neutral2_800,
    "n2_900" to android.R.color.system_neutral2_900
)
