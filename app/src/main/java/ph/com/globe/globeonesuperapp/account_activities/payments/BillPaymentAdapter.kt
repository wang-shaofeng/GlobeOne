/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.payments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import com.airbnb.lottie.LottieAnimationView
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesDiffUtil
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.account_activities.rewards.*
import ph.com.globe.globeonesuperapp.databinding.*
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.payment.Payment
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty

class BillPaymentAdapter(
    private val onClick: (Payment) -> Unit,
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
                else -> BillPaymentHolder(
                    BillPaymentItemBinding.inflate(it, parent, false)
                )
            }
        }

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        when (holder) {
            is BillPaymentHolder -> {
                val data =
                    (getItem(position) as AccountActivityPagingState.BillPayment).billPayment
                with(holder.viewBinding) {
                    val date = data.date.toDateOrNull()
                        .toFormattedStringOrEmpty(GlobeDateFormat.SecurityQuestionApi)
                    tvDate.text = date
                    tvAmount.text = data.amount.toDouble().toPezosFormattedDisplayBalance()
                    root.setOnClickListener { onClick.invoke(data) }
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
                    holder.itemView.resources.getString(R.string.bill_payments_empty_description)
                holder.viewBinding.tvEmptyStateTitle.text =
                    holder.itemView.resources.getString(R.string.bill_payments_empty_title)
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
        else -> PAYMENTS
    }

    companion object {
        const val SKELETON_LOADING = 0
        const val REACH_END = 1
        const val EMPTY = 2
        const val ERROR = 3
        const val PAYMENTS = 4
    }
}

class BillPaymentHolder(viewBinding: BillPaymentItemBinding) :
    RecyclerViewHolderBinding<BillPaymentItemBinding>(viewBinding)
