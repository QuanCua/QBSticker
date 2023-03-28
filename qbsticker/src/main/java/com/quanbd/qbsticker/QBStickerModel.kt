package com.quanbd.qbsticker

import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Typeface
import androidx.annotation.FloatRange
import androidx.core.content.res.ResourcesCompat
import com.google.gson.Gson
import com.quanbd.qbsticker.util.FontUtils

class QBStickerModel(
    var id: String,
    var text: String,
    var fontKey : String,
    var color : String,
    var align : Int,
    var isSelected: Boolean,
    var translation: PointF,
    var rotate: Float,
    var scale: Float,
    var startTime : Long,
    var endTime : Long
) {
    companion object {
        const val MIN_SCALE = 0.1f
        const val MAX_SCALE = 5.0f
        const val ALIGN_LEFT = 0
        const val ALIGN_CENTER = 1
        const val ALIGN_RIGHT = 2
        const val ALIGN_JUSTIFIED = 3
    }

    private constructor(builder: Builder) : this(
        builder.id,
        builder.text,
        builder.fontKey,
        builder.color,
        builder.align,
        builder.isSelected,
        builder.translation,
        builder.rotate,
        builder.scale,
        builder.startTime,
        builder.endTime
    )

    class Builder {
        var id = System.nanoTime().toString()
            private set

        var text = QBTextView.DEFAULT_CONTENT
            private set

        var fontKey = FontUtils.DEFAULT_FONT
            private set

        var color = "#ffffff"
            private set

        var align = ALIGN_CENTER
            private set

        var isSelected = false
            private set

        var translation = PointF(0f, 0f)
            private set

        @FloatRange(from = 0.0, to = 360.0)
        var rotate = 0f
            private set

        var scale = 1f
            private set

        var startTime = 0L
            private set

        var endTime = 0L
            private set

        fun id(_id: String) = apply { id = _id }
        fun text(_text: String) = apply { text = _text }
        fun fontKey(_fontKey: String) = apply { fontKey = _fontKey }
        fun color(_color: String) = apply { color = _color }
        fun align(_align: Int) = apply { align = _align }
        fun isSelected(_isSelected: Boolean) = apply { isSelected = _isSelected }
        fun translation(_translation: PointF) = apply { translation = _translation }
        fun rotate(_rotate: Float) = apply { rotate = _rotate }
        fun scale(_scale: Float) = apply { scale = _scale }
        fun startTime(_startTime: Long) = apply { startTime = _startTime }
        fun endTime(_endTime: Long) = apply { endTime = _endTime }

        fun build() = QBStickerModel(this)
    }

    fun copy(): QBStickerModel {
        val gson = Gson()
        val json = gson.toJson(this@QBStickerModel)
        return gson.fromJson(json, QBStickerModel::class.java)
    }

    fun getFontDefault(context: Context): Pair<Typeface?, String> {
        val typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
        return Pair(typeface, "roboto_regular")
    }

}