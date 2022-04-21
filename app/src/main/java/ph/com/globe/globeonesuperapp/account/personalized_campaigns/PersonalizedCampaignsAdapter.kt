/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.personalized_campaigns

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AvailableCampaignPromosModelWithBrand
import ph.com.globe.globeonesuperapp.databinding.PersonalizedCampaignsItemBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.account.AvailableCampaignPromosModel.PersonalizedCampaignsPromoType.*
import ph.com.globe.model.util.brand.AccountBrand

class PersonalizedCampaignsAdapter(
    private val callback: (AvailableCampaignPromosModelWithBrand) -> Unit
) :
    ListAdapter<AvailableCampaignPromosModelWithBrand, PersonalizedCampaignsHolder>(
        TestDiffUtil
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalizedCampaignsHolder =
        PersonalizedCampaignsHolder.instantiate(LayoutInflater.from(parent.context), parent)

    override fun onBindViewHolder(holder: PersonalizedCampaignsHolder, position: Int) {
        with(holder.viewBinding) {
            with(getItem(position)) {
                btnAction.setOnClickListener { callback(this) }

                GlobeGlide.with(ivCampaigns)
                    .load(availableCampaignPromosModel.bannerUrl)
                    .placeholder(R.drawable.ic_personalized_campaigns_blue)
                    .into(ivCampaigns)

                tvCampaigns.text = availableCampaignPromosModel.promoMechanics
                btnAction.text = availableCampaignPromosModel.buttonLabel
            }
        }
    }

    override fun onViewRecycled(holder: PersonalizedCampaignsHolder) {
        super.onViewRecycled(holder)

        with(holder.viewBinding) {
            GlobeGlide.with(ivCampaigns).clear(ivCampaigns)
        }
    }
}

class PersonalizedCampaignsHolder private constructor(binding: PersonalizedCampaignsItemBinding) :
    RecyclerViewHolderBinding<PersonalizedCampaignsItemBinding>(binding) {

    companion object {
        fun instantiate(layoutInflater: LayoutInflater, parent: ViewGroup) =
            PersonalizedCampaignsHolder(
                PersonalizedCampaignsItemBinding.inflate(layoutInflater, parent, false)
            )
    }
}

object TestDiffUtil : DiffUtil.ItemCallback<AvailableCampaignPromosModelWithBrand>() {
    override fun areItemsTheSame(
        oldItem: AvailableCampaignPromosModelWithBrand,
        newItem: AvailableCampaignPromosModelWithBrand
    ): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(
        oldItem: AvailableCampaignPromosModelWithBrand,
        newItem: AvailableCampaignPromosModelWithBrand
    ): Boolean = oldItem == newItem
}

