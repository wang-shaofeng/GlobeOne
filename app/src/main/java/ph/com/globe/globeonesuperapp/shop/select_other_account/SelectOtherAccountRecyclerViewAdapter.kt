/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.select_other_account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectOtherAccountItemBinding
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.util.brand.AccountBrandType

class SelectOtherAccountRecyclerViewAdapter(
    private val enablingViewsCallback: (Boolean) -> Unit,
    private val selectAccountCallback: (AccountItem) -> Unit,
    private val isChargeToLoadUsage: Boolean = false,
) :
    ListAdapter<AccountItem, RecyclerViewHolderBinding<SelectOtherAccountItemBinding>>(
        object : DiffUtil.ItemCallback<AccountItem>() {

            override fun areItemsTheSame(oldItem: AccountItem, newItem: AccountItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: AccountItem,
                newItem: AccountItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RecyclerViewHolderBinding(
            SelectOtherAccountItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<SelectOtherAccountItemBinding>,
        position: Int
    ) {
        val account = getItem(position)
        with(holder.viewBinding) {
            tvAccountName.text = account.name
            tvPhoneNumber.text = account.phoneNumber
            tvBrand.text = account.brandType.toString().uppercase()
            clSelectOtherAccountItemLayout.apply {
                isSelected = account.selected

                if (isChargeToLoadUsage) {
                    val text = account.balance?.toPezosFormattedDisplayBalance()
                    tvBalance.text = text
                    tvBalance.isVisible = text.isNullOrEmpty().not()
                } else {
                    tvBalance.isVisible = false
                }

                if (account.isClickable.not()) {
                    ivSelectAccountStartDrawable.isEnabled = false
                    this.isSelected = false
                    this.isEnabled = false
                    tvBalance.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.neutral_A_4,
                            null
                        )
                    )
                    return@apply
                } else {
                    ivSelectAccountStartDrawable.isEnabled = true
                    this.isEnabled = true
                    tvBalance.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            R.color.accent_dark,
                            null
                        )
                    )
                }

                setOnClickListener {
                    unSelectItems()
                    account.selected = !account.selected
                    isSelected = account.selected
                    enablingViewsCallback.invoke(account.selected)
                    selectAccountCallback.invoke(account)
                }
            }
        }
    }

    fun unSelectItems() {
        for (account in currentList) account.selected = false
        notifyDataSetChanged()
    }
}

data class AccountItem(
    val name: String,
    val phoneNumber: String,
    val brandType: AccountBrandType,
    var selected: Boolean,
    var balance: Float? = null,
    /**
     * for now, the [isClickable] is only
     * suit for Load tab
     */
    var isClickable: Boolean = true
)
