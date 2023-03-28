package com.quanbd.qbsticker

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import com.quanbd.qbsticker.gesture.MoveGestureDetector
import com.quanbd.qbsticker.gesture.RotateGestureDetector
import com.quanbd.qbsticker.util.BitmapUtils

class QBStickerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    companion object {
        const val TAG = "QBStickerView"
        val widthScreen = Resources.getSystem().displayMetrics.widthPixels
        val ICON_SIZE = widthScreen * 0.075f
    }

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var rotateGestureDetector: RotateGestureDetector? = null
    private var moveGestureDetector: MoveGestureDetector? = null
    private var gestureDetectorCompat: GestureDetectorCompat? = null

    var qbStickerViewListener: QBStickerViewListener? = null

    val listText: ArrayList<QBTextView> = arrayListOf()
    var textSelected: QBTextView? = null
    private var icTransform: Bitmap? = null
    private var isTransformTouched = false
    private var icDelete: Bitmap? = null
    private var isDeleteTouched = false
    private var isPointerDown = false
    private var isSizeChange = false
    var widthView = 0f
    var heightView = 0f

    private val paint = Paint()

    init {
        setWillNotDraw(false)
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
            true
        }
    }

    private fun initViewSize() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        widthView = measuredWidth.toFloat()
        heightView = measuredHeight.toFloat()
        Log.v("initViewSize", "$widthView - $heightView")
    }

    private fun initIcon() {
        val bitmapTransform =
            BitmapUtils.getBitmapFromDrawable(context, R.drawable.ic_transform_sticker)
        icTransform = bitmapTransform?.let { BitmapUtils.resizeBitmap(it, ICON_SIZE, ICON_SIZE) }

        val bitmapDelete = BitmapUtils.getBitmapFromDrawable(context, R.drawable.ic_delete_sticker)
        icDelete = bitmapDelete?.let { BitmapUtils.resizeBitmap(it, ICON_SIZE, ICON_SIZE) }
    }

    private fun initPaint() {
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
    }

    fun duplicateText() {
        textSelected?.let {
            val newText = QBTextView(context, null).apply {
                textSize = it.textSize / resources.displayMetrics.scaledDensity
                typeface = it.typeface
                updateModel(it.model)
            }
            newText.setOptions(icTransform, icDelete)
            listText.add(0, newText)
            this.addView(listText[0])
        }
    }

    fun addText() {
        val qbTextView = QBTextView(context, null)
        qbTextView.setOptions(icTransform, icDelete)
        setDefaultTranslation(qbTextView)
        listText.add(0, qbTextView)
        this.addView(listText[0])
    }

    fun updateSize(newWidth: Int, newHeight: Int) {
        isSizeChange = true
        val params = ViewGroup.LayoutParams(newWidth, newHeight)
        this.layoutParams = params
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
            drawTextComponent(canvas, it)
        }
        canvas.restore()
    }

    private fun drawTextComponent(canvas: Canvas, qbTextView: QBTextView) {
        qbTextView.drawFrameText(canvas, icTransform, icDelete)
    }


    private fun findEntity(event: MotionEvent): QBTextView? {
        var outputEntity: QBTextView? = null
        listText.forEach {
            if (it.isInsideTextArea(PointF(event.x, event.y)) && !it.model.isSelected) {
                it.model.isSelected = true
                outputEntity = it
            } else
                it.model.isSelected = false
        }
        return outputEntity
    }

    private fun checkTouchByIcon(x: Float, y: Float) {
        textSelected?.let {
            isTransformTouched = it.dstTransformRect.contains(x.toInt(), y.toInt()) == true
            isDeleteTouched = it.dstDeleteRect.contains(x.toInt(), y.toInt()) == true
            if (isDeleteTouched) {
                qbStickerViewListener?.onCurrentStickerDeleted()
                this.removeView(it)
                listText.remove(it)
                textSelected = null
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
            qbStickerViewListener?.onEntitySelected(textSelected?.model)
            invalidate()
            return true
        }
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (textSelected != null && isPointerDown) {
                textSelected?.updateScale(detector.scaleFactor - 1.0f, true)
                invalidate()
            }
            return true
        }
    }

    inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            if (textSelected != null && isPointerDown) {
                textSelected?.updateRotate(-detector.rotationDegreesDelta)
                invalidate()
            }
            return true
        }
    }

    inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            textSelected?.let {
                if (isTransformTouched)
                    it.rotateAndScaleByIcon(detector.focusDelta)
                else
                    if (!isPointerDown && !isDeleteTouched)
                        it.updateTranslation(detector.focusDelta)

                invalidate()
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
            qbStickerViewListener?.onActionUp()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.v("onLayout", "onSizeChanged")

        if (isSizeChange) {
            if (oldw == 0 || oldh == 0) return
            val calX = (oldw - w) / 2f
            val calY = (oldh - h) / 2f
            val oldSize = (oldw + oldh).toFloat()
            val newSize = (w + h).toFloat()
            listText.forEach {
                it.updateTranslation(PointF(-calX, -calY))
                it.updateScale(newSize / oldSize)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }
}