package com.quanbd.qbsticker

interface QBStickerViewListener {
    fun onEntitySelected(qbTextView: QBTextView?)
    fun onEntityDeleted(id : String)
    fun onUpdateEntityModel(currentModel : QBStickerModel)
}