package com.quanbd.qbsticker.gesture

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent

class MoveGestureDetector(context: Context, private val listener: OnMoveGestureListener) :
    BaseGestureDetector(context) {
    companion object {
        private val FOCUS_DELTA_ZERO = PointF()
    }
    var focusDelta = PointF()
        private set

    private var currentCoordinateInternal: PointF? = null
    private var prevCoordinateInternal: PointF? = null
    private val coordinateExternal = PointF()

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_DOWN -> {
                event?.let {
                    listener.onDown(it.x, it.y)
                    resetState()
                    prevEvent = MotionEvent.obtain(it)
                    timeDelta = 0
                    updateStateByEvent(it)
                }

            }
            MotionEvent.ACTION_MOVE -> isInProgress = listener.onMoveBegin(this)
            MotionEvent.ACTION_POINTER_DOWN -> listener.onPointerDown()
        }
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener.onMoveFinish(this)
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                event?.let {
                    updateStateByEvent(it)

                    if (currentPressure / prevPressure > PRESSURE_THRESHOLD) {
                        val updatePrevious = listener.onMove(this)
                        if (updatePrevious) {
                            prevEvent?.recycle()
                            prevEvent = MotionEvent.obtain(it)
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> listener.onPointerDown()
        }
    }

    override fun updateStateByEvent(event: MotionEvent) {
        super.updateStateByEvent(event)
        val prev = prevEvent!!

        currentCoordinateInternal = determineFocalPoint(event)
        prevCoordinateInternal = determineFocalPoint(prev)

        val skipNextMoveEvent = prev.pointerCount != event.pointerCount
        focusDelta = if (skipNextMoveEvent) FOCUS_DELTA_ZERO else PointF(
            currentCoordinateInternal!!.x - prevCoordinateInternal!!.x,
            currentCoordinateInternal!!.y - prevCoordinateInternal!!.y
        )

        coordinateExternal.x += focusDelta.x
        coordinateExternal.y += focusDelta.y
    }

    private fun determineFocalPoint(e: MotionEvent): PointF {
        val count = e.pointerCount
        var x = 0f
        var y = 0f
        for (i in 0 until count) {
            x += e.getX(i)
            y += e.getY(i)
        }
        return PointF(x / count, y / count)
    }

    interface OnMoveGestureListener {
        fun onDown(x: Float, y: Float)
        fun onMove(detector: MoveGestureDetector): Boolean
        fun onMoveBegin(detector: MoveGestureDetector): Boolean
        fun onMoveFinish(detector: MoveGestureDetector)
        fun onPointerDown()
    }

    open class SimpleOnMoveGestureListener : OnMoveGestureListener {
        override fun onDown(x: Float, y: Float) {}
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
            return true
        }

        override fun onMoveFinish(detector: MoveGestureDetector) {

        }

        override fun onPointerDown() {}
    }
}