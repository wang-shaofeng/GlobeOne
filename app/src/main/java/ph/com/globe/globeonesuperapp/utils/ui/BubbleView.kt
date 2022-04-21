/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.utils.ui.BubbleType.*

class BubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val _cardHeight by lazy { context.resources.getDimension(R.dimen.dashboard_account_card_height) }
    private val _cardMargin by lazy { context.resources.getDimension(R.dimen.margin_standard) }
    private val _bubbleMargin by lazy { context.resources.getDimension(R.dimen.dashboard_bubble_margin) }

    init {
        alpha = 0f
        animateToAlpha(1f)
    }

    fun show(parent: ConstraintLayout, adapterPosition: Int, type: BubbleType) {

        setImageResource(
            when (type) {
                BillDueSoon -> R.drawable.tooltip_bill_due_soon
                BillOverdue -> R.drawable.tooltip_bill_overdue
                Surprise -> R.drawable.tooltip_surprise
            }
        )

        val params = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {

            var offset = (adapterPosition * _cardHeight) + (adapterPosition - 1) * _cardMargin

            if (type is Surprise || type is BillOverdue)
                offset += context.resources.getDimension(R.dimen.margin_small)

            topToTop = ConstraintSet.PARENT_ID
            topMargin = offset.toInt()

            if (type is Surprise) {
                startToStart = ConstraintSet.PARENT_ID
                marginStart = _bubbleMargin.toInt()
            } else {
                endToEnd = ConstraintSet.PARENT_ID
                marginEnd = _bubbleMargin.toInt()
            }
        }

        parent.addView(this, params)

        MainScope().launch {
            delay(BUBBLE_VISIBILITY_DURATION)
            animateToAlpha(0f, animationFinishedCallback = {
                parent.removeView(this@BubbleView)
            })
        }
    }

    private fun animateToAlpha(value: Float, animationFinishedCallback: () -> Unit = {}) {
        animate().alpha(value)
            .setDuration(FADE_ANIMATION_DURATION)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    animationFinishedCallback.invoke()
                }
            })
    }
}

private const val FADE_ANIMATION_DURATION = 300L
private const val BUBBLE_VISIBILITY_DURATION = 5000L

sealed class BubbleType {
    object BillDueSoon : BubbleType()
    object BillOverdue : BubbleType()
    object Surprise : BubbleType()
}
