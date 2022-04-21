/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.rewards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesDiffUtil
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.databinding.*
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.account_activities.AccountRewardsTransaction
import ph.com.globe.model.account_activities.RewardsTransactionType

class AccountRewardsAdapter(
    private val onClick: (AccountRewardsTransaction) -> Unit,
    private val reachEnd: () -> Unit,
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
                LOADING -> LoadingHolder(
                    AccountActivityLoadingItemBinding.inflate(it, parent, false)
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
                else -> AccountActivityHolder(
                    AccountRewardsItemBinding.inflate(it, parent, false)
                )
            }
        }

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        when (holder) {
            is AccountActivityHolder -> {
                val data = (getItem(position) as AccountActivityPagingState.Rewards).transaction
                with(holder.viewBinding) {

                    tvPoints.text = (if (data.totalPoints > 0f) "+" else "") +
                            tvPoints.resources.getQuantityString(
                                R.plurals.reward_points_short_decimal,
                                if (data.totalPoints == 1.0) 1 else 2,
                                data.totalPoints
                            )
                    tvDate.text = data.date.convertDateToGroupDataFormat()

                    when (data.rewardsTransactionType) {
                        RewardsTransactionType.EarnedPoints -> {
                            ivType.setImageResource(R.drawable.ic_earned_points)
                            tvType.text =
                                holder.itemView.resources.getString(R.string.earned_points_title)
                        }
                        RewardsTransactionType.RedeemedReward -> {
                            ivType.setImageResource(R.drawable.ic_redeemed_reward)
                            tvType.text =
                                holder.itemView.resources.getString(R.string.redeemed_reward_title)
                        }
                        is RewardsTransactionType.PaidWithPoints -> {
                            ivType.setImageResource(R.drawable.ic_paid_with_points)
                            tvType.text =
                                holder.itemView.resources.getString(R.string.paid_with_points_title)
                        }
                        RewardsTransactionType.ExpiredPoints -> {
                            ivType.setImageResource(R.drawable.ic_expired_points)
                            tvType.text =
                                holder.itemView.resources.getString(R.string.expired_points_title)
                        }
                        RewardsTransactionType.RefundedPoints -> {
                            ivType.setImageResource(R.drawable.ic_refunded_points)
                            tvType.text =
                                holder.itemView.resources.getString(R.string.refunded_points_title)
                        }
                        RewardsTransactionType.GiftedReward -> {
                            ivType.setImageResource(R.drawable.ic_gifted_reward)
                            tvType.text =
                                holder.itemView.resources.getString(R.string.gifted_reward_title)
                        }
                        RewardsTransactionType.DeductedPoints -> {
                            ivType.setImageResource(R.drawable.ic_deducted_points)
                            tvType.text =
                                holder.itemView.resources.getString(R.string.deducted_points_title)
                        }
                    }

                    this.root.setOnClickListener { onClick(data) }
                }
            }
            is SkeletonLoadingHolder -> Unit
            is LoadingHolder -> {
                val data = (getItem(position) as AccountActivityPagingState.Loading)
                if (data.trigger) reachEnd()
            }
            is ReachEndHolder -> holder.viewBinding.btnBackToTop.setOnClickListener { backToTopOnCLick() }
            is ErrorHolder -> holder.viewBinding.btnReload.setOnClickListener { somethingWentWrongOnClick() }
            is EmptyHolder -> Unit
        }
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<out ViewBinding>) {
        super.onViewRecycled(holder)
        holder.viewBinding.root.setOnClickListener(null)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        AccountActivityPagingState.SkeletonLoading -> SKELETON_LOADING
        is AccountActivityPagingState.Loading -> LOADING
        AccountActivityPagingState.ReachEnd -> REACH_END
        AccountActivityPagingState.Empty -> EMPTY
        AccountActivityPagingState.Error -> ERROR
        else -> ACCOUNT_ACTIVITY
    }

    companion object {
        const val SKELETON_LOADING = 0
        const val LOADING = 1
        const val REACH_END = 2
        const val EMPTY = 3
        const val ERROR = 4
        const val ACCOUNT_ACTIVITY = 5
    }
}

class AccountActivityHolder(viewBinding: AccountRewardsItemBinding) :
    RecyclerViewHolderBinding<AccountRewardsItemBinding>(viewBinding)

class SkeletonLoadingHolder(viewBinding: AccountActivitySkeletonLoadingItemBinding) :
    RecyclerViewHolderBinding<AccountActivitySkeletonLoadingItemBinding>(viewBinding)

class LoadingHolder(viewBinding: AccountActivityLoadingItemBinding) :
    RecyclerViewHolderBinding<AccountActivityLoadingItemBinding>(viewBinding)

class ReachEndHolder(viewBinding: AccountActivityReachEndItemBinding) :
    RecyclerViewHolderBinding<AccountActivityReachEndItemBinding>(viewBinding)

class EmptyHolder(viewBinding: AccountActivityEmptyItemBinding) :
    RecyclerViewHolderBinding<AccountActivityEmptyItemBinding>(viewBinding)

class ErrorHolder(viewBinding: AccountActivityErrorItemBinding) :
    RecyclerViewHolderBinding<AccountActivityErrorItemBinding>(viewBinding)
