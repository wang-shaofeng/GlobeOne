/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.bill_statements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieAnimationView
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesDiffUtil
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.account_activities.rewards.EmptyHolder
import ph.com.globe.globeonesuperapp.account_activities.rewards.ErrorHolder
import ph.com.globe.globeonesuperapp.account_activities.rewards.ReachEndHolder
import ph.com.globe.globeonesuperapp.account_activities.rewards.SkeletonLoadingHolder
import ph.com.globe.globeonesuperapp.databinding.*
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.billings.domain_models.BillingStatement
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrNull

class AccountBillStatementsAdapter(
    private val onClick: (BillingStatement) -> Unit,
    private val somethingWentWrongOnClick: () -> Unit,
    private val backToTopOnCLick: () -> Unit
) : ListAdapter<AccountActivityPagingState, RecyclerViewHolderBinding<out ViewBinding>>(
    AccountActivitiesDiffUtil
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<out ViewBinding> =
        LayoutInflater.from(parent.context).let {
            when (viewType) {
                SKELETON_LOADING -> SkeletonLoadingHolder(
                    AccountActivitySkeletonLoadingItemBinding.inflate(it, parent, false)
                )
                REACH_END -> ReachEndHolder(
                    AccountActivityReachEndItemBinding.inflate(it, parent, false)
                )
                EMPTY -> EmptyHolder(
                    AccountActivityEmptyItemBinding.inflate(it, parent, false)
                )
                ERROR -> ErrorHolder(
                    AccountActivityErrorItemBinding.inflate(it, parent, false)
                )
                else -> AccountBillStatementsHolder(
                    AccountBillStatementsItemBinding.inflate(it, parent, false)
                )
            }
        }

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        when (holder) {
            is AccountBillStatementsHolder -> {
                val data =
                    (getItem(position) as AccountActivityPagingState.BillStatement).billingStatement
                with(holder.viewBinding) {
                    val startDate = data.billStartDate?.toDateOrNull()
                        .toFormattedStringOrNull(GlobeDateFormat.PrepaidTransaction)
                    val endDate = data.billEndDate?.toDateOrNull()
                        .toFormattedStringOrNull(GlobeDateFormat.PrepaidTransaction)

                    tvDate.text = tvDate.context.getString(R.string.date_range, startDate, endDate)

                    tvP.text = data.totalAmount?.toPezosFormattedDisplayBalance()

                    this.root.setOnClickListener { onClick(data) }
                }
            }
            is ReachEndHolder -> {
                with(holder.viewBinding) {
                    btnBackToTop.setOnClickListener { backToTopOnCLick() }
                    tvInfoMessage.text =
                        root.resources.getString(R.string.bill_statements_reach_end_info)
                }
            }
            is SkeletonLoadingHolder -> {
                for (v in holder.viewBinding.root.children) {
                    if (v is LottieAnimationView)
                        v.setAnimation(R.raw.account_activity_skeleton_bills)
                }
            }
            is ErrorHolder -> holder.viewBinding.btnReload.setOnClickListener { somethingWentWrongOnClick() }
            is EmptyHolder -> {
                holder.viewBinding.tvEmptyStateDescription.text =
                    holder.itemView.resources.getString(R.string.bill_statements_empty_description)
                holder.viewBinding.tvEmptyStateTitle.text =
                    holder.itemView.resources.getString(R.string.bill_statements_empty_title)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<out ViewBinding>) {
        super.onViewRecycled(holder)
        holder.viewBinding.root.setOnClickListener(null)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        AccountActivityPagingState.SkeletonLoading -> SKELETON_LOADING
        AccountActivityPagingState.ReachEnd -> REACH_END
        AccountActivityPagingState.Empty -> EMPTY
        AccountActivityPagingState.Error -> ERROR
        else -> BILLS
    }

    companion object {
        const val SKELETON_LOADING = 0
        const val REACH_END = 1
        const val EMPTY = 2
        const val ERROR = 3
        const val BILLS = 4
    }
}

class AccountBillStatementsHolder(viewBinding: AccountBillStatementsItemBinding) :
    RecyclerViewHolderBinding<AccountBillStatementsItemBinding>(viewBinding)
