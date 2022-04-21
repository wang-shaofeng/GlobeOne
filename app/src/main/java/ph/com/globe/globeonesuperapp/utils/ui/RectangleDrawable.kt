/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

abstract class RectangleDrawable(
    backgroundColor: Int,
    foregroundColor: Int
) : Drawable() {

    internal val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = backgroundColor
    }

    internal val foregroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = foregroundColor
    }

    override fun setAlpha(alpha: Int) {
        backgroundPaint.alpha = alpha
        foregroundPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        backgroundPaint.colorFilter = colorFilter
        foregroundPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int =
        PixelFormat.TRANSLUCENT

    internal fun getHeight(): Int =
        (bounds.bottom - bounds.top)

    internal fun getWidth(): Int =
        (bounds.right - bounds.left)
}
