/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import ph.com.globe.globeonesuperapp.R

class RectangleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    var imageView: View

    init {
        View.inflate(context, R.layout.rectangle_view, this)

        imageView = findViewById(R.id.iv_view)
    }

    /**
     * [aspectRatio] is width-to-height ratio of transparent rect.
     */
    val aspectRatio: Float
        get() = width.toFloat() / height.toFloat()

    /**
     * [widthRatio] is ratio between width of transparent rect and width of screen
     */
    val widthRatio: Float
        get() = imageView.width.toFloat() / width.toFloat()

    /**
     * [heightRatio] is ratio between height of transparent rect and height of screen
     */
    val heightRatio: Float
        get() = imageView.height.toFloat() / height.toFloat()
}
