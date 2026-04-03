package aether.killergram.neo.hooks

import aether.killergram.neo.log
import android.content.Context
import android.content.SharedPreferences
import android.content.res.XModuleResources
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField
import de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField
import java.util.concurrent.ConcurrentHashMap

private val emoticonByName = ConcurrentHashMap<String, String>()
private val emoticonById = ConcurrentHashMap<Int, String>()
private var iconCache: SharedPreferences? = null

private fun getCache(context: Context): SharedPreferences {
    return iconCache ?: context.getSharedPreferences("neo_folder_icons", Context.MODE_PRIVATE).also {
        iconCache = it
    }
}

private fun loadCacheIntoMaps(prefs: SharedPreferences) {
    for ((key, value) in prefs.all) {
        val emoticon = value as? String ?: continue
        if (key.startsWith("id_")) {
            val id = key.removePrefix("id_").toIntOrNull() ?: continue
            emoticonById.putIfAbsent(id, emoticon)
        } else if (key.startsWith("name_")) {
            val name = key.removePrefix("name_")
            emoticonByName.putIfAbsent(name, emoticon)
        }
    }
}

private fun persistToCache(prefs: SharedPreferences) {
    prefs.edit().apply {
        clear()
        for ((id, emoticon) in emoticonById) put("id_$id", emoticon)
        for ((name, emoticon) in emoticonByName) put("name_$name", emoticon)
        apply()
    }
}

private fun SharedPreferences.Editor.put(key: String, value: String): SharedPreferences.Editor {
    putString(key, value)
    return this
}

fun Hooks.folderIcons(moduleResources: XModuleResources, displayMode: String) {
    log("Enabling folder icons (display mode: $displayMode)...")

    val messagesStorageClass = loadClass("org.telegram.messenger.MessagesStorage") ?: return
    val filterCreateActivityClass = loadClass("org.telegram.ui.FilterCreateActivity") ?: return
    val filterTabsViewClass = loadClass("org.telegram.ui.Components.FilterTabsView") ?: return
    val tabClass = loadClass("org.telegram.ui.Components.FilterTabsView\$Tab") ?: return
    val tabViewClass = loadClass("org.telegram.ui.Components.FilterTabsView\$TabView") ?: return
    val tlDialogFilterClass = loadClass("org.telegram.tgnet.TLRPC\$TL_dialogFilter") ?: return
    val tlDialogFilterChatlistClass = loadClass("org.telegram.tgnet.TLRPC\$TL_dialogFilterChatlist")

    // Load cached emoticons from disk on first Application context access
    val appClass = loadClass("org.telegram.messenger.ApplicationLoader") ?: return
    XposedBridge.hookAllMethods(appClass, "onCreate", object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            runCatching {
                val app = param.thisObject as? android.app.Application ?: return
                val prefs = getCache(app)
                loadCacheIntoMaps(prefs)
                log("Loaded ${emoticonById.size} cached folder emoticons", "DEBUG")
            }
        }
    })

    // ──────────────────────────────────────────────
    // Hook 1: Capture emoticons from server sync
    // ──────────────────────────────────────────────
    XposedBridge.hookAllMethods(
        messagesStorageClass,
        "checkLoadedRemoteFilters",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val filters = param.args[0] as? ArrayList<*> ?: return
                var changed = false
                for (filter in filters) {
                    filter ?: continue
                    val id = runCatching { XposedHelpers.getIntField(filter, "id") }.getOrNull() ?: continue
                    val title = runCatching { XposedHelpers.getObjectField(filter, "title") }.getOrNull() ?: continue
                    val name = runCatching { XposedHelpers.getObjectField(title, "text") as? String }.getOrNull() ?: continue
                    val emoticon = runCatching { XposedHelpers.getObjectField(filter, "emoticon") as? String }.getOrNull()
                    if (emoticon != null && name.isNotEmpty()) {
                        emoticonByName[name] = emoticon
                        emoticonById[id] = emoticon
                        changed = true
                    }
                }
                if (changed) {
                    runCatching {
                        val app = android.app.AndroidAppHelper.currentApplication() ?: return
                        persistToCache(getCache(app))
                    }
                }
            }
        }
    )

    // ──────────────────────────────────────────────
    // Hook 2: Icon picker in folder editor
    // ──────────────────────────────────────────────
    XposedBridge.hookAllMethods(
        filterCreateActivityClass,
        "createView",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val activity = param.thisObject
                val rootView = param.result as? View ?: return

                rootView.post {
                    val cell = runCatching {
                        XposedHelpers.getObjectField(activity, "nameEditTextCell")
                    }.getOrNull() as? FrameLayout ?: return@post

                    val context = cell.context
                    val density = context.resources.displayMetrics.density
                    val filter = runCatching { XposedHelpers.getObjectField(activity, "filter") }.getOrNull()
                    val filterId = runCatching { XposedHelpers.getIntField(filter, "id") }.getOrNull() ?: 0
                    val currentEmoticon = emoticonById[filterId]

                    // Shift editTextEmoji right to make room
                    runCatching {
                        val etv = XposedHelpers.getObjectField(cell, "editTextEmoji") as? View ?: return@runCatching
                        val lp = etv.layoutParams as? FrameLayout.LayoutParams ?: return@runCatching
                        lp.leftMargin = (40 * density).toInt()
                        etv.layoutParams = lp
                    }

                    val iconButton = ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_INSIDE
                        val pad = (10 * density).toInt()
                        setPadding(pad, pad, pad, pad)
                        setImageDrawable(moduleResources.getDrawable(getIconResId(currentEmoticon)).mutate().apply {
                            colorFilter = PorterDuffColorFilter(0xFF8E8E93.toInt(), PorterDuff.Mode.SRC_IN)
                        })
                    }
                    cell.addView(iconButton, FrameLayout.LayoutParams(
                        (48 * density).toInt(), (48 * density).toInt(),
                        Gravity.START or Gravity.CENTER_VERTICAL
                    ))

                    setAdditionalInstanceField(activity, "neo_emoticon", currentEmoticon)

                    iconButton.setOnClickListener {
                        showIconSelectorDialog(context, moduleResources) { emoticon ->
                            iconButton.setImageDrawable(moduleResources.getDrawable(getIconResId(emoticon)).mutate().apply {
                                colorFilter = PorterDuffColorFilter(0xFF8E8E93.toInt(), PorterDuff.Mode.SRC_IN)
                            })
                            setAdditionalInstanceField(activity, "neo_emoticon", emoticon)
                            emoticonById[filterId] = emoticon
                            runCatching {
                                val name = XposedHelpers.getObjectField(activity, "newFilterName")?.toString()
                                if (name != null) emoticonByName[name] = emoticon
                            }
                            // Persist immediately
                            runCatching { persistToCache(getCache(context)) }
                            runCatching { XposedHelpers.callMethod(activity, "checkDoneButton", true) }
                        }
                    }
                }
            }
        }
    )

    // ──────────────────────────────────────────────
    // Hook 3: Tab bar icon display
    // ──────────────────────────────────────────────
    if (displayMode != "text") {
        var savedName: String? = null

        // 3a: addTab — save name, clear title in icon mode, store emoticon on Tab
        XposedBridge.hookAllMethods(filterTabsViewClass, "addTab", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (param.args.size != 7) return
                savedName = param.args[2] as? String
                if (displayMode == "icon") {
                    param.args[2] = ""
                    param.args[3] = null
                }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                if (param.args.size != 7) return
                val name = savedName ?: return
                savedName = null
                val tabs = runCatching {
                    XposedHelpers.getObjectField(param.thisObject, "tabs") as? ArrayList<*>
                }.getOrNull() ?: return
                val tab = tabs.lastOrNull() ?: return
                val isDefault = param.args[5] as? Boolean ?: false
                val emoticon = if (isDefault) ALL_CHATS_EMOTICON
                               else emoticonByName[name] ?: FALLBACK_EMOTICON
                setAdditionalInstanceField(tab, "neo_emoticon", emoticon)
            }
        })

        // 3b: setTitle — force empty in icon-only mode (onMeasure re-sets titles)
        if (displayMode == "icon") {
            XposedBridge.hookAllMethods(tabClass, "setTitle", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.args[0] = ""
                    if (param.args.size > 1) param.args[1] = null
                }
            })
        }

        // 3c: getWidth — always add icon width
        XposedBridge.hookAllMethods(tabClass, "getWidth", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val outerView = runCatching {
                    XposedHelpers.getObjectField(param.thisObject, "this\$0") as? View
                }.getOrNull() ?: return
                val density = outerView.resources.displayMetrics.density
                val iconSize = (ICON_SIZE_DP * density).toInt()
                val originalResult = param.result as Int

                if (displayMode == "icon") {
                    val counter = runCatching { XposedHelpers.getIntField(param.thisObject, "counter") }.getOrDefault(0)
                    var width = iconSize
                    if (counter > 0) {
                        val tcp = runCatching {
                            XposedHelpers.getObjectField(outerView, "textCounterPaint") as android.text.TextPaint
                        }.getOrNull()
                        if (tcp != null) {
                            val cw = tcp.measureText(String.format("%d", counter)).toInt()
                            width += maxOf((7.333f * density).toInt(), cw) + (10 * density).toInt()
                        }
                    }
                    // Use dp(40) minimum to match the indicator's built-in floor in drawChild
                    param.result = maxOf((40 * density).toInt(), width)
                } else {
                    param.result = originalResult + iconSize + (3 * density).toInt()
                }
            }
        })

        // 3d: onDraw — render tinted icon + fix tabWidth for indicator
        XposedBridge.hookAllMethods(tabViewClass, "onDraw", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (displayMode != "icon") return
                val tabView = param.thisObject as? View ?: return
                val tab = runCatching { XposedHelpers.getObjectField(tabView, "currentTab") }.getOrNull() ?: return
                val density = tabView.resources.displayMetrics.density
                val iconSize = (ICON_SIZE_DP * density).toInt()
                // Temporarily set titleWidth to icon size so stock positions the
                // counter badge to the right of the icon, not overlapping it
                runCatching { XposedHelpers.setIntField(tab, "titleWidth", iconSize) }
            }

            override fun afterHookedMethod(param: MethodHookParam) {
                val tabView = param.thisObject as? View ?: return
                val canvas = param.args[0] as? Canvas ?: return
                val tab = runCatching { XposedHelpers.getObjectField(tabView, "currentTab") }.getOrNull() ?: return
                val emoticon = getAdditionalInstanceField(tab, "neo_emoticon") as? String ?: FALLBACK_EMOTICON

                val outerInstance = runCatching { XposedHelpers.getObjectField(tabView, "this\$0") }.getOrNull() ?: return
                val textPaint = runCatching {
                    XposedHelpers.getObjectField(outerInstance, "textPaint") as android.text.TextPaint
                }.getOrNull() ?: return

                val density = tabView.resources.displayMetrics.density
                val icon = loadIconDrawable(moduleResources, emoticon, density)
                icon.setTint(textPaint.color)
                val iconSize = icon.bounds.width()
                val gap = (3 * density).toInt()

                // Read stock's tabWidth (titleWidth + countPart) BEFORE we overwrite it
                val stockTabWidth = runCatching { XposedHelpers.getIntField(tabView, "tabWidth") }.getOrDefault(0)

                val iconX: Float
                val iconY = (tabView.measuredHeight - iconSize) / 2f
                var indicatorContentWidth: Int

                if (displayMode == "icon") {
                    // Stock already positioned the counter correctly (titleWidth = iconSize
                    // was set in beforeHookedMethod), so just place icon at textX
                    iconX = (tabView.measuredWidth - stockTabWidth) / 2f
                    indicatorContentWidth = stockTabWidth
                    // Restore titleWidth to avoid side effects on getWidth recalculations
                    runCatching { XposedHelpers.setIntField(tab, "titleWidth", 0) }
                } else {
                    // Mix: position icon to the LEFT of where stock drew the text
                    val textX = (tabView.measuredWidth - stockTabWidth) / 2f
                    iconX = textX - gap - iconSize
                    indicatorContentWidth = iconSize + gap + stockTabWidth
                }

                // Fix tabWidth for indicator consistency
                runCatching {
                    XposedHelpers.setIntField(tabView, "tabWidth",
                        maxOf((40 * density).toInt(), indicatorContentWidth))
                }

                canvas.save()
                canvas.translate(iconX, iconY)
                icon.draw(canvas)
                canvas.restore()
            }
        })
    }

    // ──────────────────────────────────────────────
    // Hook 4: Save emoticon to server
    // ──────────────────────────────────────────────
    val serializeHook = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            val id = runCatching { XposedHelpers.getIntField(param.thisObject, "id") }.getOrNull() ?: return
            val emoticon = emoticonById[id] ?: return
            XposedHelpers.setObjectField(param.thisObject, "emoticon", emoticon)
            val flags = XposedHelpers.getIntField(param.thisObject, "flags")
            XposedHelpers.setIntField(param.thisObject, "flags", flags or 33554432)
        }
    }
    XposedBridge.hookAllMethods(tlDialogFilterClass, "serializeToStream", serializeHook)
    tlDialogFilterChatlistClass?.let { XposedBridge.hookAllMethods(it, "serializeToStream", serializeHook) }

    // Detect emoticon changes in folder editor
    XposedBridge.hookAllMethods(filterCreateActivityClass, "hasChanges", object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            if (param.result == true) return
            if (getAdditionalInstanceField(param.thisObject, "neo_emoticon") as? String != null) {
                param.result = true
            }
        }
    })
}
