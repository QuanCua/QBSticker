package com.quanbd.qbsticker.util

import android.graphics.PointF

object MathUtils {
    /**
     * For more info:
     * see more : http://math.stackexchange.com/questions/190111/how-to-check-if-a-point-is-inside-a-rectangle
     *
     * @param pt point to check
     * @param v1 vertex 1 of the triangle
     * @param v2 vertex 2 of the triangle
     * @param v3 vertex 3 of the triangle
     * @return true if point (x, y) is inside the triangle
     */
    fun pointInTriangle(
        pt: PointF, v1: PointF,
        v2: PointF, v3: PointF
    ): Boolean {
        val b1: Boolean = crossProduct(pt, v1, v2) < 0.0f
        val b2: Boolean = crossProduct(pt, v2, v3) < 0.0f
        val b3: Boolean = crossProduct(pt, v3, v1) < 0.0f
        return b1 == b2 && b2 == b3
    }

    /**
     * calculates cross product of vectors AB and AC
     *
     * @param a beginning of 2 vectors
     * @param b end of vector 1
     * @param c enf of vector 2
     * @return cross product AB * AC
     */
    private fun crossProduct(a: PointF, b: PointF, c: PointF): Float {
        return crossProduct(a.x, a.y, b.x, b.y, c.x, c.y)
    }

    /**
     * calculates cross product of vectors AB and AC
     *
     * @param ax X coordinate of point A
     * @param ay Y coordinate of point A
     * @param bx X coordinate of point B
     * @param by Y coordinate of point B
     * @param cx X coordinate of point C
     * @param cy Y coordinate of point C
     * @return cross product AB * AC
     */
    private fun crossProduct(
        ax: Float,
        ay: Float,
        bx: Float,
        by: Float,
        cx: Float,
        cy: Float
    ): Float {
        return (ax - cx) * (by - cy) - (bx - cx) * (ay - cy)
    }
}