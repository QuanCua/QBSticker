package com.quanbd.qbsticker

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView

class QBStickerManager {
    private lateinit var stickerView : QBStickerView
    private lateinit var context: Context

    fun setComponent(context: Context, stickerView : QBStickerView) {
        this.stickerView = stickerView
        this.context = context
    }

    fun getListText() : ArrayList<QBTextView> {
        return stickerView.getListText()
    }

    fun getTextSelected(): QBTextView? {
        return stickerView.getTextSelected()
    }

    fun addText(stickerModel : QBStickerModel) {
        val qbTextView = QBTextView(context, null)
//        qbTextView.text = stickerModel.text
        qbTextView.textSize = 40f
        qbTextView.setTextColor(stickerModel.color)
        qbTextView.typeface = stickerModel.typeface
        stickerView.addText(qbTextView)
    }
}