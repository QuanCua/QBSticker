package com.quanbd.libsticker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.quanbd.qbsticker.QBStickerManager
import com.quanbd.qbsticker.QBStickerModel
import com.quanbd.qbsticker.QBStickerView

class MainActivity : AppCompatActivity() {
    private lateinit var qbStickerView: QBStickerView
    private lateinit var btnAddText: Button
    private lateinit var edtInputText: EditText
    private val qbStickerManager = QBStickerManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qbStickerView = findViewById<QBStickerView>(R.id.qbStickerView)
        btnAddText = findViewById<Button>(R.id.btnAddText)
        edtInputText = findViewById<EditText>(R.id.edtInputText)
        qbStickerManager.setComponent(this, qbStickerView)

        onListener()
    }

    private fun onListener() {
        btnAddText.setOnClickListener {
            val qbStickerModel = QBStickerModel(this)
            qbStickerManager.addText(qbStickerModel)
        }

        edtInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                qbStickerManager.getTextSelected()?.let {
                    it.text = p0
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }
}