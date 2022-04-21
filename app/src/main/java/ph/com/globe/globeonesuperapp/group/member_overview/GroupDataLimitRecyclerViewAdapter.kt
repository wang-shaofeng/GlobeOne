/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.member_overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.DataLimitItemLayoutBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class GroupDataLimitRecyclerViewAdapter(val callback: (String) -> (Unit)) :
    ListAdapter<DataLimitItem, RecyclerViewHolderBinding<out ViewBinding>>(
        object : DiffUtil.ItemCallback<DataLimitItem>() {

            override fun areItemsTheSame(
                oldItem: DataLimitItem,
                newItem: DataLimitItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: DataLimitItem,
                newItem: DataLimitItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecyclerViewHolderBinding(
        DataLimitItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        val dataLimitItem = currentList[position]

        with(holder.viewBinding as DataLimitItemLayoutBinding) {
            tvDataAmount.text =
                if (dataLimitItem.amount.isDigitsOnly())
                    root.context.resources.getString(R.string.limit_gb, dataLimitItem.amount)
                else dataLimitItem.amount

            clDataLimitItemLayout.isEnabled = dataLimitItem.enabled
            tvDataAmount.isEnabled = dataLimitItem.enabled
            clDataLimitItemLayout.setOnClickListener {
                callback.invoke(dataLimitItem.amount)
            }
        }
    }
}

data class DataLimitItem(
    val amount: String,
    var enabled: Boolean
)
