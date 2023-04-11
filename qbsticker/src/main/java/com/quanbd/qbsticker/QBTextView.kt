package com.quanbd.qbsticker

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.quanbd.qbsticker.util.FontUtils
import com.quanbd.qbsticker.util.MathUtils
import kotlin.math.acos
import kotlin.math.sqrt

class QBTextView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        const val DEFAULT_CONTENT = "Enter your text"
        const val DEFAULT_SIZE = 20f
        const val PADDING_HORIZONTAL = 40f
        const val PADDING_VERTICAL = 20f
    }

    private val paintFrame = Paint()
    private val paintText = TextPaint()
    private val srcPoints = FloatArray(10)
    private val destPoints = FloatArray(10)
    val centerPoint = PointF()
    private var srcTransformRect = Rect()
    private var srcDeleteRect = Rect()
    var dstTransformRect = Rect()
    var dstDeleteRect = Rect()
    var hitRect = Rect()
    var textBoundRect = Rect()
    var model = QBStickerModel.Builder().build()
    private var staticLayout: StaticLayout? = null
    var widthView = 0f
    var heightView = 0f
    private var isTextChanged = false

    init {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params

        paintText.apply {
            isAntiAlias = true
            textSize = model.textSize * Resources.getSystem().displayMetrics.density
            color = model.color
            typeface = model.getFontDefault(context).first
        }

        paintFrame.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = Color.WHITE
        }

        createStaticLayout()
    }

    private fun createStaticLayout() {
        val width: Int
        if (model.text.contains("\n")) {
            val textSubs = model.text.split("\n").toTypedArray()
            var indexOfMax = 0
            for (i in textSubs.indices) {
                if (textSubs[indexOfMax].length < textSubs[i].length)
                    indexOfMax = i
            }
            paintText.getTextBounds(model.text, 0, textSubs[indexOfMax].length, textBoundRect)
            width = textBoundRect.width() + PADDING_HORIZONTAL.toInt()
        } else {
            if (model.text.isNotEmpty())
                paintText.getTextBounds(model.text, 0, model.text.length, textBoundRect)

            width = textBoundRect.width() + PADDING_HORIZONTAL.toInt()
        }

        val newStaticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(model.text, 0, model.text.length, paintText, width)
                .setAlignment(model.textAlignment)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false).build()
        } else {
            StaticLayout(
                model.text,
                paintText,
                width,
                model.textAlignment,
                1.0f,
                0f,
                false
            )
        }
        staticLayout = newStaticLayout
        widthView = staticLayout?.width?.toFloat() ?: 0f
        heightView = staticLayout?.height?.toFloat() ?: 0f
    }

    fun setText(newText: String) {
        isTextChanged = true
        model.text = newText
        createStaticLayout()
        requestLayout()
    }

    fun setOptions(icTransform: Bitmap?, icDelete: Bitmap?) {
        srcTransformRect[0, 0, icTransform?.width ?: 0] = icTransform?.height ?: 0
        dstTransformRect = Rect(0, 0, 32 shl 1, 32 shl 1)

        srcDeleteRect[0, 0, icDelete?.width ?: 0] = icDelete?.height ?: 0
        dstDeleteRect = Rect(0, 0, 32 shl 1, 32 shl 1)
    }

    fun invalidateModel() {
        paintText.apply {
            isAntiAlias = true
            textSize = model.textSize * Resources.getSystem().displayMetrics.density
            color = model.color
            typeface = FontUtils.getFontByKey(model.fontKey)
        }
        this.translationX = model.translation.x
        this.translationY = model.translation.y
        this.scaleX = model.scale
        this.scaleY = model.scale
        this.rotation = model.rotate
        createStaticLayout()
        requestLayout()
    }

    fun visibleText(isVisible: Boolean) {
        model.isVisible = isVisible
        invalidate()
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

    fun getRectAroundText(): Rect {
        this.getHitRect(hitRect)
        return hitRect
    }

    fun getWidthFrame(): Float {
        return srcPoints[2]
    }

    fun getHeightFrame(): Float {
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

        canvas.save()
        if (model.isVisible)
            staticLayout?.draw(canvas)
        canvas.restore()
    }

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

    private fun setSrcPoint(width: Float, height: Float) {
        srcPoints[0] = 0f
        srcPoints[1] = 0f
        srcPoints[2] = width
        srcPoints[3] = 0f
        srcPoints[4] = width
        srcPoints[5] = height
        srcPoints[6] = 0f
        srcPoints[7] = height
        srcPoints[8] = 0f
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (isTextChanged) {
            isTextChanged = false
            val oldCenter = absoluteCenter(srcPoints[2], srcPoints[5])
            setSrcPoint(w.toFloat(), h.toFloat())
            val newCenter = absoluteCenter(srcPoints[2], srcPoints[5])
            calculationCenter(oldCenter, newCenter)
        } else
            setSrcPoint(w.toFloat(), h.toFloat())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthView = staticLayout?.width?.toFloat() ?: 0f
        heightView = staticLayout?.height?.toFloat() ?: 0f
        setMeasuredDimension(widthView.toInt(), heightView.toInt())
    }
}