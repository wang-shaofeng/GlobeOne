/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.rewards_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.rewards.allrewards.RewardHolder
import ph.com.globe.model.rewards.RewardsCatalogItem

class RewardsAdapter(private val onClick: (RewardsCatalogItem) -> Unit) :
    ListAdapter<RewardsCatalogItem, RewardHolder>(
        object : DiffUtil.ItemCallback<RewardsCatalogItem>() {
            override fun areItemsTheSame(oldItem: RewardsCatalogItem, newItem: RewardsCatalogItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: RewardsCatalogItem,
                newItem: RewardsCatalogItem
            ) =
                oldItem == newItem
        }
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardHolder =
        RewardHolder.inflate(LayoutInflater.from(parent.context), parent)

    override fun onBindViewHolder(holder: RewardHolder, position: Int) {
        with(getItem(position)) {
            holder.bind(this)
            holder.itemView.setOnClickListener { onClick(this) }
        }
    }
}
