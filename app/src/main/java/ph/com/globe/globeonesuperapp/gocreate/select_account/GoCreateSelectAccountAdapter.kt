/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.select_account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GoCreateSelectAccountItemBinding
import ph.com.globe.model.util.brand.toUserFriendlyBrandName
import ph.com.globe.globeonesuperapp.utils.ui.KtItemHolder
import ph.com.globe.model.shop.formattedForPhilippines

class GoCreateSelectAccountAdapter(
    private val selectAccountCallback: (AccountItem) -> Unit
) :
    ListAdapter<AccountItem, KtItemHolder>(
        object : DiffUtil.ItemCallback<AccountItem>() {

            override fun areItemsTheSame(
                oldItem: AccountItem,
                newItem: AccountItem
            ): Boolean = oldItem == newItem

            override fun areContentsTheSame(
                oldItem: AccountItem,
                newItem: AccountItem
            ): Boolean = oldItem == newItem
        }
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtItemHolder =
        KtItemHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.go_create_select_account_item, parent, false)
        )

    override fun onBindViewHolder(holder: KtItemHolder, position: Int) {
        val account = getItem(position)

        GoCreateSelectAccountItemBinding.bind(holder.containerView).apply {
            tvAccountName.text = account.name
            tvPhoneNumber.text = account.msisdn.formattedForPhilippines()
            tvBrand.text = account.brand?.toUserFriendlyBrandName()?.uppercase()

            clSelectAccountItem.apply {
                isSelected = account.selected
                setOnClickListener {
                    selectAccountCallback.invoke(account)
                }
            }

            vNotEligibleAccount.isVisible = account.brand != GO_CREATE_ELIGIBLE_BRAND
        }
    }
}
