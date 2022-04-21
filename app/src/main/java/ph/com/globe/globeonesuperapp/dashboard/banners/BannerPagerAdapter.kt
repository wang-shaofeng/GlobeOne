/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard.banners

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.databinding.DashboardBannerItemLayoutBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.banners.BannerModel
import ph.com.globe.model.banners.CTAType

class BannerPagerAdapter(
    private val bannerCallback: BannerAdapterCallback
) :
    ListAdapter<BannerModel, RecyclerViewHolderBinding<DashboardBannerItemLayoutBinding>>(
        bannerItemDiffUtil
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<DashboardBannerItemLayoutBinding> =
        RecyclerViewHolderBinding(
            DashboardBannerItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<DashboardBannerItemLayoutBinding>,
        position: Int
    ) {

        val item = getItem(position)
        with(holder.viewBinding) {
            with(item) {
                title?.let {
                    tvTitle.visibility = View.VISIBLE
                    tvTitle.text = it
                } ?: run { tvTitle.visibility = View.INVISIBLE }

                subtext?.let {
                    tvSubtext.visibility = View.VISIBLE
                    tvSubtext.text = it
                } ?: run { tvSubtext.visibility = View.INVISIBLE }

                primaryCTA?.let {
                    btnFirst.visibility = View.VISIBLE
                    btnFirst.text = it
                    btnFirst.setOnClickListener {
                        bannerCallback.buttonClicked(primaryCTAType, primaryCTALink)
                    }
                } ?: run { btnFirst.visibility = View.GONE }

                secondaryCTA?.let {
                    btnSecond.visibility = View.VISIBLE
                    btnSecond.text = it
                    btnSecond.setOnClickListener {
                        bannerCallback.buttonClicked(secondaryCTAType, secondaryCTALink)
                    }
                } ?: run { btnSecond.visibility = View.GONE }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<DashboardBannerItemLayoutBinding>) {
        super.onViewRecycled(holder)

        with(holder.viewBinding) {
            btnFirst.setOnClickListener(null)
            btnSecond.setOnClickListener(null)
        }
    }

    fun getItemAtPosition(position: Int): BannerModel {
        return super.getItem(position)
    }
}

val bannerItemDiffUtil = object : DiffUtil.ItemCallback<BannerModel>() {

    override fun areItemsTheSame(
        oldItem: BannerModel,
        newItem: BannerModel
    ) = oldItem == newItem

    override fun areContentsTheSame(
        oldItem: BannerModel,
        newItem: BannerModel
    ) = oldItem == newItem
}

interface BannerAdapterCallback {
    fun setBannerBackground(imageUrl: String?)
    fun buttonClicked(type: CTAType?, action: String?)
}
