/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.animation.ObjectAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.Keep

class CircularUsageDrawable constructor(
    backgroundColor: Int,
    backgroundCircleColor: Int,
    foregroundCircleColor: Int,
    private val ringWidth: Int,
    private val ringMarginStartEnd: Int,
    private val ringMarginTop: Int
) : Drawable() {

    private var circleFillAngle: Float = 0f
        @Keep
        set(value) {
            field = value

            callback?.invalidateDrawable(this) ?: {
                foregroundCircleAnimator.cancel()
            }
        }

    private val foregroundCircleAnimator =
        ObjectAnimator.ofFloat(this, "circleFillAngle", 0f, 0f)

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundColor
    }

    private val backgroundCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = backgroundCircleColor
        strokeWidth = ringWidth.toFloat()
    }

    private val foregroundCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = foregroundCircleColor
        strokeWidth = ringWidth.toFloat()
    }

    private val oval = RectF()
    private var initialised = false

    override fun draw(canvas: Canvas) {
        canvas.drawRect(Rect(0, 0, getWidth(), getHeight()), backgroundPaint)

        if (!initialised) {
            initialised = true
            val center_x = (getWidth() / 2).toFloat()
            val center_y = (0.6f * getHeight()) / 2 + ringMarginTop
            val radius: Float = (getWidth() - 2 * ringMarginStartEnd) / 2f - ringWidth / 2f
            oval.set(
                center_x - radius,
                center_y - radius,
                center_x + radius,
                center_y + radius
            )
        }

        canvas.drawArc(oval, 0f, 360f, false, backgroundCirclePaint)

        canvas.drawArc(
            oval,
            0 - 90f - 0.5f,
            circleFillAngle + 0.5f,
            false,
            foregroundCirclePaint
        )
    }

    override fun setAlpha(alpha: Int) {
        backgroundCirclePaint.alpha = alpha
        foregroundCirclePaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backgroundCirclePaint.colorFilter = colorFilter
        foregroundCirclePaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int =
        PixelFormat.TRANSLUCENT

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        initialised = false
    }

    override fun onLevelChange(level: Int): Boolean {
        require(level in 0..100) {
            error("The level should be between 0 and 100")
        }

        foregroundCircleAnimator.cancel()
        foregroundCircleAnimator.setFloatValues(circleFillAngle, calculateAngleFromPercent(level))
        foregroundCircleAnimator.start()

        return true
    }

    // 1% of circle is 3.6 degrees
    private fun calculateAngleFromPercent(percentage: Int): Float =
        (3.6f * percentage)

    private fun getHeight(): Int =
        (bounds.bottom - bounds.top)

    private fun getWidth(): Int =
        (bounds.right - bounds.left)
}
