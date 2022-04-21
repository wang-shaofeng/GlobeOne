/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.databinding.SelectEnrolledAccountItemBinding
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.model.util.brand.toUserFriendlyBrandName
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class EnrolledAccountsAdapter(
    private val indexCallback: (String) -> Unit
) : ListAdapter<EnrolledAccountUiModel, RecyclerViewHolderBinding<SelectEnrolledAccountItemBinding>>(
    object : DiffUtil.ItemCallback<EnrolledAccountUiModel>() {

        override fun areItemsTheSame(
            oldItem: EnrolledAccountUiModel,
            newItem: EnrolledAccountUiModel
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: EnrolledAccountUiModel,
            newItem: EnrolledAccountUiModel
        ) = oldItem == newItem
    }
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = RecyclerViewHolderBinding(
        SelectEnrolledAccountItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<SelectEnrolledAccountItemBinding>,
        position: Int
    ) {
        val account = getItem(position)
        with(holder.viewBinding) {
            tvAccountName.text = account.enrolledAccount.accountAlias
            tvNumber.text = account.enrolledAccount.primaryMsisdn.toDisplayUINumberFormat()

            tvBrand.apply {
                text = account.brand?.toUserFriendlyBrandName(account.enrolledAccount.segment)?.uppercase()
                isVisible = account.brand != null
            }

            clSelectOtherAccountItemLayout.apply {
                isSelected = account.selected
                setOnClickListener {
                    indexCallback.invoke(account.enrolledAccount.primaryMsisdn)
                }
            }

            groupNotEligibleAccount.isVisible = !account.availableToSelect
        }
    }
}
