/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.graphics.Rect
import androidx.annotation.Keep

class FilledRectangleDrawable constructor(
    backgroundColor: Int,
    foregroundColor: Int
) : RectangleDrawable(backgroundColor, foregroundColor) {

    private var rectangleHeightPercentage: Int = 0
        @Keep
        set(value) {
            field = value

            callback?.invalidateDrawable(this) ?: {
                secondRectangleAnimator.cancel()
            }
        }

    private val secondRectangleAnimator: ObjectAnimator =
        ObjectAnimator.ofInt(this, "rectangleHeightPercentage", 0, 0)

    override fun draw(canvas: Canvas) {
        canvas.drawRect(Rect(0, 0, getWidth(), getHeight()), backgroundPaint)
        canvas.drawRect(
            Rect(0, getHeight() - getHeightForPercentage(rectangleHeightPercentage), getWidth(), getHeight()),
            foregroundPaint
        )
    }

    override fun onLevelChange(level: Int): Boolean {
        if(level in 0..100) {
            secondRectangleAnimator.cancel()
            secondRectangleAnimator.setIntValues(rectangleHeightPercentage, level)
            secondRectangleAnimator.start()
            return true
        }
        return false
    }

    private fun getHeightForPercentage(percentage: Int): Int =
        ((getHeight() * percentage) / 100.0).toInt()
}
