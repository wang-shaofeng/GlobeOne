/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ProfileSubsectionItemBinding

class ProfileSubsectionItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ProfileSubsectionItemBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ProfileSubsectionItem,
            defStyleAttr, 0
        ).apply {
            try {
                binding.tvTitle.text = getString(R.styleable.ProfileSubsectionItem_title) ?: ""
            } finally {
                recycle()
            }
        }
    }

    fun setBadgeValue(value: Int) {
        if (value > 0) {
            binding.flBadgeContainer.visibility = View.VISIBLE
            binding.tvBadge.text = value.toString()
        }
    }

    fun setSubtitle(subtitle: String, isWarning: Boolean = false) {
        binding.tvSubtitle.text = subtitle
        binding.tvSubtitle.visibility = View.VISIBLE
        if (isWarning) {
            binding.tvSubtitle.setTypeface(binding.tvSubtitle.typeface, Typeface.BOLD)
            binding.tvSubtitle.setTextColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.state_caution_orange,
                    null
                )
            )
        }
    }
}
