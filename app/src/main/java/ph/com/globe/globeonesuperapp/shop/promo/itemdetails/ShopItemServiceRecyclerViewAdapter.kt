/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.itemdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.databinding.ShopItemDetailsItemLayoutBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class ShopItemServiceRecyclerViewAdapter :
    ListAdapter<PromoServiceItem, RecyclerViewHolderBinding<ShopItemDetailsItemLayoutBinding>>(
        object : DiffUtil.ItemCallback<PromoServiceItem>() {

            override fun areItemsTheSame(
                oldItem: PromoServiceItem,
                newItem: PromoServiceItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: PromoServiceItem,
                newItem: PromoServiceItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            ShopItemDetailsItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ShopItemDetailsItemLayoutBinding>,
        position: Int
    ) {
        with(getItem(position)) {
            with(holder.viewBinding) {
                ivServiceIcon.setImageResource(icon)
                tvServiceText.text = serviceInfo
                if (position == currentList.size - 1)
                    vDetailVerticalLine.visibility = View.GONE
            }
        }
    }
}

data class PromoServiceItem(
    val icon: Int,
    val serviceInfo: String
)
