/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.group.group_overview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.databinding.GroupMemberItemLayoutBinding
import ph.com.globe.globeonesuperapp.group.GROUP_ROLE_OWNER
import ph.com.globe.globeonesuperapp.utils.formatPhoneNumber
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.shop.formattedForPhilippines
import java.io.Serializable

class GroupMembersRecyclerViewAdapter(val callback: (GroupMemberItem) -> (Unit)) :
    ListAdapter<GroupMemberItem, RecyclerViewHolderBinding<out ViewBinding>>(
        object : DiffUtil.ItemCallback<GroupMemberItem>() {

            override fun areItemsTheSame(
                oldItem: GroupMemberItem,
                newItem: GroupMemberItem
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: GroupMemberItem,
                newItem: GroupMemberItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    private var showBubble = false
    private var bubblePosition = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<out ViewBinding> = RecyclerViewHolderBinding(
        GroupMemberItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        val member = getItem(position)
        with(member) {
            with(holder.viewBinding as GroupMemberItemLayoutBinding) {
                tvGroupMemberName.text = memberAccountAlias
                tvGroupMemberFunction.text = memberRole
                tvGroupMemberNumber.text =
                    memberNumber.formattedForPhilippines().formatPhoneNumber()

                bubblePosition = if(memberRole != GROUP_ROLE_OWNER && currentList.size == 2) position else bubblePosition

                ivDataLimitHint.isVisible = position == bubblePosition && showBubble

                if (memberRole == GROUP_ROLE_OWNER) ivMemberOverview.visibility = View.GONE
                else {
                    ivMemberOverview.visibility = View.VISIBLE
                    clGroupMemberItemLayout.setOnClickListener {
                        callback.invoke(member)
                    }
                }

                vHorizontalLine.isVisible = position != currentList.size - 1
            }
        }
    }

    fun showBubble() {
        showBubble = true
        notifyItemChanged(bubblePosition)
    }

    fun hideBubble() {
        showBubble = false
        notifyItemChanged(bubblePosition)
    }
}

data class GroupMemberItem(
    val memberAccountAlias: String,
    val memberRole: String,
    val memberNumber: String,
    val walletId: String,
    val keyword: String,
    val skelligCategory: String,
    val totalAllocated: Int,
    val ownerMobileNumber: String,
    val ownerAccountAlias: String
) : Serializable
