/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.rewards_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ph.com.globe.globeonesuperapp.databinding.CategoryItemBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.rewards.RewardsCategory

class CategoryAdapter(private val callback: (RewardsCategory) -> Unit) :
    RecyclerView.Adapter<RecyclerViewHolderBinding<CategoryItemBinding>>() {

    var raffleInProgress = false
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<CategoryItemBinding> =
        RecyclerViewHolderBinding(
            CategoryItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<CategoryItemBinding>,
        position: Int
    ) {
        with(holder.viewBinding) {
            btnPromos.setOnClickListener { callback(RewardsCategory.PROMO) }
            btnDonations.setOnClickListener { callback(RewardsCategory.DONATION) }
            btnOthers.setOnClickListener { callback(RewardsCategory.OTHER) }
            btnRaffle.setOnClickListener { callback(RewardsCategory.RAFFLE) }
            ivRaffleBanner.setOnClickListener { callback(RewardsCategory.RAFFLE) }
            ivRaffleBanner.isVisible = raffleInProgress
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<CategoryItemBinding>) {
        super.onViewRecycled(holder)
        with(holder.viewBinding) {
            btnPromos.setOnClickListener(null)
            btnDonations.setOnClickListener(null)
            btnOthers.setOnClickListener(null)
            btnRaffle.setOnClickListener(null)
            ivRaffleBanner.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = 1
}
