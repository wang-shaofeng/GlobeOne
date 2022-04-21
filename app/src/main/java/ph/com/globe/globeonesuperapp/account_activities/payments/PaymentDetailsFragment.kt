/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.payments

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.domain.utils.convertDateToGroupDataFormat
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesViewModel
import ph.com.globe.globeonesuperapp.databinding.PaymentDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment

@AndroidEntryPoint
class PaymentDetailsFragment :
    NoBottomNavViewBindingFragment<PaymentDetailsFragmentBinding>({
        PaymentDetailsFragmentBinding.inflate(it)
    }) {

    private val accountActivityViewModel by hiltNavGraphViewModels<AccountActivitiesViewModel>(R.id.account_activities_subgraph)

    private val args by navArgs<PaymentDetailsFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            accountActivityViewModel.enrolledAccount.observe(viewLifecycleOwner) {
                tvAccount.text = it.accountAlias
                tvNumber.text = it.primaryMsisdn.toDisplayUINumberFormat()
            }

            wfBillPaymentToHistory.onBack {
                findNavController().navigateUp()
            }

            with(args.payment) {
                tvAmountPaidValue.text = amount.toFloat().toPezosFormattedDisplayBalance()
                tvPaymentChannelValue.text = sourceId
                tvPaymentDate.text = date.convertDateToGroupDataFormat(true)
                tvPostingDateValue.text = loadTime.convertDateToGroupDataFormat(true)
                tvOrNumberValue.text = receiptId
            }

            btnViewOR.setOnClickListener {
                findNavController().safeNavigate(
                    PaymentDetailsFragmentDirections.actionPaymentDetailsFragmentToOrPaymentFragment(
                        args.payment,
                        args.token
                    )
                )
            }
        }
    }

    override val logTag: String = "PaymentDetailsFragment"
}
