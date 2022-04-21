/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.text

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsConsumptionItemTextBinding
import ph.com.globe.globeonesuperapp.utils.getSubscriptionNetworkType
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.account.AccountDetailsUsageUIModel

class TextUsageAdapter :
    ListAdapter<AccountDetailsUsageUIModel, RecyclerViewHolderBinding<AccountDetailsConsumptionItemTextBinding>>(
        object : DiffUtil.ItemCallback<AccountDetailsUsageUIModel>() {

            override fun areItemsTheSame(
                oldItem: AccountDetailsUsageUIModel,
                newItem: AccountDetailsUsageUIModel
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: AccountDetailsUsageUIModel,
                newItem: AccountDetailsUsageUIModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            AccountDetailsConsumptionItemTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<AccountDetailsConsumptionItemTextBinding>,
        position: Int
    ) {
        val usageItem = getItem(position)

        with(holder.viewBinding) {

            tvNetworkType.text = getSubscriptionNetworkType(root.context, usageItem.bucketId)
            tvLimitation.text = root.resources.getString(
                if (usageItem.unlimited) R.string.account_details_consumption_texts_unli
                else R.string.account_details_consumption_texts_left
            )
            tvUsage.apply {
                text = context.getString(
                    R.string.account_details_consumption_texts,
                    usageItem.remaining, usageItem.total
                )
                isVisible = !usageItem.unlimited
            }
            vRemaining.apply {
                val layoutParams = layoutParams as LinearLayout.LayoutParams
                layoutParams.weight = usageItem.remainingPercentage.toFloat()
                setLayoutParams(layoutParams)
            }
            ivUnlimited.isVisible = usageItem.unlimited

            val expirationDate = usageItem.expirationDate.convertDateToGroupDataFormat()
            tvExpirationDate.text = root.resources.getString(R.string.expires, expirationDate)
        }
    }
}
