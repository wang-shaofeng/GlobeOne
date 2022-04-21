/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.voucher_pocket.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.*
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.voucher.PromoVoucher
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty

class PromoVouchersRecyclerViewAdapter(
    private val revealVoucher: (PromoVoucher) -> Unit,
    private val copyVoucherNumber: (String) -> Unit,
    private val reachEnd: () -> Unit,
    private val somethingWentWrongOnClick: () -> Unit,
    private val backToTopOnCLick: () -> Unit
) : ListAdapter<PromoVoucherPagingState, RecyclerViewHolderBinding<out ViewBinding>>(
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
                COMING_SOON -> ComingSoonHolder(
                    VoucherPocketComingSoonItemBinding.inflate(it, parent, false)
                )
                else -> VoucherPocketHolder(
                    PromoVoucherItemLayoutBinding.inflate(it, parent, false)
                )
            }
        }

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        when (holder) {
            is VoucherPocketHolder -> {
                val data = (getItem(position) as PromoVoucherPagingState.Data).voucher
                with(holder.viewBinding) {
                    tvVoucherTitle.text = data.voucher.description
                    tvExpires.text = this.root.context.getString(
                        R.string.expires_on_with_formatted_date,
                        data.voucher.validityEndDate.toDateOrNull()
                            ?.toFormattedStringOrEmpty(GlobeDateFormat.Voucher)
                    )

                    clHiddenVoucher.isVisible = !data.used
                    ivHiddenVoucherIcon.isVisible = !data.used && !data.isLoading
                    pbLoading.isVisible = !data.used && data.isLoading
                    clRevealedVoucher.isVisible = data.used
                    tvVoucherCode.text = data.voucher.code

                    ivTapToReveal.isVisible = !data.used && data.showTapToRevealBubble
                    ivTapToCopy.isVisible = data.used && data.showTapToCopyBubble

                    if (!data.used) {
                        cvVoucherItem.setOnClickListener { revealVoucher(data.voucher) }
                    } else {
                        cvVoucherItem.setOnClickListener { copyVoucherNumber(tvVoucherCode.text.toString()) }
                    }
                }
            }
            is SkeletonLoadingHolder -> Unit
            is LoadingHolder -> {
                val data = (getItem(position) as PromoVoucherPagingState.Loading)
                if (data.trigger) reachEnd()
            }
            is ReachEndHolder -> holder.viewBinding.btnBackToTop.setOnClickListener { backToTopOnCLick() }
            is ErrorHolder -> holder.viewBinding.btnReload.setOnClickListener { somethingWentWrongOnClick() }
            is EmptyHolder -> {
                with(holder.viewBinding) {
                    btnRedeemRewards.visibility = View.GONE
                    tvEmptyStateDescription.text =
                        root.context.getString(R.string.promo_voucher_empty_description)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is PromoVoucherPagingState.SkeletonLoading -> SKELETON_LOADING
        is PromoVoucherPagingState.Loading -> LOADING
        is PromoVoucherPagingState.ReachEnd -> REACH_END
        is PromoVoucherPagingState.Empty -> EMPTY
        is PromoVoucherPagingState.Error -> ERROR
        is PromoVoucherPagingState.ComingSoon -> COMING_SOON
        is PromoVoucherPagingState.Data -> VOUCHER_POCKET
    }

    override fun onViewRecycled(holder: RecyclerViewHolderBinding<out ViewBinding>) {
        super.onViewRecycled(holder)

        when (holder) {
            is VoucherPocketHolder -> {
                holder.viewBinding.cvVoucherItem.setOnClickListener(null)
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
        const val COMING_SOON = 5
        const val VOUCHER_POCKET = 6
    }
}

private class VoucherPocketHolder(viewBinding: PromoVoucherItemLayoutBinding) :
    RecyclerViewHolderBinding<PromoVoucherItemLayoutBinding>(viewBinding)

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

private class ComingSoonHolder(viewBinding: VoucherPocketComingSoonItemBinding) :
    RecyclerViewHolderBinding<VoucherPocketComingSoonItemBinding>(viewBinding)

object VoucherPocketDiffUtil : DiffUtil.ItemCallback<PromoVoucherPagingState>() {
    override fun areItemsTheSame(
        oldItem: PromoVoucherPagingState,
        newItem: PromoVoucherPagingState
    ): Boolean =
        oldItem is PromoVoucherPagingState.Loading && newItem is PromoVoucherPagingState.Loading && oldItem.id == newItem.id ||
                oldItem is PromoVoucherPagingState.SkeletonLoading && newItem is PromoVoucherPagingState.SkeletonLoading && oldItem.id == newItem.id ||
                oldItem is PromoVoucherPagingState.ReachEnd && newItem is PromoVoucherPagingState.ReachEnd && oldItem.id == newItem.id ||
                oldItem is PromoVoucherPagingState.Data && newItem is PromoVoucherPagingState.Data && oldItem.voucher.voucher.serialNumber == newItem.voucher.voucher.serialNumber ||
                oldItem is PromoVoucherPagingState.Empty && newItem is PromoVoucherPagingState.Empty && oldItem.id == newItem.id ||
                oldItem is PromoVoucherPagingState.Error && newItem is PromoVoucherPagingState.Error && oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: PromoVoucherPagingState,
        newItem: PromoVoucherPagingState
    ): Boolean =
        oldItem == newItem
}
