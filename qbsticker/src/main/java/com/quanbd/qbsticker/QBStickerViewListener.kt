package com.quanbd.qbsticker

interface QBStickerViewListener {
    fun onEntitySelected(qbSticker: QBStickerModel?)
    fun onCurrentStickerDeleted()
    fun onActionUp()
    fun updateTextStickerData(qbTextView: QBTextView)
}