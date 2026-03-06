package aether.killergram.neo.hooks

import aether.killergram.neo.log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

fun Hooks.unlimitedRecents(recentStickersLimit: Int, recentEmojiLimit: Int) {
    log("Overriding recent stickers limit to $recentStickersLimit, recent emoji limit to $recentEmojiLimit...")

    // Override maxRecentStickersCount on MessagesController
    val messagesControllerClass = loadClass("org.telegram.messenger.MessagesController") ?: return

    XposedBridge.hookAllMethods(
        messagesControllerClass,
        "updateConfig",
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                param ?: return
                XposedHelpers.setIntField(param.thisObject, "maxRecentStickersCount", recentStickersLimit)
            }
        }
    )

    XposedBridge.hookAllConstructors(
        messagesControllerClass,
        object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                param ?: return
                XposedHelpers.setIntField(param.thisObject, "maxRecentStickersCount", recentStickersLimit)
            }
        }
    )

    // Override MAX_RECENT_EMOJI_COUNT in Emoji class by replacing addRecentEmoji and sortEmoji
    val emojiClass = loadClass("org.telegram.messenger.Emoji") ?: return

    XposedBridge.hookAllMethods(
        emojiClass,
        "addRecentEmoji",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                param ?: return
                val code = param.args[0] as String
                @Suppress("UNCHECKED_CAST")
                val emojiUseHistory = XposedHelpers.getStaticObjectField(emojiClass, "emojiUseHistory") as HashMap<String, Int>
                val count = emojiUseHistory[code] ?: 0

                if (count == 0 && emojiUseHistory.size >= recentEmojiLimit) {
                    @Suppress("UNCHECKED_CAST")
                    val recentEmoji = XposedHelpers.getStaticObjectField(emojiClass, "recentEmoji") as ArrayList<String>
                    if (recentEmoji.isNotEmpty()) {
                        val emoji = recentEmoji[recentEmoji.size - 1]
                        emojiUseHistory.remove(emoji)
                        recentEmoji[recentEmoji.size - 1] = code
                    }
                }
                emojiUseHistory[code] = count + 1
                param.result = null
            }
        }
    )

    XposedBridge.hookAllMethods(
        emojiClass,
        "sortEmoji",
        object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                param ?: return
                @Suppress("UNCHECKED_CAST")
                val recentEmoji = XposedHelpers.getStaticObjectField(emojiClass, "recentEmoji") as ArrayList<String>
                @Suppress("UNCHECKED_CAST")
                val emojiUseHistory = XposedHelpers.getStaticObjectField(emojiClass, "emojiUseHistory") as HashMap<String, Int>

                recentEmoji.sortWith { lhs, rhs ->
                    val count1 = emojiUseHistory[lhs] ?: 0
                    val count2 = emojiUseHistory[rhs] ?: 0
                    count2.compareTo(count1)
                }
                while (recentEmoji.size > recentEmojiLimit) {
                    recentEmoji.removeAt(recentEmoji.size - 1)
                }
                param.result = null
            }
        }
    )
}
