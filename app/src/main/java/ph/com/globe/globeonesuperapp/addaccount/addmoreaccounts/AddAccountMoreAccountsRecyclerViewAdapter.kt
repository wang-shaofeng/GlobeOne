/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel.EnrollAccountUI
import ph.com.globe.globeonesuperapp.databinding.AddAccountMoreAccountsItemBinding
import ph.com.globe.globeonesuperapp.databinding.AddAccountMoreAccountsItemEmptyBinding
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.util.brand.AccountSegment

class AddAccountMoreAccountsRecyclerViewAdapter(
    private val displayDeleteDialogCallback: (EnrollAccountUI) -> Unit,
    private val onItemClickedCallback: (EnrollAccountUI) -> Unit
) :
    ListAdapter<EnrollAccountUI, RecyclerViewHolderBinding<out ViewBinding>>(
        object : DiffUtil.ItemCallback<EnrollAccountUI>() {

            override fun areItemsTheSame(
                oldItem: EnrollAccountUI,
                newItem: EnrollAccountUI
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: EnrollAccountUI,
                newItem: EnrollAccountUI
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    companion object {
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    private var emptyViewDescriptionText: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_EMPTY ->
            EmptyViewHolder(
                AddAccountMoreAccountsItemEmptyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        VIEW_TYPE_ITEM ->
            AccountViewHolder(
                AddAccountMoreAccountsItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        else -> throw IllegalArgumentException("Invalid ViewHolder")
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        when (holder) {
            is AccountViewHolder -> {
                holder.bind(position)
            }
        }
    }

    inner class AccountViewHolder(viewBinding: AddAccountMoreAccountsItemBinding) :
        RecyclerViewHolderBinding<AddAccountMoreAccountsItemBinding>(viewBinding) {

        fun bind(position: Int) {

            with(viewBinding) {

                val account = getItem(position)

                tvDuplicatedNameError.isVisible = account.duplicatedNameError
                tvTooLongNameError.isVisible = account.tooLongNameError
                tvDuplicatedNumberError.isVisible = account.duplicatedNumberError

                with(account.enrollAccount) {
                    tvAccountName.text = accountAlias
                    msisdn.let {
                        tvPhoneNumber.text = msisdn.toDisplayUINumberFormat()
                    }
                    tvBrand.text = brandType.toString().uppercase()

                    if (segment == AccountSegment.Mobile) {
                        ivBrandIcon.setImageResource(
                            R.drawable.ic_mobile_icon
                        )
                    } else if (segment == AccountSegment.Broadband) {
                        ivBrandIcon.setImageResource(
                            R.drawable.ic_broadband_icon
                        )
                    }

                    ivDelete.setOnClickListener {
                        displayDeleteDialogCallback.invoke(account)
                    }

                    vAccountItemLineBottom.isVisible =
                        position != currentList.size - 1

                    root.setOnClickListener {
                        onItemClickedCallback.invoke(account)
                    }
                }
            }
        }
    }

    inner class EmptyViewHolder(viewBinding: AddAccountMoreAccountsItemEmptyBinding) :
        RecyclerViewHolderBinding<AddAccountMoreAccountsItemEmptyBinding>(viewBinding)

    override fun getItemViewType(position: Int): Int = if (currentList.count() == 0) {
        VIEW_TYPE_EMPTY
    } else {
        VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return when (val count = currentList.count()) {
            0 -> 1
            else -> count
        }
    }

    fun updateEmptyViewDescriptionText(text: String) {
        this.emptyViewDescriptionText = text
    }
}
