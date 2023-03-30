package com.quanbd.qbsticker.gesture

import android.content.Context
import android.view.MotionEvent

abstract class BaseGestureDetector(protected val context: Context) {
    companion object {
        const val PRESSURE_THRESHOLD = 0.67f
    }

    var isInProgress = false
        protected set
    var timeDelta: Long = 0
        protected set

    protected var prevEvent: MotionEvent? = null
    protected var currentEvent: MotionEvent? = null
    protected var currentPressure = 0f
    protected var prevPressure = 0f

    fun onTouchEvent(event: MotionEvent): Boolean {
        val actionCode = event.action and MotionEvent.ACTION_MASK
        if (!isInProgress) {
            handleStartProgressEvent(actionCode, event)
        } else {
            handleInProgressEvent(actionCode, event)
        }
        return true
    }

    protected abstract fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?)
    protected abstract fun handleInProgressEvent(actionCode: Int, event: MotionEvent?)

    protected open fun updateStateByEvent(event: MotionEvent) {
        val prev = prevEvent

        if (currentEvent != null) {
            currentEvent?.recycle()
            currentEvent = null
        }
        currentEvent = MotionEvent.obtain(event)

        timeDelta = event.eventTime - prev!!.eventTime
        currentPressure = event.getPressure(event.actionIndex)
        prevPressure = prev.getPressure(prev.actionIndex)
    }

    protected open fun resetState() {
        if (prevEvent != null) {
            prevEvent?.recycle()
            prevEvent = null
        }
        if (currentEvent != null) {
            currentEvent?.recycle()
            currentEvent = null
        }
        isInProgress = false
    }

    val eventTime: Long
        get() = currentEvent?.eventTime ?: 0
}