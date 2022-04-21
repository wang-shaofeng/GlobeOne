/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsContentSubscriptionItemBinding
import ph.com.globe.globeonesuperapp.glide.GlobeGlide
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaidMobile
import ph.com.globe.model.shop.ContentSubscriptionUIModel

class ContentSubscriptionsAdapter(
    private val enrolledAccount: EnrolledAccount,
    private val onItemClickedCallback: (ContentSubscriptionUIModel) -> Unit
) : ListAdapter<ContentSubscriptionUIModel, RecyclerViewHolderBinding<AccountDetailsContentSubscriptionItemBinding>>(
    object : DiffUtil.ItemCallback<ContentSubscriptionUIModel>() {

        override fun areItemsTheSame(
            oldItem: ContentSubscriptionUIModel,
            newItem: ContentSubscriptionUIModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: ContentSubscriptionUIModel,
            newItem: ContentSubscriptionUIModel
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            AccountDetailsContentSubscriptionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<AccountDetailsContentSubscriptionItemBinding>,
        position: Int
    ) {
        val subscription = getItem(position)
        with(holder.viewBinding) {

            GlobeGlide.with(ivPromoIcon).load(subscription.asset).into(ivPromoIcon)

            tvPromoName.text = subscription.promoName
            tvNotActivated.isVisible = !subscription.isActivated
            tvPurchased.isVisible = enrolledAccount.isPostpaidMobile()

            val expirationDate = subscription.expiryDate.convertDateToGroupDataFormat()
            tvExpirationDate.text = root.resources.getString(
                if (enrolledAccount.isPostpaidMobile()) R.string.refreshes_on
                else R.string.expires, expirationDate
            )

            tvExpirationDate.isVisible = expirationDate.isNotEmpty()

            // Make items not clickable for postpaid mobile
            if (!enrolledAccount.isPostpaidMobile()) {
                root.setOnClickListener {
                    onItemClickedCallback.invoke(subscription)
                }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<AccountDetailsContentSubscriptionItemBinding>) {
        super.onViewRecycled(holder)

        with(holder.viewBinding) {
            GlobeGlide.with(ivPromoIcon).clear(ivPromoIcon)
        }
    }
}
