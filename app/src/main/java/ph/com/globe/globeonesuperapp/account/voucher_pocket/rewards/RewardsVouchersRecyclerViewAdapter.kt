/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket.rewards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.*
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.voucher.*
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty

class RewardsVouchersRecyclerViewAdapter(
    private val revealVoucher: (Coupon) -> Unit,
    private val copyVoucherNumber: (String) -> Unit,
    private val openLink: (String) -> Unit,
    private val reachEnd: () -> Unit,
    private val somethingWentWrongOnClick: () -> Unit,
    private val redeemRewards: () -> Unit,
    private val backToTopOnCLick: () -> Unit
) : ListAdapter<VoucherPocketPagingState, RecyclerViewHolderBinding<out ViewBinding>>(
    VoucherPocketDiffUtil
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<out ViewBinding> =
        LayoutInflater.from(parent.context).let {
            when (viewType) {
                SKELETON_LOADING -> SkeletonLoadingHolder(
                    VoucherPocketSkeletonLoadingItemBinding.inflate(it, parent, false)
                )
                LOADING -> LoadingHolder(
                    VoucherPocketLoadingItemBinding.inflate(it, parent, false)
                )
                REACH_END -> ReachEndHolder(
                    VoucherPocketReachEndItemBinding.inflate(it, parent, false)
                )
                EMPTY -> EmptyHolder(
                    VoucherPocketEmptyItemBinding.inflate(it, parent, false)
                )
                ERROR -> ErrorHolder(
                    VoucherPocketErrorItemBinding.inflate(it, parent, false)
                )
                else -> VoucherPocketHolder(
                    VoucherItemLayoutBinding.inflate(it, parent, false)
                )
            }
        }

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        when (holder) {
            is VoucherPocketHolder -> {
                val data = (getItem(position) as VoucherPocketPagingState.Data).coupon
                with(holder.viewBinding) {
                    tvVoucherTitle.text =
                        if (data.coupon.couponDescription.startsWith(DESCRIPTION_VOUCHER))
                            data.coupon.couponDescription.removePrefix(DESCRIPTION_VOUCHER)
                        else data.coupon.couponDescription.removePrefix(DESCRIPTION_SOFT_BENEFIT)
                    data.coupon.expiryDate?.let {
                        tvExpires.text = this.root.context.getString(
                            R.string.expires_on_with_formatted_date,
                            it.toDateOrNull()?.toFormattedStringOrEmpty(GlobeDateFormat.Voucher)
                        )
                    } ?: kotlin.run {
                        tvExpires.text = holder.itemView.resources.getString(R.string.no_expiry)
                    }

                    clHiddenVoucher.isVisible = !data.used
                    ivHiddenVoucherIcon.isVisible = !data.used && !data.isLoading
                    pbLoading.isVisible = !data.used && data.isLoading
                    clRevealedVoucher.isVisible = data.used && !data.couponWithLink
                    tvVoucherCode.text =
                        if (data.softBenefit) data.coupon.couponType else data.coupon.couponNumber
                    clVoucherWithLink.isVisible = data.used && data.couponWithLink
                    tvLink.text = "$VOUCHER_LINK_PREFIX${data.coupon.couponNumber}"

                    ivTapToReveal.isVisible = !data.used && data.showTapToRevealBubble
                    ivTapToCopy.isVisible = data.used && data.showTapToCopyBubble

                    if (!data.used) {
                        cvVoucherItem.setOnClickListener { revealVoucher(data.coupon) }
                    } else if (!data.couponWithLink) {
                        cvVoucherItem.setOnClickListener { copyVoucherNumber(tvVoucherCode.text.toString()) }
                    } else {
                        btnActivate.setOnClickListener { openLink("$HTTPS_PREFIX${tvLink.text}") }
                    }
                }
            }
            is SkeletonLoadingHolder -> Unit
            is LoadingHolder -> {
                val data = (getItem(position) as VoucherPocketPagingState.Loading)
                if (data.trigger) reachEnd()
            }
            is ReachEndHolder -> holder.viewBinding.btnBackToTop.setOnClickListener { backToTopOnCLick() }
            is ErrorHolder -> holder.viewBinding.btnReload.setOnClickListener { somethingWentWrongOnClick() }
            is EmptyHolder -> holder.viewBinding.btnRedeemRewards.setOnClickListener { redeemRewards() }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is VoucherPocketPagingState.SkeletonLoading -> SKELETON_LOADING
        is VoucherPocketPagingState.Loading -> LOADING
        is VoucherPocketPagingState.ReachEnd -> REACH_END
        is VoucherPocketPagingState.Empty -> EMPTY
        is VoucherPocketPagingState.Error -> ERROR
        is VoucherPocketPagingState.Data -> VOUCHER_POCKET
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<out ViewBinding>) {
        super.onViewRecycled(holder)

        when (holder) {
            is VoucherPocketHolder -> {
                holder.viewBinding.cvVoucherItem.setOnClickListener(null)
                holder.viewBinding.btnActivate.setOnClickListener(null)
            }

            is ReachEndHolder -> holder.viewBinding.btnBackToTop.setOnClickListener(null)
            is ErrorHolder -> holder.viewBinding.btnReload.setOnClickListener(null)
            is EmptyHolder -> holder.viewBinding.btnRedeemRewards.setOnClickListener(null)
        }
    }

    companion object {
        const val SKELETON_LOADING = 0
        const val LOADING = 1
        const val REACH_END = 2
        const val EMPTY = 3
        const val ERROR = 4
        const val VOUCHER_POCKET = 5
    }
}

private class VoucherPocketHolder(viewBinding: VoucherItemLayoutBinding) :
    RecyclerViewHolderBinding<VoucherItemLayoutBinding>(viewBinding)

private class SkeletonLoadingHolder(viewBinding: VoucherPocketSkeletonLoadingItemBinding) :
    RecyclerViewHolderBinding<VoucherPocketSkeletonLoadingItemBinding>(viewBinding)

private class LoadingHolder(viewBinding: VoucherPocketLoadingItemBinding) :
    RecyclerViewHolderBinding<VoucherPocketLoadingItemBinding>(viewBinding)

private class ReachEndHolder(viewBinding: VoucherPocketReachEndItemBinding) :
    RecyclerViewHolderBinding<VoucherPocketReachEndItemBinding>(viewBinding)

private class EmptyHolder(viewBinding: VoucherPocketEmptyItemBinding) :
    RecyclerViewHolderBinding<VoucherPocketEmptyItemBinding>(viewBinding)

private class ErrorHolder(viewBinding: VoucherPocketErrorItemBinding) :
    RecyclerViewHolderBinding<VoucherPocketErrorItemBinding>(viewBinding)

object VoucherPocketDiffUtil : DiffUtil.ItemCallback<VoucherPocketPagingState>() {
    override fun areItemsTheSame(
        oldItem: VoucherPocketPagingState,
        newItem: VoucherPocketPagingState
    ): Boolean =
        oldItem is VoucherPocketPagingState.Loading && newItem is VoucherPocketPagingState.Loading && oldItem.id == newItem.id ||
                oldItem is VoucherPocketPagingState.SkeletonLoading && newItem is VoucherPocketPagingState.SkeletonLoading && oldItem.id == newItem.id ||
                oldItem is VoucherPocketPagingState.ReachEnd && newItem is VoucherPocketPagingState.ReachEnd && oldItem.id == newItem.id ||
                oldItem is VoucherPocketPagingState.Data && newItem is VoucherPocketPagingState.Data && oldItem.coupon.coupon.couponId == newItem.coupon.coupon.couponId ||
                oldItem is VoucherPocketPagingState.Empty && newItem is VoucherPocketPagingState.Empty && oldItem.id == newItem.id ||
                oldItem is VoucherPocketPagingState.Error && newItem is VoucherPocketPagingState.Error && oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: VoucherPocketPagingState,
        newItem: VoucherPocketPagingState
    ): Boolean =
        oldItem == newItem // if areItemsTheSame returns true, it means that oldItem and newItem are also the same type.
}
