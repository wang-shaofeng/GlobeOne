/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.calls

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsConsumptionItemCallsBinding
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsPostpaidPlanUsageBinding
import ph.com.globe.globeonesuperapp.utils.getSubscriptionNetworkType
import ph.com.globe.globeonesuperapp.utils.ui.KtItemHolder
import ph.com.globe.model.account.AccountDetailsUsageUIModel
import ph.com.globe.model.account.PlanUsageType.*
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidMobile

class CallsUsageAdapter(
    private val enrolledAccount: EnrolledAccount
) : ListAdapter<AccountDetailsUsageUIModel, KtItemHolder>(
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

    private var postpaidMobileRefreshDate = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = KtItemHolder(
        LayoutInflater.from(parent.context)
            .inflate(
                if (enrolledAccount.isPostpaidMobile())
                    R.layout.account_details_postpaid_plan_usage
                else
                    R.layout.account_details_consumption_item_calls, parent, false
            )
    )

    override fun onBindViewHolder(holder: KtItemHolder, position: Int) {
        val usageItem = getItem(position)

        with(holder.containerView) {
            with(usageItem) {
                if (enrolledAccount.isPostpaidMobile()) {
                    AccountDetailsPostpaidPlanUsageBinding.bind(holder.containerView).apply {
                        tvUsageTitle.text = when (usageType) {
                            CallsUsage -> context.getString(R.string.account_details_consumption_calls_left)
                            TextsUsage -> context.getString(R.string.account_details_consumption_texts_left)
                            else -> ""
                        }
                        tvUsage.text = when (usageType) {
                            CallsUsage -> context.getString(
                                R.string.account_details_consumption_calls,
                                remaining,
                                total
                            )
                            TextsUsage -> context.getString(
                                R.string.account_details_consumption_texts,
                                remaining,
                                total
                            )
                            else -> ""
                        }
                        vRemaining.apply {
                            val layoutParams = layoutParams as LinearLayout.LayoutParams
                            layoutParams.weight = remainingPercentage.toFloat()
                            setLayoutParams(layoutParams)
                        }
                        ivAddOn.isVisible = addOnUsage

                        tvExpirationDate.text = if (addOnUsage) {
                            context.getString(
                                R.string.expires,
                                expirationDate.convertDateToGroupDataFormat()
                            )
                        } else {
                            context.getString(R.string.refreshes_on, postpaidMobileRefreshDate)
                        }
                        tvExpirationDate.isVisible = tvExpirationDate.text.isNotEmpty()
                    }
                } else {
                    AccountDetailsConsumptionItemCallsBinding.bind(holder.containerView).apply {
                        tvNetworkType.text = getSubscriptionNetworkType(context, bucketId)
                        tvLimitation.text = context.getString(
                            if (unlimited) R.string.account_details_consumption_calls_unli
                            else R.string.account_details_consumption_calls_left
                        )
                        tvUsage.apply {
                            text = context.getString(
                                R.string.account_details_consumption_calls,
                                remaining,
                                total
                            )
                            isVisible = !unlimited
                        }
                        vRemaining.apply {
                            val layoutParams = layoutParams as LinearLayout.LayoutParams
                            layoutParams.weight = remainingPercentage.toFloat()
                            setLayoutParams(layoutParams)
                        }
                        ivUnlimited.isVisible = unlimited

                        val expirationDate = expirationDate.convertDateToGroupDataFormat()
                        tvExpirationDate.text = context.getString(R.string.expires, expirationDate)
                    }
                }
            }
        }
    }

    fun updateItemsRefreshDate(date: String) {
        postpaidMobileRefreshDate = date

        // Update already loaded items
        if (currentList.isNotEmpty()) {
            val planUsageBuckets = currentList.filter { !it.addOnUsage }
            notifyItemRangeChanged(0, planUsageBuckets.size)
        }
    }
}
