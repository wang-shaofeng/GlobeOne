/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.animation.Animator
import android.animation.AnimatorInflater.loadAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.Gravity.CENTER
import android.view.View
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import androidx.viewpager2.widget.ViewPager2
import ph.com.globe.globeonesuperapp.R
import java.lang.Math.abs

class DotsIndicator2(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    private var viewPager: ViewPager2? = null

    private var indicatorMargin = -1
    private var indicatorWidth = -1
    private var indicatorHeight = -1

    private var indicatorBackgroundResId: Int = 0
    private var indicatorUnselectedBackgroundResId: Int = 0

    private var animatorOut: Animator
    private var animatorIn: Animator
    private var immediateAnimatorOut: Animator
    private var immediateAnimatorIn: Animator

    private var lastPosition = -1

    private var animatorResId: Int = 0
    private var animatorReverseResId: Int = 0
    private var backgroundResId: Int = 0
    private var unselectedBackgroundId: Int = 0
    private var dotTint: Int = 0

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DotsIndicator2)
        val intrinsicWidth: Int
        val intrinsicHeight: Int
        val intrinsicMargin: Int
        val intrinsicOrientation: Int
        val intrinsicGravity: Int

        try {
            intrinsicWidth = ta.getDimensionPixelSize(R.styleable.DotsIndicator2_dot_width, -1)
            intrinsicHeight = ta.getDimensionPixelSize(R.styleable.DotsIndicator2_dot_height, -1)
            intrinsicMargin = ta.getDimensionPixelSize(R.styleable.DotsIndicator2_dot_margin, -1)
            intrinsicOrientation = ta.getInt(R.styleable.DotsIndicator2_dots_orientation, -1)
            intrinsicGravity = ta.getInt(R.styleable.DotsIndicator2_dots_gravity, -1)

            this.animatorResId =
                ta.getResourceId(R.styleable.DotsIndicator2_dots_animator, 0)
            this.animatorReverseResId =
                ta.getResourceId(R.styleable.DotsIndicator2_dots_animator_reverse, 0)
            this.backgroundResId = ta.getResourceId(
                R.styleable.DotsIndicator2_dot_drawable,
                R.drawable.inactive_dot
            )
            this.unselectedBackgroundId = ta.getResourceId(
                R.styleable.DotsIndicator2_dot_drawable_unselected,
                this.backgroundResId
            )
            this.dotTint = ta.getColor(R.styleable.DotsIndicator2_dot_tint, 0)
        } finally {
            ta.recycle()
        }

        val miniSize = (TypedValue.applyDimension(
            COMPLEX_UNIT_DIP,
            DEFAULT_INDICATOR_WIDTH.toFloat(),
            resources.displayMetrics
        ) + 0.5f).toInt()
        indicatorWidth = if (intrinsicWidth < 0) miniSize else intrinsicWidth
        indicatorHeight = if (intrinsicHeight < 0) miniSize else intrinsicHeight
        indicatorMargin = if (intrinsicMargin < 0) miniSize else intrinsicMargin

        animatorOut = createAnimatorOut()
        immediateAnimatorOut = createAnimatorOut()
        immediateAnimatorOut.duration = 0

        animatorIn = createAnimatorIn()
        immediateAnimatorIn = createAnimatorIn()
        immediateAnimatorIn.duration = 0

        indicatorBackgroundResId =
            if (this.backgroundResId == 0) R.drawable.inactive_dot else this.backgroundResId
        indicatorUnselectedBackgroundResId =
            if (this.unselectedBackgroundId == 0) this.backgroundResId else this.unselectedBackgroundId

        orientation =
            if (intrinsicOrientation == VERTICAL) VERTICAL else HORIZONTAL
        gravity =
            if (intrinsicGravity >= 0) intrinsicGravity else CENTER
    }

    fun setDotDrawable(
        @DrawableRes indicatorRes: Int,
        @DrawableRes unselectedIndicatorRes: Int = indicatorRes
    ) {
        this.backgroundResId = indicatorRes
        this.unselectedBackgroundId = unselectedIndicatorRes
        invalidateDots()
    }

    fun setDotTint(@ColorInt tint: Int) {
        this.dotTint = tint
        invalidateDots()
    }

    fun setDotTintRes(@ColorRes tintRes: Int) = setDotTint(getColor(context, tintRes))

    fun attachViewPager(viewPager: ViewPager2?) {
        this.viewPager = viewPager
        this.viewPager?.let {
            if (it.adapter != null) {
                lastPosition = -1
                createIndicators()
                it.unregisterOnPageChangeCallback(internalPageChangeListener)
                it.registerOnPageChangeCallback(internalPageChangeListener)
                internalPageChangeListener.onPageSelected(it.currentItem)
            }
        }
    }

    private fun invalidateDots() {
        for (i in 0 until childCount) {
            val indicator = getChildAt(i)
            val bgDrawableRes =
                if (currentItem() == i) indicatorBackgroundResId else indicatorUnselectedBackgroundResId
            var bgDrawable = getDrawable(context, bgDrawableRes)
            if (this.dotTint != 0) {
                bgDrawable = bgDrawable?.tint(this.dotTint)
            }
            indicator.background = bgDrawable
        }
    }

    private fun createIndicators(count: Int) {
        for (i in 0 until count) {
            val bgDrawable =
                if (currentItem() == i) indicatorBackgroundResId else indicatorUnselectedBackgroundResId
            val animator =
                if (currentItem() == i) immediateAnimatorOut else immediateAnimatorIn
            addIndicator(
                orientation = orientation,
                drawableRes = bgDrawable,
                animator = animator
            )
        }
    }

    private fun internalPageSelected(position: Int) {
        if (animatorIn.isRunning) {
            animatorIn.end()
            animatorIn.cancel()
        }
        if (animatorOut.isRunning) {
            animatorOut.end()
            animatorOut.cancel()
        }
        val currentIndicator = if (lastPosition >= 0) getChildAt(lastPosition) else null
        if (currentIndicator != null) {
            currentIndicator.setBackgroundResource(indicatorUnselectedBackgroundResId)
            animatorIn.setTarget(currentIndicator)
            animatorIn.start()
        }
        val selectedIndicator = getChildAt(position)
        if (selectedIndicator != null) {
            selectedIndicator.setBackgroundResource(indicatorBackgroundResId)
            animatorOut.setTarget(selectedIndicator)
            animatorOut.start()
        }
    }

    private fun createIndicators() {
        removeAllViews()
        val adapter = viewPager!!.adapter
        val count = adapter?.itemCount ?: 0
        if (count <= 0) return
        createIndicators(count)
    }

    private fun addIndicator(
        orientation: Int,
        @DrawableRes drawableRes: Int,
        animator: Animator
    ) {
        if (animator.isRunning) {
            animator.end()
            animator.cancel()
        }
        val indicator = View(context)

        var bgDrawable = getDrawable(context, drawableRes)
        if (this.dotTint != 0) {
            bgDrawable = bgDrawable?.tint(this.dotTint)
        }
        indicator.background = bgDrawable

        addView(indicator, indicatorWidth, indicatorHeight)
        val lp = indicator.layoutParams as LinearLayout.LayoutParams

        if (orientation == LinearLayout.HORIZONTAL) {
            lp.leftMargin = indicatorMargin
            lp.rightMargin = indicatorMargin
        } else {
            lp.topMargin = indicatorMargin
            lp.bottomMargin = indicatorMargin
        }

        indicator.layoutParams = lp
        animator.setTarget(indicator)
        animator.start()
    }

    private fun createEmptyAnimator() = ValueAnimator.ofInt(0, 0)

    private fun createAnimatorOut(): Animator =
        if (animatorResId != 0) {
            loadAnimator(context, animatorResId)
        } else {
            createEmptyAnimator()
        }

    private fun createAnimatorIn(): Animator {
        val animatorIn: Animator
        if (this.animatorReverseResId == 0) {
            animatorIn = createAnimatorOut()
            animatorIn.interpolator = ReverseInterpolator()
        } else {
            animatorIn = loadAnimator(context, this.animatorReverseResId)
        }
        return animatorIn
    }

    private fun currentItem() = viewPager?.currentItem ?: -1

    private val internalPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            if ((viewPager?.adapter?.itemCount ?: 0) <= 0) return
            internalPageSelected(position)
            lastPosition = position
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) = Unit

        override fun onPageScrollStateChanged(state: Int) = Unit
    }

    private inner class ReverseInterpolator : Interpolator {
        override fun getInterpolation(value: Float) = abs(1.0f - value)
    }

    companion object {
        private const val DEFAULT_INDICATOR_WIDTH = 5
    }
}

private fun Drawable.tint(@ColorInt color: Int): Drawable {
    val wrapped = DrawableCompat.wrap(this)
    DrawableCompat.setTint(wrapped, color)
    return wrapped
}
