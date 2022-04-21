/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

// This class handles ViewPager2 height when it contains fragments with different heights
class ViewPager2ViewHeightAnimator {

    var viewPager2: ViewPager2? = null
        set(value) {
            if (field != value) {
                field?.unregisterOnPageChangeCallback(onPageChangeCallback)
                field = value
                value?.registerOnPageChangeCallback(onPageChangeCallback)
            }
        }

    private val layoutManager: LinearLayoutManager?
        get() = (viewPager2?.getChildAt(0) as? RecyclerView)?.layoutManager as? LinearLayoutManager


    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            recalculate(position)
        }
    }

    fun recalculate(position: Int) = layoutManager?.apply {
        val leftView = findViewByPosition(position) ?: return@apply
        viewPager2?.apply {
            val leftHeight = getMeasuredViewHeightFor(leftView)
            layoutParams = layoutParams.apply {
                height = leftHeight
            }
            invalidate()
        }
    }

    private fun getMeasuredViewHeightFor(view: View): Int {
        val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
        val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(wMeasureSpec, hMeasureSpec)
        return view.measuredHeight
    }
}
