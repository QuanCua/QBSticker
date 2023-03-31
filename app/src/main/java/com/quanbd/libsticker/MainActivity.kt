package com.quanbd.libsticker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.quanbd.qbsticker.QBStickerModel
import com.quanbd.qbsticker.QBStickerView
import com.quanbd.qbsticker.QBStickerViewListener
import com.quanbd.qbsticker.QBTextView

class MainActivity : AppCompatActivity() {
    private lateinit var qbStickerView: QBStickerView
    private lateinit var btnAddText: Button
    private lateinit var edtInputText: EditText
    private lateinit var btnRatio_169: Button
    private lateinit var btnRatio_916: Button
    private lateinit var btnRatio_11: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qbStickerView = findViewById<QBStickerView>(R.id.qbStickerView)
        btnAddText = findViewById<Button>(R.id.btnAddText)
        edtInputText = findViewById<EditText>(R.id.edtInputText)
        btnRatio_169 = findViewById<Button>(R.id.btnRatio_169)
        btnRatio_916 = findViewById<Button>(R.id.btnRatio_916)
        btnRatio_11 = findViewById<Button>(R.id.btnRatio_11)

        onListener()
    }

    private fun onListener() {
        btnRatio_11.setOnClickListener {
            qbStickerView.updateSize(QBStickerView.widthScreen, QBStickerView.widthScreen)
        }

        btnRatio_169.setOnClickListener {
            qbStickerView.updateSize(QBStickerView.widthScreen, (QBStickerView.widthScreen.toFloat()* (9f/16f)).toInt())
        }

        btnRatio_916.setOnClickListener {
            qbStickerView.updateSize((QBStickerView.widthScreen.toFloat()* (9f/16f)).toInt(), QBStickerView.widthScreen)
        }

        btnAddText.setOnClickListener {
            qbStickerView.textSelected?.let {
                qbStickerView.duplicateText()
            } ?: run {
                qbStickerView.addText()
            }
        }

        qbStickerView.qbStickerViewListener = object : QBStickerViewListener {
            override fun onEntitySelected(qbSticker: QBStickerModel?) {
                Log.v("qbStickerViewListener", "$qbSticker")
            }

            override fun onCurrentStickerDeleted() {

            }

            override fun onActionUp() {

            }

            override fun updateTextStickerData(qbTextView: QBTextView) {

            }
        }

        edtInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                qbStickerView.textSelected?.let {
                    it.text = p0
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }
}