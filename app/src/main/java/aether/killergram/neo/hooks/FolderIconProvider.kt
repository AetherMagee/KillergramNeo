package aether.killergram.neo.hooks

import aether.killergram.neo.R
import android.app.AlertDialog
import android.content.Context
import android.content.res.XModuleResources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.widget.GridView
import android.widget.BaseAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlin.math.ceil

val ICON_MAP = linkedMapOf(
    "\uD83D\uDC31" to R.drawable.filter_cat,
    "\uD83D\uDCD5" to R.drawable.filter_book,
    "\uD83D\uDCB0" to R.drawable.filter_money,
    "\uD83C\uDFAE" to R.drawable.filter_game,
    "\uD83D\uDCA1" to R.drawable.filter_light,
    "\uD83D\uDC4C" to R.drawable.filter_like,
    "\uD83C\uDFB5" to R.drawable.filter_note,
    "\uD83C\uDFA8" to R.drawable.filter_palette,
    "\u2708" to R.drawable.filter_travel,
    "\u26BD" to R.drawable.filter_sport,
    "\u2B50" to R.drawable.filter_favorite,
    "\uD83C\uDF93" to R.drawable.filter_study,
    "\uD83D\uDEEB" to R.drawable.filter_airplane,
    "\uD83D\uDC64" to R.drawable.filter_private,
    "\uD83D\uDC65" to R.drawable.filter_group,
    "\uD83D\uDCAC" to R.drawable.filter_all,
    "\u2705" to R.drawable.filter_unread,
    "\uD83E\uDD16" to R.drawable.filter_bots,
    "\uD83D\uDC51" to R.drawable.filter_crown,
    "\uD83C\uDF39" to R.drawable.filter_flower,
    "\uD83C\uDFE0" to R.drawable.filter_home,
    "\u2764" to R.drawable.filter_love,
    "\uD83C\uDFAD" to R.drawable.filter_mask,
    "\uD83C\uDF78" to R.drawable.filter_party,
    "\uD83D\uDCC8" to R.drawable.filter_trade,
    "\uD83D\uDCBC" to R.drawable.filter_work,
    "\uD83D\uDD14" to R.drawable.filter_unmuted,
    "\uD83D\uDCE2" to R.drawable.filter_channels,
    "\uD83D\uDCC1" to R.drawable.filter_custom,
    "\uD83D\uDCCB" to R.drawable.filter_setup
)

const val ALL_CHATS_EMOTICON = "\uD83D\uDCAC"
const val FALLBACK_EMOTICON = "\uD83D\uDCC1"
const val ICON_SIZE_DP = 28
private const val SMALL_TAB_ICON_SCALE = 0.9f

private val drawableCache = HashMap<Pair<Int, Int>, Drawable>()

fun getTabIconSizePx(density: Float, useSmallerIcons: Boolean = false): Int {
    val scale = if (useSmallerIcons) SMALL_TAB_ICON_SCALE else 1f
    return ceil(ICON_SIZE_DP * scale * density).toInt()
}

fun getIconResId(emoticon: String?): Int {
    if (emoticon == null) return R.drawable.filter_custom
    return ICON_MAP[emoticon] ?: R.drawable.filter_custom
}

fun loadIconDrawable(
    moduleResources: XModuleResources,
    emoticon: String,
    density: Float,
    useSmallerIcons: Boolean = false
): Drawable {
    val resId = getIconResId(emoticon)
    val iconSize = getTabIconSizePx(density, useSmallerIcons)
    return drawableCache.getOrPut(resId to iconSize) {
        moduleResources.getDrawable(resId).mutate().apply {
            setBounds(0, 0, iconSize, iconSize)
        }
    }
}

fun showIconSelectorDialog(
    context: Context,
    moduleResources: XModuleResources,
    onSelect: (String) -> Unit
) {
    val emoticons = ICON_MAP.keys.toList()
    val density = context.resources.displayMetrics.density
    val iconSizePx = (40 * density).toInt()
    val paddingPx = (8 * density).toInt()

    val gridView = GridView(context).apply {
        numColumns = 6
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        adapter = object : BaseAdapter() {
            override fun getCount() = emoticons.size
            override fun getItem(position: Int) = emoticons[position]
            override fun getItemId(position: Int) = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val imageView = (convertView as? ImageView) ?: ImageView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(iconSizePx, iconSizePx)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    val pad = (6 * density).toInt()
                    setPadding(pad, pad, pad, pad)
                }
                val emoticon = emoticons[position]
                val resId = getIconResId(emoticon)
                val drawable = moduleResources.getDrawable(resId).mutate()
                drawable.colorFilter = PorterDuffColorFilter(0xFF8E8E93.toInt(), PorterDuff.Mode.SRC_IN)
                imageView.setImageDrawable(drawable)
                imageView.tag = emoticon
                return imageView
            }
        }
    }

    val dialog = AlertDialog.Builder(context)
        .setTitle("Select folder icon")
        .setView(gridView)
        .setNegativeButton("Cancel", null)
        .create()

    gridView.setOnItemClickListener { _, view, _, _ ->
        val emoticon = view.tag as? String ?: return@setOnItemClickListener
        onSelect(emoticon)
        dialog.dismiss()
    }

    dialog.show()
}
