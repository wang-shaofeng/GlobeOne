/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard.raffle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.databinding.RaffleItemBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.profile.response_models.RaffleSet

class RaffleAdapter : ListAdapter<RaffleSet, RecyclerViewHolderBinding<RaffleItemBinding>>(
    object : DiffUtil.ItemCallback<RaffleSet>() {
        override fun areItemsTheSame(oldItem: RaffleSet, newItem: RaffleSet): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: RaffleSet, newItem: RaffleSet): Boolean =
            oldItem == newItem

    }) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<RaffleItemBinding> =
        RecyclerViewHolderBinding(
            RaffleItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<RaffleItemBinding>,
        position: Int
    ) {
        val set = getItem(position)
        with(holder.viewBinding) {
            tvRaffleName.text = set.set
            tvTicketCount.text = set.count.toString()
            vItemBottomLine.isVisible = position < currentList.size - 1
        }
    }
}
