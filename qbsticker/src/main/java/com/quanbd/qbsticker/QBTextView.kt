package com.quanbd.qbsticker

import android.content.Context
import android.graphics.*
import android.text.InputFilter
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.quanbd.qbsticker.util.FontUtils
import com.quanbd.qbsticker.util.MathUtils
import kotlin.math.acos
import kotlin.math.sqrt

class QBTextView(context: Context, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    companion object {
        const val DEFAULT_CONTENT = "Enter your text"
        const val PADDING_HORIZONTAL = 20f
        const val PADDING_VERTICAL = 20f
    }

    private var isInit = false
    private val paintFrame = Paint()
    private val srcPoints = FloatArray(10)
    private val destPoints = FloatArray(10)
    val centerPoint = PointF()
    var srcTransformRect = Rect()
    var dstTransformRect = Rect()
    var srcDeleteRect = Rect()
    var dstDeleteRect = Rect()
    var hitRect = Rect()
    var model = QBStickerModel.Builder().build()

    var widthView = 0f
    var heightView = 0f

    init {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
//        this.isSingleLine = true
        this.text = DEFAULT_CONTENT
        this.textSize = 30f
        this.textAlignment = TEXT_ALIGNMENT_CENTER
        this.setTextColor(Color.WHITE)
        val filters = arrayOfNulls<InputFilter>(1)
        filters[0] = InputFilter.LengthFilter(241996)
        this.filters = filters
        initViewSize()

        paintFrame.isAntiAlias = true
        paintFrame.style = Paint.Style.STROKE
        paintFrame.strokeWidth = 4f
        paintFrame.color = Color.WHITE

        isInit = true
    }

    private fun initViewSize() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        this.widthView = paint.measureText(text.toString())
        this.heightView = measuredHeight.toFloat()
    }

    private fun updateViewSizeByTextChanged() {
        this.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val width = this@QBTextView.width
                val height = this@QBTextView.height

                widthView = width.toFloat()
                heightView = height.toFloat()

                val oldCenter = absoluteCenter(srcPoints[2], srcPoints[5])
                setSrcPoint(widthView, heightView)
                val newCenter = absoluteCenter(srcPoints[2], srcPoints[5])
                calculationCenter(oldCenter, newCenter)

                this@QBTextView.viewTreeObserver.removeOnPreDrawListener(this)
                return false
            }
        })
    }

    fun setOptions(icTransform: Bitmap?, icDelete: Bitmap?) {
        srcTransformRect[0, 0, icTransform?.width ?: 0] = icTransform?.height ?: 0
        dstTransformRect = Rect(0, 0, 32 shl 1, 32 shl 1)

        srcDeleteRect[0, 0, icDelete?.width ?: 0] = icDelete?.height ?: 0
        dstDeleteRect = Rect(0, 0, 32 shl 1, 32 shl 1)
    }

    fun updateModel(newModel: QBStickerModel? = null) {
        if (newModel != null) {
            model = newModel.copy()
            model.id = System.nanoTime().toString()
            model.isSelected = false
        }

        newModel?.let {
            val translationBonus = 4f
            this@QBTextView.text = text
            this@QBTextView.setTextColor(it.color)
            when (it.align) {
                QBStickerModel.ALIGN_LEFT, QBStickerModel.ALIGN_JUSTIFIED ->
                    this@QBTextView.textAlignment = TEXT_ALIGNMENT_TEXT_START
                QBStickerModel.ALIGN_RIGHT ->
                    this@QBTextView.textAlignment = TEXT_ALIGNMENT_TEXT_END
                else ->
                    this@QBTextView.textAlignment = TEXT_ALIGNMENT_CENTER
            }

            model.translation.x = it.translation.x + translationBonus
            model.translation.y = it.translation.y + translationBonus
            this@QBTextView.translationX = it.translation.x + translationBonus
            this@QBTextView.translationY = it.translation.y + translationBonus
            this@QBTextView.scaleX = it.scale
            this@QBTextView.scaleY = it.scale
            this@QBTextView.rotation = it.rotate
        }
    }

    fun invalidateModel() {
        model.apply {
            this@QBTextView.text = this.text
            this@QBTextView.setTextColor(this.color)
            this@QBTextView.typeface = FontUtils.getFontByKey(this.fontKey)
            this@QBTextView.visibility = if (this.isVisible) View.VISIBLE else View.GONE
            when (this.align) {
                QBStickerModel.ALIGN_LEFT, QBStickerModel.ALIGN_JUSTIFIED ->
                    this@QBTextView.textAlignment = TEXT_ALIGNMENT_TEXT_START
                QBStickerModel.ALIGN_RIGHT ->
                    this@QBTextView.textAlignment = TEXT_ALIGNMENT_TEXT_END
                else ->
                    this@QBTextView.textAlignment = TEXT_ALIGNMENT_CENTER
            }
            this@QBTextView.translationX = this.translation.x
            this@QBTextView.translationY = this.translation.y
            this@QBTextView.scaleX = this.scale
            this@QBTextView.scaleY = this.scale
            this@QBTextView.rotation = this.rotate
        }
    }
    
    fun updateTranslation(translationValue: PointF) {
        model.translation.x += translationValue.x
        model.translation.y += translationValue.y
        this.translationX = model.translation.x
        this.translationY = model.translation.y
    }

    fun updateRotate(rotateValue: Float) {
        var newRotate = model.rotate + rotateValue
        newRotate %= 360.0f
        model.rotate = newRotate
        this.rotation = model.rotate
    }

    fun updateScale(scaleValue: Float, isMultiTouch: Boolean = false) {
        val newScale = if (isMultiTouch) model.scale + scaleValue
        else model.scale * scaleValue
        if (newScale in QBStickerModel.MIN_SCALE..QBStickerModel.MAX_SCALE) {
            model.scale = newScale
            this.scaleX = this.model.scale
            this.scaleY = this.model.scale
        }
    }

    fun getRectAroundText() : Rect {
        this.getHitRect(hitRect)
        return hitRect
    }

    fun getWidthFrame() : Float {
        return srcPoints[2]
    }

    fun getHeightFrame() : Float {
        return srcPoints[5]
    }

    fun rotateAndScaleByIcon(focusDelta: PointF) {
        val cx = centerPoint.x
        val cy = centerPoint.y
        val x = dstTransformRect.centerX()
        val y = dstTransformRect.centerY()
        val nx = x.plus(focusDelta.x)
        val ny = y.plus(focusDelta.y)
        val xa = x.minus(cx)
        val ya = y.minus(cy)
        val xb = nx.minus(cx)
        val yb = ny.minus(cy)
        val srcLen = sqrt(xa.times(xa).toDouble() + ya.times(ya).toDouble()).toFloat()
        val curLen = sqrt(xb.times(xb).toDouble() + yb.times(yb).toDouble()).toFloat()
        val scale = curLen / srcLen
        updateScale(scale)

        val cos = (xa.times(xb) + ya.times(yb)).toDouble() / (srcLen * curLen).toDouble()
        if (cos > 1.0 || cos < -1.0) return
        var angle = Math.toDegrees(acos(cos)).toFloat()
        val calculationMatrix = xa.times(yb) - xb.times(ya)
        val flag = if (calculationMatrix > 0f) 1f else -1f
        angle *= flag
        updateRotate(angle)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }

    /*fun flip() {
        if (!isFlip) {
            // Lật văn bản theo trục Y
            rotationY = 180f
            // Hoán đổi chiều của văn bản bằng ma trận
            val matrix = Matrix()
            matrix.preScale(-1f, 1f)
            paint.textScaleX = -1f
            matrix.postTranslate(width.toFloat(), 0f)
            this.matrix.set(matrix)
            isFlip = true
        } else {
            // Đặt lại văn bản và ma trận về trạng thái ban đầu
            rotationY = 0f
            val matrix = Matrix()
            matrix.preScale(1f, 1f)
            paint.textScaleX = 1f
            matrix.postTranslate(0f, 0f)
            this.matrix.set(matrix)
            isFlip = false
        }
    }*/

    fun drawFrameText(
        canvas: Canvas,
        icTransform: Bitmap?,
        icDelete: Bitmap?,
        lineSelectedColor: Int
    ) {
        this.matrix.mapPoints(destPoints, srcPoints)
        paintFrame.color = lineSelectedColor
        canvas.drawLines(destPoints, 0, 8, paintFrame)
        canvas.drawLines(destPoints, 2, 8, paintFrame)

        val offsetValue = dstTransformRect.width() shr 1
        dstTransformRect.offsetTo(
            (destPoints[4] - offsetValue).toInt(),
            (destPoints[5] - offsetValue).toInt()
        )
        dstDeleteRect.offsetTo(
            (destPoints[0] - offsetValue).toInt(),
            (destPoints[1] - offsetValue).toInt()
        )

        centerPoint.x = (dstTransformRect.centerX() + dstDeleteRect.centerX()) * 0.5f
        centerPoint.y = (dstTransformRect.centerY() + dstDeleteRect.centerY()) * 0.5f

        icDelete?.let {
            canvas.drawBitmap(
                it,
                dstDeleteRect.centerX().toFloat() - it.width / 2f,
                dstDeleteRect.centerY().toFloat() - it.height / 2f,
                null
            )
        }
        icTransform?.let {
            canvas.drawBitmap(
                it,
                dstTransformRect.centerX().toFloat() - it.width / 2f,
                dstTransformRect.centerY().toFloat() - it.height / 2f,
                null
            )
        }
    }

    fun isInsideTextArea(point: PointF): Boolean {
        val pA = PointF()
        val pB = PointF()
        val pC = PointF()
        val pD = PointF()
        this.matrix.mapPoints(destPoints, srcPoints)
        pA.x = destPoints[0]
        pA.y = destPoints[1]
        pB.x = destPoints[2]
        pB.y = destPoints[3]
        pC.x = destPoints[4]
        pC.y = destPoints[5]
        pD.x = destPoints[6]
        pD.y = destPoints[7]
        return MathUtils.pointInTriangle(point, pA, pB, pC) || MathUtils.pointInTriangle(
            point, pA, pD, pC
        )
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        try {
            if (!isInit) {
                this.post {
                    initViewSize()
                    setSrcPoint(this.widthView, this.heightView)
                }
            } else {
                updateViewSizeByTextChanged()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setSrcPoint(width: Float, height: Float) {
        srcPoints[0] = -PADDING_HORIZONTAL
        srcPoints[1] = 0f
        srcPoints[2] = width + PADDING_HORIZONTAL
        srcPoints[3] = 0f
        srcPoints[4] = width + PADDING_HORIZONTAL
        srcPoints[5] = height
        srcPoints[6] = -PADDING_HORIZONTAL
        srcPoints[7] = height
        srcPoints[8] = -PADDING_HORIZONTAL
        srcPoints[9] = 0f
    }

    private fun calculationCenter(oldCenter: PointF, newCenter: PointF) {
        val x = (oldCenter.x - newCenter.x) / 2f
        val y = (oldCenter.y - newCenter.y) / 2f
        updateTranslation(PointF(x, y))
    }

    private fun absoluteCenter(width: Float, height: Float): PointF {
        val centerX = this.translationX + width
        val centerY = this.translationY + height
        return PointF(centerX, centerY)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth , measuredHeight)
    }
}