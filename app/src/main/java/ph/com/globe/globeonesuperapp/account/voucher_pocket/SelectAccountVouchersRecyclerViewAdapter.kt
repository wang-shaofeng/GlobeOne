/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import ph.com.globe.globeonesuperapp.account.voucher_pocket.VouchersViewModel.VoucherAccountUIModel
import ph.com.globe.globeonesuperapp.databinding.SelectOtherAccountItemBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.profile.domain_models.EnrolledAccount

class SelectAccountVouchersRecyclerViewAdapter(
    val callback: (EnrolledAccount) -> Unit
) : ListAdapter<VoucherAccountUIModel, RecyclerViewHolderBinding<SelectOtherAccountItemBinding>>(
    object : DiffUtil.ItemCallback<VoucherAccountUIModel>() {

        override fun areItemsTheSame(
            oldItem: VoucherAccountUIModel,
            newItem: VoucherAccountUIModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: VoucherAccountUIModel,
            newItem: VoucherAccountUIModel
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<SelectOtherAccountItemBinding> =
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
        val item = getItem(position)

        with(holder.viewBinding) {
            tvAccountName.text = item.account.accountAlias
            tvPhoneNumber.text = item.account.primaryMsisdn
            tvBrand.text = item.account.brandType.toString()

            clSelectOtherAccountItemLayout.isSelected = item.selected
            clSelectOtherAccountItemLayout.setOnClickListener {
                callback.invoke(item.account)
            }
        }
    }
}
