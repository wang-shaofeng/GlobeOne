/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.WayfinderViewBinding

class Wayfinder @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = WayfinderViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.Wayfinder,
            defStyleAttr, 0
        ).apply {
            try {
                with(binding) {
                    tvTitle.text = getString(R.styleable.Wayfinder_label) ?: ""
                    tvTitle.isAllCaps = getBoolean(R.styleable.Wayfinder_allCaps, false)
                    ivClose.isVisible = getBoolean(R.styleable.Wayfinder_showCloseImage, false)
                }
            } finally {
                recycle()
            }
        }
    }

    fun onBack(callback: () -> Unit) {
        binding.ivBackArrow.setOnClickListener {
            callback.invoke()
        }
    }

    fun setLabel(label: String) {
        binding.tvTitle.text = label
    }

    fun setAllCaps(isAllCaps: Boolean) {
        binding.tvTitle.isAllCaps = isAllCaps
    }

    fun setCloseImageVisibility(visible: Boolean) {
        binding.ivClose.isVisible = visible
    }

    fun onClose(callback: () -> Unit) {
        binding.ivClose.setOnClickListener {
            callback.invoke()
        }
    }
}
