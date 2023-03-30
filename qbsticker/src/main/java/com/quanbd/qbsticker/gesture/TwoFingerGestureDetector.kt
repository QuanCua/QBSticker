package com.quanbd.qbsticker.gesture

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.sqrt

abstract class TwoFingerGestureDetector(context: Context) : BaseGestureDetector(context) {
    private val edgeSlop: Float
    protected var prevFingerDiffX = 0f
    protected var prevFingerDiffY = 0f
    protected var currentFingerDiffX = 0f
    protected var currentFingerDiffY = 0f
    private var rightSlopEdge = 0f
    private var bottomSlopEdge = 0f
    private var currentLength = 0f
    private var prevLength = 0f

    init {
        val config = ViewConfiguration.get(context)
        edgeSlop = config.scaledEdgeSlop.toFloat()
    }

    abstract override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?)
    abstract override fun handleInProgressEvent(actionCode: Int, event: MotionEvent?)

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = prevEvent
        currentLength = -1f
        prevLength = -1f

        val px0 = prev?.getX(0)
        val py0 = prev?.getY(0)
        val px1 = prev?.getX(1)
        val py1 = prev?.getY(1)
        val pvx = (px1 ?: 0f) - (px0 ?: 0f)
        val pvy = (py1 ?: 0f) - (py0 ?: 0f)
        prevFingerDiffX = pvx
        prevFingerDiffY = pvy

        val cx0 = event.getX(0)
        val cy0 = event.getY(0)
        val cx1 = event.getX(1)
        val cy1 = event.getY(1)
        val cvx = cx1 - cx0
        val cvy = cy1 - cy0
        currentFingerDiffX = cvx
        currentFingerDiffY = cvy
    }

    val currentSpan: Float
        get() {
            if (currentLength == -1f) {
                val cvx = currentFingerDiffX
                val cvy = currentFingerDiffY
                currentLength = sqrt((cvx * cvx + cvy * cvy).toDouble()).toFloat()
            }
            return currentLength
        }

    val previousSpan: Float
        get() {
            if (prevLength == -1f) {
                val pvx = prevFingerDiffX
                val pvy = prevFingerDiffY
                prevLength = sqrt((pvx * pvx + pvy * pvy).toDouble()).toFloat()
            }
            return prevLength
        }

    protected open fun isSloppyGesture(event: MotionEvent): Boolean {
        val metrics = context.resources.displayMetrics
        rightSlopEdge = metrics.widthPixels - edgeSlop
        bottomSlopEdge = metrics.heightPixels - edgeSlop
        val edgeSlop = edgeSlop
        val rightSlop = rightSlopEdge
        val bottomSlop = bottomSlopEdge
        val x0 = event.getX(0)
        val y0 = event.getY(0)
        val x1 = event.getX(1)
        val y1 = event.getY(1)
        val p0sloppy = x0 < edgeSlop || y0 < edgeSlop || x0 > rightSlop || y0 > bottomSlop
        val p1sloppy = x1 < edgeSlop || y1 < edgeSlop || x1 > rightSlop || y1 > bottomSlop
        if (p0sloppy && p1sloppy) {
            return true
        } else if (p0sloppy) {
            return true
        } else if (p1sloppy) {
            return true
        }
        return false
    }
}