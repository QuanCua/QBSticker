package com.quanbd.qbsticker

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import com.quanbd.qbsticker.gesture.MoveGestureDetector
import com.quanbd.qbsticker.gesture.RotateGestureDetector
import com.quanbd.qbsticker.util.BitmapUtils

class QBStickerView(context: Context, private val attrs: AttributeSet) :
    FrameLayout(context, attrs) {
    companion object {
        const val TAG = "QBStickerView"
        val widthScreen = Resources.getSystem().displayMetrics.widthPixels
        val ICON_SIZE = widthScreen * 0.075f
    }

    /** Attribute */
    private var isEnableTransformIcon = true
    private var isEnableDeleteIcon = true
    private var lineSelectedColor = Color.WHITE
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var rotateGestureDetector: RotateGestureDetector? = null
    private var moveGestureDetector: MoveGestureDetector? = null
    private var gestureDetectorCompat: GestureDetectorCompat? = null

    var qbStickerViewListener: QBStickerViewListener? = null

    private var icTransform: Bitmap? = null
    private var isTransformTouched = false
    private var icDelete: Bitmap? = null
    private var isDeleteTouched = false
    private var isPointerDown = false
    private var isSizeChange = false
    var isEnableTouchEvent = true
    var isEnableFrameSelected = true
    private val paint = Paint()

    val listText: ArrayList<QBTextView> = arrayListOf()
    var textSelected: QBTextView? = null

    init {
        setWillNotDraw(false)
        initAttribute()
        initIcon()
        initPaint()
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        rotateGestureDetector = RotateGestureDetector(context, RotateListener())
        moveGestureDetector = MoveGestureDetector(context, MoveListener())
        gestureDetectorCompat = GestureDetectorCompat(context, TapsListener())

        setOnTouchListener { view, event ->
            view.performClick()
            scaleGestureDetector?.onTouchEvent(event)
            rotateGestureDetector?.onTouchEvent(event)
            moveGestureDetector?.onTouchEvent(event)
            gestureDetectorCompat?.onTouchEvent(event)
            isEnableTouchEvent
        }
    }

    private fun initPaint() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
    }

    private fun initAttribute() {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.QBStickerStyle,
            0, 0
        ).apply {
            try {
                isEnableTransformIcon = getBoolean(
                    R.styleable.QBStickerStyle_isEnableTransformIcon,
                    isEnableTransformIcon
                )
                isEnableDeleteIcon =
                    getBoolean(R.styleable.QBStickerStyle_isEnableDeleteIcon, isEnableDeleteIcon)
                lineSelectedColor =
                    Color.parseColor(getString(R.styleable.QBStickerStyle_lineSelectedColor))
            } finally {
                recycle()
            }
        }
    }

    private fun initIcon() {
        val bitmapTransform =
            BitmapUtils.getBitmapFromDrawable(context, R.drawable.ic_transform_sticker)
        icTransform = bitmapTransform?.let { BitmapUtils.resizeBitmap(it, ICON_SIZE, ICON_SIZE) }

        val bitmapDelete = BitmapUtils.getBitmapFromDrawable(context, R.drawable.ic_delete_sticker)
        icDelete = bitmapDelete?.let { BitmapUtils.resizeBitmap(it, ICON_SIZE, ICON_SIZE) }
    }

    fun setTransformIcon(resource: Int) {
        val bitmapTransform = BitmapUtils.getBitmapFromDrawable(context, resource)
        icTransform = bitmapTransform?.let { BitmapUtils.resizeBitmap(it, ICON_SIZE, ICON_SIZE) }
        invalidate()
    }

    fun setDeleteIcon(resource: Int) {
        val bitmapDelete = BitmapUtils.getBitmapFromDrawable(context, resource)
        icDelete = bitmapDelete?.let { BitmapUtils.resizeBitmap(it, ICON_SIZE, ICON_SIZE) }
        invalidate()
    }

    fun duplicateText(newId: String ?= null, newStartTime: Long? = null, newEndTime: Long? = null) {
        textSelected?.let {
            val newText = QBTextView(context, null).apply {
                val newModel = it.model.copy()
                if (newId != null)
                    newModel.id = newId
                if (newStartTime != null)
                    newModel.startTime = newStartTime
                if (newEndTime != null)
                    newModel.endTime = newEndTime
                this.model = newModel
                invalidateModel()
            }
            newText.setOptions(icTransform, icDelete)
            listText.add(newText)
            this.addView(newText)
            textSelected = newText
            invalidate()
        }
    }

    fun addListText(_listText: ArrayList<QBTextView>) {
        listText.clear()
        this.removeAllViews()
        listText.addAll(_listText)
        listText.forEach {
            it.setOptions(icTransform, icDelete)
            this.addView(it)
        }
        invalidate()
    }

    fun addText() {
        val qbTextView = QBTextView(context, null)
        qbTextView.setOptions(icTransform, icDelete)
        listText.add(qbTextView)
        this.addView(qbTextView)
        qbTextView.requestLayout()
        setDefaultTranslation(qbTextView)
    }

    fun deleteTextById(textID: String) {
        val iterator = listText.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.model.id == textID) {
                this.removeView(item)
                listText.remove(item)
                textSelected = null
                return
            }
        }
        invalidate()
    }

    private fun enableFrameSelected(isEnable: Boolean) {
        isEnableFrameSelected = isEnable
        invalidate()
    }

    fun getBitmapById(textID: String): Bitmap {
        enableFrameSelected(false)
        listText.forEach {
            if (it.model.id != textID)
                it.alpha = 0f
        }

        var bitmapOutput: Bitmap
        val bitmapParent = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapParent)
        draw(canvas)
        bitmapOutput = bitmapParent

        listText.forEach {
            if (it.model.id == textID) {
                val rect = it.getRectAroundText()
                val bitmapChild = Bitmap.createBitmap(
                    bitmapParent,
                    rect.left,
                    rect.top,
                    rect.width(),
                    rect.height()
                )
                bitmapOutput = bitmapChild
            } else
                it.alpha = 1f
        }
        enableFrameSelected(true)
        return bitmapOutput
    }

    fun updateTimeVisibleById(id: String, startTime: Long, endTime: Long) {
        listText.find { it.model.id == id }?.apply {
            model.startTime = startTime
            model.endTime = endTime
            invalidateModel()
        }
    }

    fun updateSize(newWidth: Int, newHeight: Int) {
        isSizeChange = true
        this.layoutParams.apply {
            width = newWidth
            height = newHeight
            requestLayout()
        }
    }

    private fun setDefaultTranslation(qbTextView: QBTextView) {
        val x = this.width.toFloat() / 2f - qbTextView.widthView / 2f
        val y = (this.height.toFloat() * 0.7f) - qbTextView.heightView / 2f
        qbTextView.updateTranslation(PointF(x, y))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        textSelected?.let {
            if (it.model.isVisible)
                drawTextComponent(canvas, it)
        }
        canvas.restore()
    }

    private fun drawTextComponent(canvas: Canvas, qbTextView: QBTextView) {
        val transformIcon = if (isEnableTransformIcon) icTransform else null
        val deleteIcon = if (isEnableDeleteIcon) icDelete else null
        qbTextView.drawFrameText(canvas, transformIcon, deleteIcon, lineSelectedColor)
    }

    /*fun setVisibilityEntity(qbTextView: QBTextView, isVisible: Boolean) {
        if (isVisible) {
            qbTextView.model.isVisible = true
            qbTextView.invalidateModel()
            enableFrameSelected(true)
        } else {
            qbTextView.model.isVisible = false
            qbTextView.invalidateModel()
            enableFrameSelected(false)
        }
    }*/

    fun setVisibilityEntity(qbTextView: QBTextView, isVisible: Boolean) {
        qbTextView.visibleText(isVisible)
        enableFrameSelected(isVisible)
        invalidate()
    }

    fun selectEntityById(id: String) {
        listText.forEach {
            if (it.model.id == id) {
                it.model.isSelected = true
                textSelected = it
                qbStickerViewListener?.onEntitySelected(textSelected)
            } else
                it.model.isSelected = false
        }
    }

    fun unselectEntity() {
        textSelected?.model?.isSelected = false
        textSelected = null
        qbStickerViewListener?.onEntitySelected(null)
    }

    private fun findEntity(event: MotionEvent): QBTextView? {
        var outputEntity: QBTextView? = null
        listText.forEach {
            if (it.isInsideTextArea(PointF(event.x, event.y)) && !it.model.isSelected && it.model.isVisible) {
                it.model.isSelected = true
                outputEntity = it
            } else
                it.model.isSelected = false
        }
        return outputEntity
    }

    private fun checkTouchByIcon(x: Float, y: Float) {
        textSelected?.let {
            if (isEnableTransformIcon)
                isTransformTouched = it.dstTransformRect.contains(x.toInt(), y.toInt()) == true

            if (isEnableDeleteIcon) {
                isDeleteTouched = it.dstDeleteRect.contains(x.toInt(), y.toInt()) == true
                if (isDeleteTouched) {
                    qbStickerViewListener?.onEntityDeleted(it.model.id)
                    this.removeView(it)
                    listText.remove(it)
                    textSelected = null
                }
            }
        }
    }

    inner class TapsListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            Log.v(TAG, "onDoubleTap")
            isPointerDown = false
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            Log.v(TAG, "onLongPress")
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            textSelected = findEntity(e)
            qbStickerViewListener?.onEntitySelected(textSelected)
            invalidate()
            return true
        }
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            textSelected?.let {
                if (it.model.isVisible && isPointerDown) {
                    textSelected?.updateScale(detector.scaleFactor - 1.0f, true)
                    invalidate()
                }
            }
            return true
        }
    }

    inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
            return true
        }

        override fun onRotate(detector: RotateGestureDetector): Boolean {
            textSelected?.let {
                if (it.model.isVisible && isPointerDown) {
                    textSelected?.updateRotate(-detector.rotationDegreesDelta)
                    invalidate()
                }
            }
            return true
        }
    }

    inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            textSelected?.let {
                if (it.model.isVisible) {
                    if (isTransformTouched)
                        it.rotateAndScaleByIcon(detector.focusDelta)
                    else
                        if (!isPointerDown && !isDeleteTouched)
                            it.updateTranslation(detector.focusDelta)

                    invalidate()
                }
            }
            return true
        }

        override fun onDown(x: Float, y: Float) {
            super.onDown(x, y)
            checkTouchByIcon(x, y)
        }

        override fun onPointerDown() {
            super.onPointerDown()
            Log.v(TAG, "onPointerDown")
            isPointerDown = true
        }

        override fun onMoveFinish(detector: MoveGestureDetector) {
            super.onMoveFinish(detector)
            Log.v(TAG, "onMoveFinish")
            isPointerDown = false
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.v(TAG, "onSizeChanged")

        if (isSizeChange) {
            if (oldw == 0 || oldh == 0) return
            val calX = (oldw - w) / 2f
            val calY = (oldh - h) / 2f
            val oldSize = (oldw + oldh).toFloat()
            val newSize = (w + h).toFloat()
            listText.forEach {
                it.updateTranslation(PointF(-calX, -calY))
                it.updateScale(newSize / oldSize)
                qbStickerViewListener?.onUpdateEntityModel(it.model)
            }
        }
    }
}