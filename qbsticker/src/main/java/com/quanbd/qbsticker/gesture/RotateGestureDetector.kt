package com.quanbd.qbsticker.gesture

import android.content.Context
import android.view.MotionEvent
import kotlin.math.atan2

class RotateGestureDetector(context: Context, private val listener: OnRotateGestureListener) :
    TwoFingerGestureDetector(context) {
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
                        isInProgress = listener.onRotateBegin(this)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!sloppyGesture) return

                sloppyGesture = if (event != null) isSloppyGesture(event) else false
                if (!sloppyGesture) {
                    isInProgress = listener.onRotateBegin(this)
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
                    listener.onRotateEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!sloppyGesture) {
                    listener.onRotateEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                event?.let {
                    updateStateByEvent(it)

                    if (currentPressure / prevPressure > PRESSURE_THRESHOLD) {
                        val updatePrevious = listener.onRotate(this)
                        if (updatePrevious) {
                            prevEvent!!.recycle()
                            prevEvent = MotionEvent.obtain(it)
                        }
                    }
                }
            }
        }
    }

    override fun resetState() {
        super.resetState()
        sloppyGesture = false
    }

    val rotationDegreesDelta: Float
        get() {
            val diffRadians =
                atan2(prevFingerDiffY.toDouble(), prevFingerDiffX.toDouble()) - atan2(
                    currentFingerDiffY.toDouble(),
                    currentFingerDiffX.toDouble()
                )
            return (diffRadians * 180 / Math.PI).toFloat()
        }

    interface OnRotateGestureListener {
        fun onRotate(detector: RotateGestureDetector): Boolean
        fun onRotateBegin(detector: RotateGestureDetector): Boolean
        fun onRotateEnd(detector: RotateGestureDetector)
    }

    open class SimpleOnRotateGestureListener : OnRotateGestureListener {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            return false
        }

        override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
            return true
        }

        override fun onRotateEnd(detector: RotateGestureDetector) {

        }
    }
}