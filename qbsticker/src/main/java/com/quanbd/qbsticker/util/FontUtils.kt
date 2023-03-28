package com.quanbd.qbsticker.util

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.quanbd.qbsticker.R

object FontUtils {
    const val DEFAULT_FONT = "roboto_regular"
    private var fontHashMap = HashMap<String, Typeface>()

    fun initFontDefault(context: Context) {
        val typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        if (typeface != null)
            fontHashMap[DEFAULT_FONT] = typeface
    }

    fun getFontDefault(context: Context): Typeface? {
        return ResourcesCompat.getFont(context, R.font.roboto_regular)
    }

    fun getFontDefault(): Typeface? {
        return fontHashMap[DEFAULT_FONT]
    }

    fun getListFont(): ArrayList<Pair<String, Typeface>> {
        val listOutput = arrayListOf<Pair<String, Typeface>>()
        for ((key, value) in fontHashMap.entries) {
            listOutput.add(Pair(key, value))
        }
        return listOutput
    }

    fun getFontByKey(fontKey: String): Typeface? {
        return fontHashMap[fontKey]
    }

    fun addFont(key: String, value: Typeface) {
        fontHashMap[key] = value
    }
}