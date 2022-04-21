/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat.getColor
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectItemBingoViewBinding

class SelectItemBingoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = "SelectItemBingoView"

    private val binding = SelectItemBingoViewBinding.inflate(LayoutInflater.from(context), this)

    private var selectedOptionIndex = -1

    var optionValues = listOf<Int>()
        set(value) {
            if (value.size == 9 || value.size == 6 || value.size == 3) {
                field = value
                optionValues.forEachIndexed { i, _ ->
                    optionTextViews[i].text =
                        optionValues[i].toString()
                    optionLayouts[i].visibility = View.VISIBLE
                    optionLayouts[i].showAsItemSelected(
                        false,
                        optionTextViews[i],
                        optionDescriptionTextViews[i]
                    )
                }
            } else Log.w(TAG, "Provided options must contain either 3 or 6 or 9 values")
        }

    var optionDescription: String = ""
        set(value) {
            field = value
            optionDescriptionTextViews.forEach { textView ->
                textView.text = value
            }
        }

    private var optionLayouts: List<LinearLayout>
    private var optionTextViews: List<TextView>
    private var optionDescriptionTextViews: List<TextView>

    private var onLoadOptionSelectedListener: ((Int) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BingoView,
            defStyleAttr, 0
        ).apply {
            try {
                with(binding) {
                    optionTextViews = listOf(
                        tvOptionView0,
                        tvOptionView1,
                        tvOptionView2,
                        tvOptionView3,
                        tvOptionView4,
                        tvOptionView5,
                        tvOptionView6,
                        tvOptionView7,
                        tvOptionView8
                    )
                    optionDescriptionTextViews = listOf(
                        tvOptionView0Description,
                        tvOptionView1Description,
                        tvOptionView2Description,
                        tvOptionView3Description,
                        tvOptionView4Description,
                        tvOptionView5Description,
                        tvOptionView6Description,
                        tvOptionView7Description,
                        tvOptionView8Description
                    )
                    optionLayouts = listOf(
                        llOptionView0,
                        llOptionView1,
                        llOptionView2,
                        llOptionView3,
                        llOptionView4,
                        llOptionView5,
                        llOptionView6,
                        llOptionView7,
                        llOptionView8
                    )
                }
                optionLayouts.forEachIndexed { i, ll -> ll.setOnClickListener { selectOption(i) } }
            } finally {
                recycle()
            }
        }
    }

    fun selectOption(index: Int) {
        if (index in 0 until 9) {
            if (selectedOptionIndex != -1)
                optionLayouts[selectedOptionIndex].showAsItemSelected(
                    false,
                    optionTextViews[selectedOptionIndex],
                    optionDescriptionTextViews[selectedOptionIndex]
                )
            optionLayouts[index].showAsItemSelected(
                true,
                optionTextViews[index],
                optionDescriptionTextViews[index]
            )
            selectedOptionIndex = index
            onLoadOptionSelectedListener?.invoke(optionValues[index])
        }
    }

    fun unselectAll() {
        if (selectedOptionIndex in 0 until 9) {
            optionLayouts[selectedOptionIndex].showAsItemSelected(
                false,
                optionTextViews[selectedOptionIndex],
                optionDescriptionTextViews[selectedOptionIndex]
            )
            selectedOptionIndex = -1
        }
    }

    fun setOnAmountSelectedListener(listener: ((Int) -> Unit)?) {
        onLoadOptionSelectedListener = listener
    }

    fun anySelected(): Boolean = selectedOptionIndex != -1
}

fun LinearLayout.showAsItemSelected(
    show: Boolean,
    correspondingTextView: TextView,
    correspondingDescriptionTextView: TextView
) {
    background = when (show) {
        true -> AppCompatResources.getDrawable(context, R.drawable.ic_go_create_option_selected)
        false -> AppCompatResources.getDrawable(context, R.drawable.ic_go_create_option_default)
    }
    correspondingTextView.setTextColor(
        getColor(
            resources,
            if (show) R.color.absolute_white else R.color.neutral_B_0,
            null
        )
    )
    correspondingDescriptionTextView.setTextColor(
        getColor(
            resources,
            if (show) R.color.absolute_white else R.color.neutral_B_0,
            null
        )
    )
}
