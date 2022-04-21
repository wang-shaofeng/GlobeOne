/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.boosterdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.databinding.ApplicationItemLayoutBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.shop.promo.filter.AppDetailsItem
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ApplicationItem
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class BoosterApplicationsRecyclerViewAdapter :
    ListAdapter<AppDetailsItem, RecyclerViewHolderBinding<ApplicationItemLayoutBinding>>(
        object : DiffUtil.ItemCallback<AppDetailsItem>() {
            override fun areItemsTheSame(
                oldItem: AppDetailsItem,
                newItem: AppDetailsItem
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: AppDetailsItem,
                newItem: AppDetailsItem
            ): Boolean =
                oldItem == newItem

        }) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<ApplicationItemLayoutBinding> =
        RecyclerViewHolderBinding(
            ApplicationItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ApplicationItemLayoutBinding>,
        position: Int
    ) {
        val item = getItem(position)
        with(holder.viewBinding) {
            tvAppName.text = item.appName
            GlobeGlide.with(ivAppIcon).load(item.appIcon).into(ivAppIcon)

            if (position == currentList.size - 1) vBottomLine.visibility = View.GONE
            else vBottomLine.visibility = View.VISIBLE
        }
    }


    override fun onViewRecycled(holder: RecyclerViewHolderBinding<ApplicationItemLayoutBinding>) {
        super.onViewRecycled(holder)

        GlobeGlide.with(holder.viewBinding.ivAppIcon).clear(holder.viewBinding.ivAppIcon)
    }
}
