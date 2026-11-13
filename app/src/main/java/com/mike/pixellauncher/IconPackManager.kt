package com.mike.pixellauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

data class IconPackInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap?
)

class IconPackManager(private val context: Context) {

    private val pm: PackageManager = context.packageManager
    private var currentPackPackage: String? = null
    private var packResources: Resources? = null
    private val componentDrawableMap = mutableMapOf<String, String>()

    private val iconPackActions = listOf(
        "org.adw.launcher.THEMES",
        "com.novalauncher.THEME",
        "com.gau.go.launcherex.theme",
        "com.anddoes.launcher.THEME",
        "app.lawnchair.icons.THEMED_ICON"
    )

    fun getAvailableIconPacks(): List<IconPackInfo> {
        val discovered = LinkedHashMap<String, IconPackInfo>()
        for (action in iconPackActions) {
            val resolveInfos = pm.queryIntentActivities(Intent(action), PackageManager.GET_META_DATA)
            for (info in resolveInfos) {
                val pkg = info.activityInfo.packageName
                if (!discovered.containsKey(pkg)) {
                    val label = info.loadLabel(pm).toString()
                    val iconBitmap = drawableToBitmap(info.loadIcon(pm), 128)
                    discovered[pkg] = IconPackInfo(pkg, label, iconBitmap)
                }
            }
        }
        return discovered.values.sortedBy { it.label.lowercase() }
    }

    fun loadIconPack(packageName: String?) {
        componentDrawableMap.clear()
        currentPackPackage = packageName
        if (packageName.isNullOrBlank()) {
            packResources = null
            return
        }
        try {
            val res = pm.getResourcesForApplication(packageName)
            packResources = res
            val xmlId = res.getIdentifier("appfilter", "xml", packageName)
            if (xmlId != 0) {
                parseAppFilter(res.getXml(xmlId))
            } else {
                res.assets.open("appfilter.xml").use { stream ->
                    val parser = XmlPullParserFactory.newInstance().newPullParser()
                    parser.setInput(stream, "UTF-8")
                    parseAppFilter(parser)
                }
            }
        } catch (_: Exception) {
            packResources = null
        }
    }

    private fun parseAppFilter(parser: XmlPullParser) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                val component = parser.getAttributeValue(null, "component")
                val drawable = parser.getAttributeValue(null, "drawable")
                if (!component.isNullOrBlank() && !drawable.isNullOrBlank()) {
                    componentDrawableMap[component] = drawable
                }
            }
            eventType = parser.next()
        }
    }

    fun getCustomIconForComponent(componentName: ComponentName, sizePx: Int = 160): Bitmap? {
        val pkg = currentPackPackage ?: return null
        val res = packResources ?: return null
        val key = "ComponentInfo{${componentName.packageName}/${componentName.className}}"
        val drawableName = componentDrawableMap[key] ?: return null
        val drawableId = res.getIdentifier(drawableName, "drawable", pkg)
        if (drawableId == 0) return null
        return try {
            drawableToBitmap(res.getDrawable(drawableId, null), sizePx)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun drawableToBitmap(drawable: Drawable?, sizePx: Int = 160): Bitmap? {
            if (drawable == null) return null
            if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
            val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else sizePx
            val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else sizePx
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }
    }
}
