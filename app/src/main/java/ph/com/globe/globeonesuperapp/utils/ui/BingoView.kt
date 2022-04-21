/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat.getColor
import androidx.core.view.isVisible
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.BingoViewBinding
import ph.com.globe.globeonesuperapp.shop.load.ShopLoadFragmentFull
import ph.com.globe.globeonesuperapp.shop.load.ShopLoadFragmentFull.Companion.DISCOUNT
import ph.com.globe.globeonesuperapp.utils.balance.calculateDiscountPrice
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayPrice
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat

class BingoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val TAG = "BingoView"

    private val binding = BingoViewBinding.inflate(LayoutInflater.from(context), this)

    private var selectedOptionIndex = -1

    var checkedType = ShopLoadFragmentFull.Companion.CheckedType.PERSONAL

    var optionValues = listOf<Int>()
        set(value) {
            // support options size in range(0,9)
            if (value.size <= 9) {
                field = value
                optionViews.forEachIndexed { i, constraintLayout ->
                    with(constraintLayout) {
                        if (i < value.size) {
                            showAsSelected(false)

                            optionTextViews[i][0].text = resources.getString(
                                R.string.pezos_prefix,
                                optionValues[i].formatOptionValue()
                            )
                            optionTextViews[i][1].text = resources.getString(
                                R.string.pezos_prefix,
                                optionValues[i].formatOptionValue()
                            )
                            optionTextViews[i][0].showAsSelected(false)
                            optionTextViews[i][1].showAsSelected(false)
                            optionTextViews[i][1].isVisible = false
                            visibility = View.VISIBLE
                        } else {
                            visibility = View.GONE
                        }
                    }
                }
            } else Log.w(TAG, "Provided options must be less than or equal to 9 values")
        }

    private var optionViews: List<ConstraintLayout>
    private var optionTextViews: List<List<TextView>>

    private var onLoadOptionSelectedListener: ((Int) -> Unit)? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BingoView,
            defStyleAttr, 0
        ).apply {
            try {
                with(binding) {
                    optionViews = listOf(
                        clOptionView0,
                        clOptionView1,
                        clOptionView2,
                        clOptionView3,
                        clOptionView4,
                        clOptionView5,
                        clOptionView6,
                        clOptionView7,
                        clOptionView8
                    )

                    optionTextViews = listOf(
                        listOf(tvOptionViewDiscount0, tvOptionViewInit0),
                        listOf(tvOptionViewDiscount1, tvOptionViewInit1),
                        listOf(tvOptionViewDiscount2, tvOptionViewInit2),
                        listOf(tvOptionViewDiscount3, tvOptionViewInit3),
                        listOf(tvOptionViewDiscount4, tvOptionViewInit4),
                        listOf(tvOptionViewDiscount5, tvOptionViewInit5),
                        listOf(tvOptionViewDiscount6, tvOptionViewInit6),
                        listOf(tvOptionViewDiscount7, tvOptionViewInit7),
                        listOf(tvOptionViewDiscount8, tvOptionViewInit8),
                    )
                }

                optionTextViews.forEach { it[1].paint.flags = Paint.STRIKE_THRU_TEXT_FLAG }
                optionViews.forEachIndexed { i, cl -> cl.setOnClickListener { selectOption(i) } }
            } finally {
                recycle()
            }
        }
    }

    fun select(value: Int) {
        val index = optionValues.indexOf(value)
        selectOption(index)
    }

    private fun selectOption(index: Int) {
        if (index == selectedOptionIndex) {
            return
        }

        if (index in 0 until 9) {
            if (selectedOptionIndex != -1 && selectedOptionIndex in optionValues.indices) {
                optionViews[selectedOptionIndex].showAsSelected(false)
                optionTextViews[selectedOptionIndex][0].showAsSelected(false)
                optionTextViews[selectedOptionIndex][0].text = resources.getString(
                    R.string.pezos_prefix,
                    optionValues[selectedOptionIndex].formatOptionValue()
                )
                optionTextViews[selectedOptionIndex][1].showAsSelected(false)
                optionTextViews[selectedOptionIndex][1].isVisible = false
            }
            optionViews[index].showAsSelected(true)
            optionTextViews[index][0].showAsSelected(true)
            optionTextViews[index][0].text = resources.getString(
                R.string.pezos_prefix,
                if (checkedType == ShopLoadFragmentFull.Companion.CheckedType.PERSONAL) calculateDiscountPrice(
                    optionValues[index].toDouble(),
                    DISCOUNT
                )
                    .toFormattedDisplayPrice() else {
                    optionValues[index].formatOptionValue()
                }
            )
            optionTextViews[index][1].isVisible =
                checkedType == ShopLoadFragmentFull.Companion.CheckedType.PERSONAL
            optionTextViews[index][1].showAsSelected(true)
            selectedOptionIndex = index
            onLoadOptionSelectedListener?.invoke(optionValues[index])
        }
    }

    fun unselectAll() {
        if (selectedOptionIndex in 0 until 9) {
            optionViews[selectedOptionIndex].showAsSelected(false)
            optionTextViews[selectedOptionIndex][0].showAsSelected(false)
            optionTextViews[selectedOptionIndex][0].text = resources.getString(
                R.string.pezos_prefix,
                optionValues[selectedOptionIndex].formatOptionValue()
            )
            optionTextViews[selectedOptionIndex][1].showAsSelected(false)
            optionTextViews[selectedOptionIndex][1].isVisible = false
            selectedOptionIndex = -1
        }
    }

    fun setOnAmountSelectedListener(listener: ((Int) -> Unit)?) {
        onLoadOptionSelectedListener = listener
    }
}

fun ConstraintLayout.showAsSelected(show: Boolean) {
    background = when (show) {
        true -> AppCompatResources.getDrawable(context, R.drawable.ic_load_option_selected)
        false -> AppCompatResources.getDrawable(context, R.drawable.ic_load_option_default)
    }
}

fun TextView.showAsSelected(show: Boolean) {
    setTextColor(
        getColor(
            resources,
            if (show) R.color.absolute_white else R.color.neutral_A_0,
            null
        )
    )
}

fun Int.formatOptionValue(): String {
    if (this < 1000) {
        return this.toString()
    }
    val valueString = this.toString()
    return valueString.first() + "," + valueString.substring(1)
}
