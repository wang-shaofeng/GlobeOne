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
import ph.com.globe.globeonesuperapp.databinding.ChipImageLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.DataUsageViewBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.model.account.percentage
import ph.com.globe.model.shop.domain_models.AppItem
import ph.com.globe.model.util.getDataUsageAmount
import ph.com.globe.model.util.toFormattedConsumption
import kotlin.math.roundToInt

class DataUsageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = DataUsageViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun setContent(
        left: Int,
        total: Int,
        formattedDate: String,
        apps: List<AppItem>?,
        isUnlimited: Boolean = false,
        learnMoreCallback: () -> Unit = {}
    ) {
        with(binding) {
            val percentage = percentage(left, total) / 100f
            val viewParams = vUsedIndicatior.layoutParams
            viewParams.height =
                (percentage * resources.getDimensionPixelSize(R.dimen.data_consumption_view_height)).roundToInt()
            vUsedIndicatior.layoutParams = viewParams
            tvDataUsageUsage.text = if (isUnlimited) {
                resources.getString(R.string.unli)
            } else {
                getDataUsageAmount(left, total).toFormattedConsumption()
            }
            tvDataUsageExpiration.text = if (isUnlimited) {
                resources.getString(R.string.enjoy_your_surfing)
            } else formattedDate

            ivUnlimited.isVisible = isUnlimited
            clUnlimitedInfo.isVisible = isUnlimited
            if (isUnlimited) {
                tvLearnMore.setOnClickListener {
                    learnMoreCallback.invoke()
                }
            }

            apps?.let {
                for (app in apps) {
                    val chip = ChipImageLayoutBinding.inflate(LayoutInflater.from(root.context))
                    GlobeGlide.with(chip.ivChipImage).load(app.appIcon).into(chip.ivChipImage)
                    cgAppImages.addView(chip.root)
                }
            }
        }
    }

    fun getUsageInfo(): String = with(binding) {
        tvDataUsageUsage.text.toString()
    }
}
