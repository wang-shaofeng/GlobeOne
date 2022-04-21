/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.customview.widget.ViewDragHelper.INVALID_POINTER
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.RefreshLayoutBinding
import kotlin.math.*

class RefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val DRAG_RATE = .5f
    private val MAX_OFFSET_ANIMATION_DURATION = 700
    private val DECELERATE_INTERPOLATION_FACTOR = 2f

    var mDecelerateInterpolator: Interpolator? = null
    private var mTarget: View? = null
    private var mRefreshing = false
    private var mNotify = false
    private var mCurrentOffsetTop = 0
    private var mActivePointerId = 0
    private var mIsBeingDragged = false
    private var mTouchSlop = 0
    private var mCurrentDragPercent = 0f
    private var mTotalDragDistance = 0
    private var mInitialMotionY = 0
    private var mFrom: Int = 0
    private var mFromDragPercent = 0f

    private var mBar = RefreshView(context)
    private var refreshAction: () -> Unit = { }

    init {
        mDecelerateInterpolator = DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR)
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        setRefreshing(false)

        addView(mBar)

        mTotalDragDistance = resources.getDimension(R.dimen.refresh_layout_drag_distance).toInt()
        setBackgroundColor(Color.WHITE)

        setWillNotDraw(false)
        isChildrenDrawingOrderEnabled = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        ensureTarget()

        val width = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
        val height = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)

        mTarget?.measure(width, height)
        mBar.measure(width, height)
    }

    private fun ensureTarget() {
        if (mTarget != null)
            return

        if (childCount > 0) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child != mBar) {
                    mTarget = child
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {

        if (!isEnabled || canChildScrollUp() || mRefreshing) {
            return false
        }

        when (ev?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                setTargetOffsetTop(0)
                mActivePointerId = ev.getPointerId(0)
                mIsBeingDragged = false
                val initialMotionY = getMotionEventY(ev, mActivePointerId)
                if (initialMotionY == -1) {
                    return false
                }
                mInitialMotionY = initialMotionY
            }
            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId == INVALID_POINTER) {
                    return false
                }

                val y = getMotionEventY(ev, mActivePointerId)
                if (y == -1) {
                    return false
                }

                val yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsBeingDragged = false
                mActivePointerId = INVALID_POINTER
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
            }
        }
        return mIsBeingDragged
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {

        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev)
        }

        when (ev?.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex < 0) {
                    return false
                }
                val y = ev.getY(pointerIndex)
                val yDiff = y - mInitialMotionY
                val scrollTop = yDiff * DRAG_RATE
                mCurrentDragPercent = scrollTop / mTotalDragDistance
                if (mCurrentDragPercent < 0) {
                    return false
                }
                val boundedDragPercent = min(1f, abs(mCurrentDragPercent))
                val extraOS = abs(scrollTop) - mTotalDragDistance
                val slingshotDist = mTotalDragDistance.toFloat()
                val tensionSlingshotPercent =
                    max(0f, min(extraOS, slingshotDist * 2) / slingshotDist)
                val tensionPercent =
                    ((tensionSlingshotPercent / 4) - (tensionSlingshotPercent / 4).pow(2)) * 2f
                val extraMove = slingshotDist * tensionPercent / 2
                val targetY = (slingshotDist * boundedDragPercent + extraMove).toInt()

                val offsetScrollTop = scrollTop - (mTotalDragDistance / 2)
                if (offsetScrollTop > 0) {
                    mCurrentDragPercent = offsetScrollTop / mTotalDragDistance * 2
                }
                setTargetOffsetTop(targetY - mCurrentOffsetTop)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mActivePointerId = ev.getPointerId(index)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
            }
            MotionEvent.ACTION_UP -> {
                if (mActivePointerId == INVALID_POINTER) {
                    return false
                }
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                val y = ev.getY(pointerIndex)
                val overScrollTop = (y - mInitialMotionY) * DRAG_RATE
                mIsBeingDragged = false
                if (overScrollTop > mTotalDragDistance) {
                    setRefreshing(true, true)
                } else {
                    mRefreshing = false
                    animateOffsetToStartPosition()
                }
                mActivePointerId = INVALID_POINTER
                return false
            }
        }

        return true
    }

    private fun canChildScrollUp(): Boolean {
        return mTarget?.canScrollVertically(-1) ?: false
    }

    private fun setTargetOffsetTop(offset: Int) {
        mTarget?.offsetTopAndBottom(offset)
        mCurrentOffsetTop = mTarget?.top!!
    }

    private fun getMotionEventY(ev: MotionEvent, activePointerId: Int): Int {
        val index = ev.findPointerIndex(activePointerId)
        if (index < 0)
            return -1

        return ev.getY(index).toInt()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun moveToStart(interpolatedTime: Float) {
        val targetTop = mFrom - (mFrom * interpolatedTime).toInt()
        val targetPercent = mFromDragPercent * (1.0f - interpolatedTime)
        val offset = targetTop - mTarget?.top!!
        mCurrentDragPercent = targetPercent
        setTargetOffsetTop(offset)
    }

    fun setRefreshing(refreshing: Boolean) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing, false /* notify */)
        }
    }

    private fun setRefreshing(refreshing: Boolean, notify: Boolean) {
        if (mRefreshing != refreshing) {
            mNotify = notify
            ensureTarget()
            mRefreshing = refreshing
            if (mRefreshing) {
                animateOffsetToCorrectPosition()
            } else {
                animateOffsetToStartPosition()
            }
        }
    }

    private fun animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop
        mFromDragPercent = mCurrentDragPercent
        val animationDuration = abs((MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent).toLong())

        mAnimateToStartPosition.reset()
        mAnimateToStartPosition.duration = animationDuration
        mAnimateToStartPosition.interpolator = mDecelerateInterpolator
        mAnimateToStartPosition.setAnimationListener(mToStartListener)
        mBar.stop()
        mBar.clearAnimation()
        mBar.startAnimation(mAnimateToStartPosition)
    }

    private fun animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop
        mFromDragPercent = mCurrentDragPercent

        mAnimateToCorrectPosition.reset()
        mAnimateToCorrectPosition.duration = MAX_OFFSET_ANIMATION_DURATION.toLong()
        mAnimateToCorrectPosition.interpolator = mDecelerateInterpolator

        mBar.clearAnimation()
        mBar.startAnimation(mAnimateToCorrectPosition)
        if (mRefreshing) {
            mBar.start()
            if (mNotify) {
                refreshAction.invoke()
            }
        } else {
            mBar.stop()
            animateOffsetToStartPosition()
        }
        mCurrentOffsetTop = mTarget?.top!!
    }

    private val mAnimateToStartPosition = object : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            moveToStart(interpolatedTime)
        }
    }

    private val mAnimateToCorrectPosition = object : Animation() {
        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            val targetTop: Int
            val endTarget = mTotalDragDistance
            targetTop = mFrom + ((endTarget - mFrom) * interpolatedTime).toInt()
            val offset = targetTop - mTarget?.top!!

            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime

            setTargetOffsetTop(offset)
        }
    }

    private val mToStartListener = object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {}

        override fun onAnimationRepeat(animation: Animation) {}

        override fun onAnimationEnd(animation: Animation) {
            mBar.stop()
            mCurrentOffsetTop = mTarget?.top!!
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {

        ensureTarget()
        if (mTarget == null)
            return

        val height = measuredHeight
        val width = measuredWidth
        val left = paddingLeft
        val top = paddingTop
        val right = paddingRight
        val bottom = paddingBottom

        mTarget?.layout(
            left,
            top + mCurrentOffsetTop,
            left + width - right,
            top + height - bottom + mCurrentOffsetTop
        )
        mBar.layout(left, top, left + width - right, top + height - bottom)
    }

    fun setRefreshListener(action: () -> Unit) {
        this.refreshAction = action
    }

    private class RefreshView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : LinearLayout(context, attrs, defStyleAttr) {

        private val binding = RefreshLayoutBinding.inflate(LayoutInflater.from(context), this, true)

        fun start() {
            binding.lavRefresh.playAnimation()
            binding.tvPullToRefresh.visibility = View.INVISIBLE
        }

        fun stop() {
            binding.lavRefresh.cancelAnimation()
            binding.tvPullToRefresh.visibility = View.VISIBLE
        }
    }
}
