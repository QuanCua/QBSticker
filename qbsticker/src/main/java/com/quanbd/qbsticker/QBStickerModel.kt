package com.quanbd.qbsticker

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Typeface
import androidx.annotation.FloatRange
import androidx.core.content.res.ResourcesCompat

class QBStickerModel {
    var text = ""
    var color = Color.WHITE
    var typeface: Typeface? = null
    var fontKey: String = ""
    var rotateValue = 0f
    var scaleValue = 1f
    var translationValue = PointF(0f,0f)

    constructor(context: Context) {
        this.typeface = getFontDefault(context).first
        this.fontKey = getFontDefault(context).second
    }

    constructor(context: Context, text : String, color : Int) {
        this.text = text
        this.color = color
        this.typeface = getFontDefault(context).first
        this.fontKey = getFontDefault(context).second
    }

    fun getFontDefault(context: Context): Pair<Typeface?, String> {
        val typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        return Pair(typeface,"roboto_regular")
    }
}