/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.utils.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.BalanceViewBinding
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.ui.BalanceType.*
import ph.com.globe.model.account.BalanceStatus
import ph.com.globe.model.account.BalanceStatus.*
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateWithTimeZoneOrNull
import ph.com.globe.util.toFormattedStringOrEmpty

class BalanceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding = BalanceViewBinding.inflate(LayoutInflater.from(context), this, true)

    private val balancePlaceholder by lazy { context.getString(R.string.balance_placeholder) }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BalanceView,
            defStyleAttr, defStyleRes
        ).apply {
            try {
                binding.tvBalanceTitle.text = getString(R.styleable.BalanceView_balanceTitle) ?: ""
            } finally {
                recycle()
            }
        }
    }

    internal fun updateBalanceStatus(status: BalanceStatus, type: BalanceType) {
        with(binding) {
            lavBalanceLoading.apply {
                isVisible = status is Loading
                setAnimation(
                    when (type) {
                        LoadBalance, RewardPoints -> R.raw.balance_loading_multiline
                        else -> R.raw.balance_loading_singleline
                    }
                )

                if (status is Loading)
                    playAnimation()
            }

            tvBalanceAmount.apply {
                text = when (status) {
                    is Success -> {
                        getFormattedBalance(status.balance, type)
                    }
                    is Empty -> balancePlaceholder
                    else -> ""
                }
            }

            tvExpirationDate.apply {
                text = when (status) {
                    is Success -> {
                        with(status) {
                            if (balance > 0 && expiryDate.isNotEmpty() && expiringAmount.isNotEmpty()) {
                                getFormattedExpiryDate(expiryDate, expiringAmount, type)
                            } else ""
                        }
                    }
                    is Empty -> {
                        when (type) {
                            LoadBalance, RewardPoints -> balancePlaceholder
                            else -> ""
                        }
                    }
                    else -> ""
                }
            }

            // Specific cases for GCash and Loan balance
            tvLinkGCash.isVisible = status is Empty && status.linkGCash
            tvLoadToPayBalance.isVisible = when (status) {
                is Success -> type is LoanBalance && status.balance > 0
                else -> false
            }

            // Reward points star icon
            ivStar.isVisible = type == RewardPoints && status != Loading
        }
    }

    private fun getFormattedBalance(balance: Float, type: BalanceType): String {
        return when (type) {
            LoadBalance, GCashBalance, LoanBalance -> {
                balance.toPezosFormattedDisplayBalance()
            }
            RewardPoints -> {
                balance.toFormattedDisplayBalance()
            }
        }
    }

    private fun getFormattedExpiryDate(date: String, amount: String, type: BalanceType): String {
        return when (type) {
            LoadBalance -> {
                context.getString(
                    R.string.load_expires_on,
                    amount,
                    date.toDateWithTimeZoneOrNull().toFormattedStringOrEmpty(GlobeDateFormat.Default)
                )
            }
            RewardPoints -> {
                context.getString(
                    R.string.rewards_expires_on,
                    amount,
                    date.toDateWithTimeZoneOrNull().toFormattedStringOrEmpty(GlobeDateFormat.Default)
                )
            }
            else -> ""
        }
    }
}

internal sealed class BalanceType {
    object LoadBalance : BalanceType()
    object RewardPoints : BalanceType()
    object GCashBalance : BalanceType()
    object LoanBalance : BalanceType()
}
