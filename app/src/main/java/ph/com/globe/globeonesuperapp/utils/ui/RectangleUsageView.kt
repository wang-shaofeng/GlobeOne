/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.model.util.UsageAmountUIModel

class RectangleUsageView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    private val rectangleDrawable: RectangleDrawable

    private var detailsChild: View? = null
    private lateinit var tvTitle: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvAdditionalText: TextView
    private lateinit var ivUnlimited: ImageView
    private lateinit var tvNoSubs: TextView
    private lateinit var ivIncluded: ImageView
    private lateinit var groupAmount: Group
    private lateinit var groupIncluded: Group
    private lateinit var groupUnlimited: Group
    private lateinit var usageType: String

    private var currentModel: UsageAmountUIModel? = null

    private var typedArray: TypedArray = context.theme
        .obtainStyledAttributes(
            attrs, R.styleable.RectangleUsageView, 0, 0
        )

    init {
        val backgroundRectangleColor =
            typedArray.getColor(R.styleable.RectangleUsageView_backgroundRectangleColor, 0)
        val foregroundRectangleColor =
            typedArray.getColor(R.styleable.RectangleUsageView_foregroundRectangleColor, 0)

        val fillType = typedArray.getInt(R.styleable.RectangleUsageView_rectangleFillStyle, -1)

        rectangleDrawable = if (fillType == 0) {
            FilledRectangleDrawable(backgroundRectangleColor, foregroundRectangleColor)
        } else {
            ChippedRectangleDrawable(backgroundRectangleColor, foregroundRectangleColor)
        }

        rectangleDrawable.callback = this

        setWillNotDraw(false)
        View.inflate(context, R.layout.dashboard_consumption_internal_layout, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        tvTitle = findViewById(R.id.tv_title)
        tvRemaining = findViewById(R.id.tv_remaining)
        tvTotal = findViewById(R.id.tv_total)
        tvAdditionalText = findViewById(R.id.tv_additional_info)
        ivUnlimited = findViewById(R.id.iv_unlimited)
        tvNoSubs = findViewById(R.id.tv_no_subs)
        ivIncluded = findViewById(R.id.iv_included)
        groupAmount = findViewById(R.id.group_amount)
        groupIncluded = findViewById(R.id.group_included)
        groupUnlimited = findViewById(R.id.group_unlimited)

        setupStyles()

        detailsChild = getChildAt(0)
    }

    private fun setupStyles() {
        val titleStyle = typedArray.getResourceId(R.styleable.RectangleUsageView_textTitleStyle, -1)
        val amountStyle =
            typedArray.getResourceId(R.styleable.RectangleUsageView_textAmountStyle, -1)

        if (titleStyle != -1) {
            tvTitle.setTextAppearance(context, titleStyle)
        }

        if (amountStyle != -1) {
            tvTotal.setTextAppearance(context, amountStyle)
        }

        tvTotal.setTextColor(
            typedArray.getColor(
                R.styleable.RectangleUsageView_adjustedTextColor,
                0
            )
        )

        tvNoSubs.setTextColor(
            typedArray.getColor(
                R.styleable.RectangleUsageView_adjustedTextColor,
                0
            )
        )

        ivUnlimited.setColorFilter(
            typedArray.getColor(
                R.styleable.RectangleUsageView_adjustedTextColor,
                0
            ),
            android.graphics.PorterDuff.Mode.MULTIPLY
        )

        usageType = typedArray.getString(R.styleable.RectangleUsageView_usageType) ?: ""

        val noSubsText = typedArray.getString(R.styleable.RectangleUsageView_emptyMessage)
        tvNoSubs.text = noSubsText

        val additionalText = typedArray.getString(R.styleable.RectangleUsageView_additionalText)
        tvAdditionalText.text = additionalText

        val includedIcon = typedArray.getDrawable(R.styleable.RectangleUsageView_includedIcon)
        ivIncluded.setImageDrawable(includedIcon)

        typedArray.recycle()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        detailsChild!!.layout(0, height - detailsChild!!.measuredHeight, width, height)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        detailsChild!!.measure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === rectangleDrawable || super.verifyDrawable(who)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        rectangleDrawable.setBounds(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom
        )
        rectangleDrawable.draw(canvas)
    }

    fun setUsage(usageModel: UsageAmountUIModel) {
        currentModel = usageModel

        with(usageModel) {
            tvRemaining.text = getRemainingAmount()
            tvTotal.text = getTotalAmount()
            tvTitle.isVisible = hasSubscriptions
            tvNoSubs.isVisible = !hasSubscriptions
            groupAmount.isVisible = !subscriptionsIncluded
            groupIncluded.isVisible = subscriptionsIncluded
            groupUnlimited.isVisible = isUnlimited

            tvTitle.text = when {
                isUnlimited || subscriptionsIncluded -> usageType
                else -> resources.getString(R.string.res_left, usageType)
            }

            rectangleDrawable.level = if (isUnlimited || subscriptionsIncluded)
                100
            else usageAmount.percentage
        }
    }
}
