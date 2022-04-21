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

class ChippedRectangleDrawable constructor(
    backgroundColor: Int,
    foregroundColor: Int
) : RectangleDrawable(backgroundColor, foregroundColor) {

    private var rectangleChipPercentage: Int = 0
        @Keep
        set(value) {
            field = value

            callback?.invalidateDrawable(this) ?: {
                secondRectangleAnimator.cancel()
            }
        }

    private val secondRectangleAnimator: ObjectAnimator =
        ObjectAnimator.ofInt(this, "rectangleChipPercentage", 0, 0)


    override fun draw(canvas: Canvas) {
        canvas.drawRect(Rect(0, 0, getWidth(), getHeight()), backgroundPaint)
        val (height, width) = getHeightAndWidthForPercentage(rectangleChipPercentage)

        canvas.drawRect(Rect(0, getHeight() - height, getWidth(), getHeight()), foregroundPaint)

        if (width != 0) {
            canvas.drawRect(
                Rect(
                    getWidth() - width,
                    (getHeight() * 0.8).toInt() - height,
                    getWidth(),
                    getHeight()
                ), foregroundPaint
            )
        }
    }

    override fun onLevelChange(level: Int): Boolean {
        require(level in 0..100) {
            error("The level should be between 0 and 100")
        }

        secondRectangleAnimator.cancel()
        secondRectangleAnimator.setIntValues(rectangleChipPercentage, level)
        secondRectangleAnimator.start()

        return true
    }

    private fun getHeightAndWidthForPercentage(percentage: Int): Pair<Int, Int> {
        // height of 'whole' rectangles (each representing 20% of the view)
        val wholeRectanglesHeight = (percentage / 20) * (getHeight() / 5)

        // chipped width in the last row
        val chippedWidth = (percentage % 20) * (getWidth() / 20)

        return Pair(wholeRectanglesHeight, chippedWidth)
    }
}
