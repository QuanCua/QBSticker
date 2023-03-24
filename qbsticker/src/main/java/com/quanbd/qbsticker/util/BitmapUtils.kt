package com.quanbd.qbsticker.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide

object BitmapUtils {
    fun resizeBitmap(bitmap: Bitmap, newWidth: Float, newHeight: Float): Bitmap {
        val bitmapOrigin = bitmap.copy(bitmap.config, false)
        return if (newWidth > 0f && newHeight > 0f) {
            val width = bitmapOrigin.width.toFloat()
            val height = bitmapOrigin.height.toFloat()
            val scaleWidth = newWidth / width
            val scaleHeight = newHeight / height
            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)
            Bitmap.createBitmap(bitmapOrigin, 0, 0, width.toInt(), height.toInt(), matrix, true)
        } else {
            bitmap
        }
    }

    fun getBitmapFromDrawable(context: Context, resource: Int): Bitmap? {
        val bitmapOutput = try {
            Glide.with(context)
                .asBitmap()
                .load(resource)
                .submit()
                .get()
        } catch (e: java.lang.Exception) {
            AppCompatResources.getDrawable(context, resource)?.toBitmap()
        }
        return bitmapOutput
    }
}