/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.add_member

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectGroupMemberItemBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding

class AddEnrolledAccountRecyclerViewAdapter(
    private val enablingViewsCallback: (Boolean) -> Unit,
    private val selectAccountCallback: (AddEnrolledAccountItem) -> Unit
) : ListAdapter<AddEnrolledAccountItem, RecyclerViewHolderBinding<out ViewBinding>>(
    object : DiffUtil.ItemCallback<AddEnrolledAccountItem>() {

        override fun areItemsTheSame(
            oldItem: AddEnrolledAccountItem,
            newItem: AddEnrolledAccountItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: AddEnrolledAccountItem,
            newItem: AddEnrolledAccountItem
        ): Boolean {
            return oldItem == newItem
        }
    }
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<out ViewBinding> =
        RecyclerViewHolderBinding(
            SelectGroupMemberItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        val item = getItem(position)

        with(holder.viewBinding as SelectGroupMemberItemBinding) {
            tvAccountName.text = item.name
            tvPhoneNumber.text = item.msisdn
            tvAlreadyMember.isVisible = item.addedToGroup

            clSelectGroupMemberItemLayout.apply {
                isEnabled = !item.addedToGroup
                tvAccountName.isEnabled = !item.addedToGroup
                isSelected = item.selected
                ivSelectAccountStartDrawable.isEnabled = !item.addedToGroup
                setOnClickListener {
                    selectAccountCallback.invoke(item)
                    enablingViewsCallback.invoke(true)
                }
            }
        }
    }
}

data class AddEnrolledAccountItem(
    val name: String,
    val msisdn: String,
    var selected: Boolean,
    var addedToGroup: Boolean
)
