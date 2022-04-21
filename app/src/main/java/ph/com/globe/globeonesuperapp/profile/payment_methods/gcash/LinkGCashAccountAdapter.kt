/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.profile.payment_methods.gcash

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.databinding.ProfileSelectAccountGCashItemBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.profile.domain_models.EnrolledAccount

class LinkGCashAccountAdapter(
    val selectCallback: (LinkGCashAccountItem) -> Unit = {},
    val isLinked: Boolean = false
) :
    ListAdapter<LinkGCashAccountItem, RecyclerViewHolderBinding<ProfileSelectAccountGCashItemBinding>>(
        object : DiffUtil.ItemCallback<LinkGCashAccountItem>() {

            override fun areItemsTheSame(
                oldItem: LinkGCashAccountItem,
                newItem: LinkGCashAccountItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: LinkGCashAccountItem,
                newItem: LinkGCashAccountItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            ProfileSelectAccountGCashItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<ProfileSelectAccountGCashItemBinding>,
        position: Int
    ) {
        with(holder.viewBinding) {
            with(getItem(position)) {
                tvPhoneNumber.text = accountMsisdn
                tvAccountName.apply {
                    this.text = accountName
                    this.isEnabled = !isLinked
                }
                clProfileSelectAccountGCashItem.setOnClickListener {
                    selectCallback(this)
                }
            }
        }
    }
}

data class LinkGCashAccountItem(
    val accountMsisdn: String,
    val accountName: String
)

fun EnrolledAccount.toLinkGCashAccountItem(): LinkGCashAccountItem =
    LinkGCashAccountItem(
        this.primaryMsisdn,
        this.accountAlias
    )
