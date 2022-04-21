/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_successful

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectedBoosterItemLayoutBinding
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.BoosterItem
import ph.com.globe.globeonesuperapp.utils.payment.setValidityText
import ph.com.globe.globeonesuperapp.utils.payment.stringToPesos
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class SelectedBoostersRecyclerViewAdapter :
    ListAdapter<BoosterItem, RecyclerViewHolderBinding<out ViewBinding>>(
        object : DiffUtil.ItemCallback<BoosterItem>() {
            override fun areItemsTheSame(oldItem: BoosterItem, newItem: BoosterItem): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: BoosterItem, newItem: BoosterItem): Boolean =
                oldItem == newItem

        }) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<out ViewBinding> =
        RecyclerViewHolderBinding(
            SelectedBoosterItemLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        with(getItem(position)) {
            with(holder.viewBinding as SelectedBoosterItemLayoutBinding) {
                tvBoosterDescription.text = root.resources.getString(
                    R.string.selected_booster_description,
                    boosterName,
                    root.resources.setValidityText(boosterValidity)
                )
                tvBoosterPrice.text = boosterPrice.stringToPesos()
            }
        }
    }
}
