/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.DashboardConsumptionItemBinding
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.ui.BubbleType
import ph.com.globe.globeonesuperapp.utils.view_binding.RecyclerViewHolderBinding
import ph.com.globe.model.account.PostpaidPaymentStatus.*
import ph.com.globe.model.account.UsageUIModel
import ph.com.globe.model.account.isClickable
import ph.com.globe.model.profile.domain_models.isPostpaid
import ph.com.globe.model.profile.domain_models.isPostpaidBroadband
import ph.com.globe.model.util.AccountStatus.*
import ph.com.globe.model.util.UsageAmountUIModel
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.getCallsUsageAmount
import ph.com.globe.model.util.getDataUsageAmount
import ph.com.globe.model.util.getTextsUsageAmount

class DashboardConsumptionRecyclerViewAdapter(
    private val reloadCallback: ((Int, String, String) -> Unit),
    private val promosCallback: ((UsageUIModel) -> Unit),
    private val accountDetailsCallback: ((UsageUIModel) -> Unit),
    private val removeAccountCallback: ((UsageUIModel) -> Unit),
    private val showBubbleCallback: ((Int, BubbleType) -> Unit)
) : ListAdapter<UsageUIModel, RecyclerViewHolderBinding<DashboardConsumptionItemBinding>>(
    object : DiffUtil.ItemCallback<UsageUIModel>() {

        override fun areItemsTheSame(
            oldItem: UsageUIModel,
            newItem: UsageUIModel
        ) =
            oldItem.primaryMsisdn == newItem.primaryMsisdn

        override fun areContentsTheSame(
            oldItem: UsageUIModel,
            newItem: UsageUIModel
        ) = oldItem.isLoading == newItem.isLoading
                && oldItem.error == newItem.error
                && oldItem.noSubscriptions == newItem.noSubscriptions
                && oldItem.hasGift == newItem.hasGift
                && oldItem.accountName == newItem.accountName
                && oldItem.primaryMsisdn == newItem.primaryMsisdn
                && oldItem.balance == newItem.balance
                && oldItem.dataRemaining == newItem.dataRemaining
                && oldItem.dataTotal == newItem.dataTotal
                && oldItem.isDataUnlimited == newItem.isDataUnlimited
                && oldItem.callsRemaining == newItem.callsRemaining
                && oldItem.callsTotal == newItem.callsTotal
                && oldItem.areCallsUnlimited == newItem.areCallsUnlimited
                && oldItem.textRemaining == newItem.textRemaining
                && oldItem.textTotal == newItem.textTotal
                && oldItem.areTextsUnlimited == newItem.areTextsUnlimited
    }
), DashboardAdapterOnScrollChangeListener {

    private val mainScope = MainScope()
    private val mutex = Mutex()

    private val bubbles: MutableMap<Int, BubbleType> = mutableMapOf()
    private lateinit var showBubbleAttempt: ((Int, BubbleType) -> Unit)

    /**
     * Bubble should appear when user scrolls to needed card.
     * Since we have RecyclerView inside of NestedScrollView on dashboard, [onBindViewHolder]
     * for all the cards will be called immediately and we should postpone bubbles displaying
     * if some cards are not visible for user at that moment. Bubbles will store in [bubbles]
     * local variable as Map<Adapter position, [BubbleType]>.
     * */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        showBubbleAttempt = { position, type ->
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            if (viewHolder != null || position in 0..1) {
                showBubbleCallback.invoke(position, type)
                bubbles.remove(position)
            } else {
                bubbles[position] = type
            }
        }
    }

    override fun onScroll(visiblePosition: Int) {
        val visibleRange = (visiblePosition - 1).rangeTo(visiblePosition + 2)
        if (bubbles.keys.none { it in visibleRange }) return

        mainScope.launch {
            mutex.withLock {
                bubbles.keys.find { it in visibleRange }?.let { position ->
                    showBubbleAttempt.invoke(position, bubbles.getValue(position))
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewHolderBinding<DashboardConsumptionItemBinding> =
        RecyclerViewHolderBinding(
            DashboardConsumptionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(
        holder: RecyclerViewHolderBinding<DashboardConsumptionItemBinding>,
        position: Int
    ) {
        val consumptionItem = getItem(position)

        with(holder.viewBinding) {
            if (consumptionItem.isClickable()) {
                root.setOnClickListener {
                    accountDetailsCallback.invoke(consumptionItem)
                }
            }

            tvNickname.text = consumptionItem.accountName
            tvNumber.text = consumptionItem.primaryMsisdn

            with(consumptionItem) {

                vError.root.isVisible = error.isError()
                vLoading.root.isVisible = isLoading
                vNoSubscriptions.root.isVisible = noSubscriptions
                vDisconnected.root.isVisible = accountStatus is Disconnected
                vInactive.root.isVisible = accountStatus is Inactive
                vPlatinumAccount.root.isVisible = platinumAccount

                // Temporary view for postpaid broadband account
                vPostpaidBroadband.root.isVisible =
                    enrolledAccount.isPostpaidBroadband() && accountStatus is Active

                // Balance
                groupBalance.isVisible = accountStatus !is Inactive
                if (accountStatus !is Inactive) {
                    with(holder.itemView.resources) {

                        val formattedBalance by lazy {
                            balance?.toPezosFormattedDisplayBalance()
                                ?: getString(R.string.balance_placeholder)
                        }

                        if (enrolledAccount.isPostpaid()) {
                            when (postpaidPaymentStatus) {
                                is AllSet -> {
                                    tvBalanceTitle.text = getString(R.string.amount_to_pay)
                                    tvBalance.text = getString(R.string.all_set)
                                }
                                is BillDueSoon, BillOverdue -> {
                                    tvBalanceTitle.text = ""
                                    tvBalance.text = formattedBalance
                                }
                                else -> {
                                    tvBalanceTitle.text = getString(R.string.amount_to_pay)
                                    tvBalance.text = formattedBalance
                                }
                            }
                        } else {
                            tvBalanceTitle.text = getString(R.string.load_balance)
                            tvBalance.text = formattedBalance
                        }
                    }
                }

                // Remove account
                tvRemove.apply {
                    isVisible = accountStatus is Inactive
                    setOnClickListener {
                        removeAccountCallback.invoke(consumptionItem)
                    }
                }

                // Postpaid payment status image
                ivPostpaidBill.setImageResource(
                    when (postpaidPaymentStatus) {
                        BillDueSoon -> R.drawable.ic_postpaid_due_soon
                        BillOverdue -> R.drawable.ic_postpaid_overdue
                        else -> 0
                    }
                )

                // Gift
                lavGift.isVisible = hasGift

                // Bubble
                if (!bubbles.keys.contains(position)) {
                    when {
                        postpaidPaymentStatus is BillDueSoon -> {
                            showBubbleAttempt.invoke(position, BubbleType.BillDueSoon)
                        }
                        postpaidPaymentStatus is BillOverdue -> {
                            showBubbleAttempt.invoke(position, BubbleType.BillOverdue)
                        }
                        hasGift -> {
                            showBubbleAttempt.invoke(position, BubbleType.Surprise)
                        }
                    }
                }

                when {

                    error.isError() -> {
                        vError.btnReload.setOnClickListener {
                            reloadCallback.invoke(
                                position,
                                primaryMsisdn,
                                accountName
                            )
                        }
                    }

                    noSubscriptions -> {
                        vNoSubscriptions.btnBrowsePromos.setOnClickListener {
                            promosCallback.invoke(consumptionItem)
                        }
                    }

                    !noSubscriptions && !error.isError() && !isLoading && !enrolledAccount.isPostpaidBroadband() -> {
                        with(vConsumption) {
                            root.visibility = View.VISIBLE

                            ruvData.setUsage(
                                UsageAmountUIModel(
                                    getDataUsageAmount(dataRemaining, dataTotal),
                                    isDataUnlimited,
                                    hasDataSubscriptions
                                )
                            )

                            fun setupCallUsageView(showUsage: Boolean) {
                                ruvCall.apply {
                                    isVisible = showUsage
                                    if (showUsage)
                                        setUsage(
                                            UsageAmountUIModel(
                                                getCallsUsageAmount(callsRemaining, callsTotal),
                                                areCallsUnlimited,
                                                hasCallsSubscriptions,
                                                callSubscriptionsIncluded
                                            )
                                        )
                                }
                            }

                            fun setupTextUsageView(showUsage: Boolean) {
                                ruvText.apply {
                                    isVisible = showUsage
                                    if (showUsage)
                                        setUsage(
                                            UsageAmountUIModel(
                                                getTextsUsageAmount(textRemaining, textTotal),
                                                areTextsUnlimited,
                                                hasTextSubscriptions,
                                                textSubscriptionsIncluded
                                            )
                                        )
                                }
                            }

                            if (enrolledAccount.isPostpaid()) {
                                setupCallUsageView(hasCallsSubscriptions)
                                setupTextUsageView(hasTextSubscriptions)
                            } else {
                                setupCallUsageView(brand != AccountBrand.Hpw)
                                setupTextUsageView(brand != AccountBrand.Hpw)
                            }
                        }
                    }
                }
            }
        }
    }
}

interface DashboardAdapterOnScrollChangeListener {
    fun onScroll(visiblePosition: Int)
}
