package com.quanbd.qbsticker.gesture

import android.content.Context
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2

class ShoveGestureDetector(context: Context, private val listener: OnShoveGestureListener) :
    TwoFingerGestureDetector(context) {
    private var prevAverageY = 0f
    private var currentAverageY = 0f
    private var sloppyGesture = false

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                event?.let {
                    resetState()
                    prevEvent = MotionEvent.obtain(it)
                    timeDelta = 0
                    updateStateByEvent(it)

                    sloppyGesture = isSloppyGesture(it)
                    if (!sloppyGesture) {
                        isInProgress = listener.onShoveBegin(this)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!sloppyGesture) {
                    return
                }

                sloppyGesture = if (event != null) isSloppyGesture(event) else false
                if (!sloppyGesture) {
                    isInProgress = listener.onShoveBegin(this)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> if (!sloppyGesture) {
                return
            }
        }
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_UP -> {
                event?.let { updateStateByEvent(it) }
                if (!sloppyGesture) {
                    listener.onShoveEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!sloppyGesture) {
                    listener.onShoveEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                event?.let { updateStateByEvent(it) }

                if (currentPressure / prevPressure > PRESSURE_THRESHOLD
                    && abs(shovePixelsDelta) > 0.5f) {
                    val updatePrevious = listener.onShove(this)
                    if (updatePrevious) {
                        prevEvent?.recycle()
                        prevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = prevEvent!!
        val py0 = prev.getY(0)
        val py1 = prev.getY(1)
        prevAverageY = (py0 + py1) / 2.0f
        val cy0 = event.getY(0)
        val cy1 = event.getY(1)
        currentAverageY = (cy0 + cy1) / 2.0f
    }

    override fun isSloppyGesture(event: MotionEvent): Boolean {
        val sloppy = super.isSloppyGesture(event)
        if (sloppy) return true

        val angle = abs(atan2(currentFingerDiffY.toDouble(), currentFingerDiffX.toDouble()))
        return !(0.0f < angle && angle < 0.35f
                || 2.79f < angle && angle < Math.PI)
    }

    private val shovePixelsDelta: Float
        get() = currentAverageY - prevAverageY

    override fun resetState() {
        super.resetState()
        sloppyGesture = false
        prevAverageY = 0.0f
        currentAverageY = 0.0f
    }

    interface OnShoveGestureListener {
        fun onShove(detector: ShoveGestureDetector?): Boolean
        fun onShoveBegin(detector: ShoveGestureDetector?): Boolean
        fun onShoveEnd(detector: ShoveGestureDetector?)
    }

    class SimpleOnShoveGestureListener : OnShoveGestureListener {
        override fun onShove(detector: ShoveGestureDetector?): Boolean {
            return false
        }

        override fun onShoveBegin(detector: ShoveGestureDetector?): Boolean {
            return true
        }

        override fun onShoveEnd(detector: ShoveGestureDetector?) {

        }
    }
}