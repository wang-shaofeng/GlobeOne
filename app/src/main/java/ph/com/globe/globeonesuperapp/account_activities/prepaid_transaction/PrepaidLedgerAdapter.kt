package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesDiffUtil
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.account_activities.rewards.LoadingHolder
import ph.com.globe.globeonesuperapp.account_activities.rewards.ReachEndHolder
import ph.com.globe.globeonesuperapp.account_activities.rewards.SkeletonLoadingHolder
import ph.com.globe.globeonesuperapp.databinding.*
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.*
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Call.CallType.CALL_MADE
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Call.CallType.CALL_RECEIVED
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Load.LoadType.*
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Promo.PromoType.*
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.Text.TextType.*
import ph.com.globe.model.util.megaBytesToDataUnitsFormatted
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty

class PrepaidLedgerAdapter(
    private val onClick: (PrepaidLedgerTransactionItem) -> Unit,
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
                EMPTY -> EmptyTransactionHolder(
                    TransactionEmptyItemBinding.inflate(it, parent, false)
                )
                ERROR -> ErrorTransactionHolder(
                    TransactionErrorItemBinding.inflate(it, parent, false)
                )
                else -> TransactionHolder(
                    TransactionItemBinding.inflate(it, parent, false)
                )
            }
        }

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<out ViewBinding>,
        position: Int
    ) {
        when (holder) {
            is TransactionHolder -> {
                val data =
                    (getItem(position) as AccountActivityPagingState.PrepaidLedger).prepaidLedger
                with(holder.viewBinding) {
                    tvDate.text = data.eventStartDate.toDateOrNull()
                        .toFormattedStringOrEmpty(GlobeDateFormat.PrepaidTransaction)
                    tvPlan.visibility = View.GONE

                    tvDateHeader.text = data.eventStartDate.toDateOrNull()
                        .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                    if (position == 0) {
                        tvDateHeader.visibility = View.VISIBLE
                    } else {
                        val previousData =
                            (getItem(position - 1) as AccountActivityPagingState.PrepaidLedger).prepaidLedger
                        tvDateHeader.isVisible = previousData.eventStartDate != data.eventStartDate
                    }

                    tvPoints.setAmountContent(data.chargeAmount, data.type)

                    when (data.type) {
                        is Load -> {
                            when ((data.type as Load).type) {
                                LOAD_BOUGHT -> {
                                    tvType.text = holder.getHeader(
                                        R.string.load_bought,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_load_bought)
                                }
                                LOAD_RECEIVED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.load_received,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_load_received)
                                }
                                LOAD_SHARED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.load_shared,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_load_shared)
                                }
                                LOAD_LOANED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.load_loaned,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_load_loaned)
                                }
                                LOAN_PAID -> {
                                    tvType.text = holder.getHeader(
                                        R.string.loan_paid,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_loan_paid)
                                }
                                LOAD_EXPIRED -> {
                                    tvType.text = holder.getHeader(R.string.load_expired)
                                    ivType.setImageResource(R.drawable.ic_load_expired)
                                }
                            }
                        }
                        is Call -> {
                            when ((data.type as Call).type) {
                                CALL_MADE -> {
                                    tvType.text = holder.getHeader(
                                        R.string.call_made,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_call_made)
                                }
                                CALL_RECEIVED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.call_received,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_call_received)
                                }
                            }
                        }
                        is Text -> {
                            when ((data.type as Text).type) {
                                TEXT_SENT -> {
                                    tvType.text = holder.getHeader(
                                        R.string.text_sent,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_text_sent)
                                }
                                TEXT_RECEIVED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.text_received,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_text_received)
                                }
                                TEXT_REFUNDED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.text_refunded,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_text_refunded)
                                }
                            }
                        }
                        is Data -> {
                            tvType.text = holder.getHeader(R.string.data_used_prepaid)
                            ivType.setImageResource(R.drawable.ic_data_used)
                            with(tvPlan) {
                                text = data.dataVolumeCount?.megaBytesToDataUnitsFormatted()
                                visibility = View.VISIBLE
                            }
                        }
                        is Promo -> {
                            when ((data.type as Promo).type) {
                                PROMO_RECEIVED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.promo_received,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_promo_received)
                                    tvPoints.isVisible = false
                                }
                                PROMO_BOUGHT -> {
                                    tvType.text = holder.getHeader(
                                        R.string.promo_bought,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_promo_bought)
                                }
                                PROMO_SHARED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.promo_shared,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_promo_shared)
                                }
                                PROMO_LOANED -> {
                                    tvType.text = holder.getHeader(
                                        R.string.promo_loaned,
                                        data.transactionCount ?: 0
                                    )
                                    ivType.setImageResource(R.drawable.ic_promo_loaned)
                                    tvPoints.isVisible = false
                                }
                                PROMO_RECEIVED_OTHERS -> {
                                    tvType.text = holder.getHeader(R.string.promo_received)
                                    ivType.setImageResource(R.drawable.ic_promo_received)
                                    tvPoints.isVisible = false
                                }
                            }
                        }
                        else -> {}
                    }

                    this.root.setOnClickListener { onClick(data) }
                }
            }
            is SkeletonLoadingHolder -> Unit
            is LoadingHolder -> {
                val data = (getItem(position) as AccountActivityPagingState.Loading)
                if (data.trigger) reachEnd()
            }
            is ReachEndHolder -> {
                with(holder.viewBinding) {
                    btnBackToTop.setOnClickListener { backToTopOnCLick() }
                    tvInfoMessage.text =
                        holder.itemView.context.getString(R.string.prepaid_ledger_reach_end)
                }

            }
            is ErrorTransactionHolder -> holder.viewBinding.btnReload.setOnClickListener { somethingWentWrongOnClick() }
            is EmptyTransactionHolder -> Unit
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
        else -> PREPAID_LEDGER
    }

    companion object {
        const val SKELETON_LOADING = 0
        const val LOADING = 1
        const val REACH_END = 2
        const val EMPTY = 3
        const val ERROR = 4
        const val PREPAID_LEDGER = 5
    }
}

class EmptyTransactionHolder(viewBinding: TransactionEmptyItemBinding) :
    RecyclerViewHolderBinding<TransactionEmptyItemBinding>(viewBinding)

class ErrorTransactionHolder(viewBinding: TransactionErrorItemBinding) :
    RecyclerViewHolderBinding<TransactionErrorItemBinding>(viewBinding)

class TransactionHolder(viewBinding: TransactionItemBinding) :
    RecyclerViewHolderBinding<TransactionItemBinding>(viewBinding)

private fun RecyclerView.ViewHolder.getHeader(resId: Int, transactionCount: Int = 0): String {
    return if (transactionCount <= 1) {
        this.itemView.resources.getString(resId, "")
    } else {
        this.itemView.resources.getString(resId, "(${transactionCount})")
    }
}

private fun TextView.setAmountContent(
    amount: Double,
    type: PrepaidLedgerTransactionItem.PrepaidType
) {

    isVisible = amount != 0.0
    text = when (type) {
        is Call -> {
            resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
        }
        is Data -> {
            resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
        }
        is Load -> {
            when (type.type) {
                LOAD_BOUGHT, LOAD_RECEIVED, LOAD_LOANED -> {
                    resources.getString(R.string.prepaid_ledger_amount_add, amount)
                }
                LOAD_SHARED, LOAN_PAID, LOAD_EXPIRED -> {
                    resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
                }
            }
        }
        is Promo -> {
            when (type.type) {
                PROMO_BOUGHT, PROMO_LOANED, PROMO_SHARED, PROMO_RECEIVED_OTHERS -> {
                    resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
                }
                PROMO_RECEIVED -> {
                    resources.getString(R.string.prepaid_ledger_amount_neutral, amount)
                }
            }
        }
        is Text -> {
            when (type.type) {
                TEXT_RECEIVED, TEXT_SENT -> {
                    resources.getString(R.string.prepaid_ledger_amount_deduct, amount)
                }
                TEXT_REFUNDED -> {
                    resources.getString(R.string.prepaid_ledger_amount_add, amount)
                }
            }
        }
        None -> {
            ""
        }
    }

    val color = if (text.contains("+")) {
        ContextCompat.getColorStateList(context, R.color.success)
    } else {
        ContextCompat.getColorStateList(context, R.color.neutral_A_2)
    }
    setTextColor(color)
}
