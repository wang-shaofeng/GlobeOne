package ph.com.globe.globeonesuperapp.account.bill

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.databinding.BillingsChooseAmountFragmentBinding
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.hideError
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.BILL
import ph.com.globe.globeonesuperapp.utils.payment.PAY_BILLS
import ph.com.globe.globeonesuperapp.utils.payment.checkoutToPaymentParams
import ph.com.globe.globeonesuperapp.utils.payment.pesosToDouble
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.showError
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.util.AccountStatus
import ph.com.globe.model.account.extractStatus
import ph.com.globe.model.util.brand.*

@AndroidEntryPoint
class BillingsChooseAmountFragment :
    NoBottomNavViewBindingFragment<BillingsChooseAmountFragmentBinding>({
        BillingsChooseAmountFragmentBinding.inflate(it)
    }) {

    private val accountDetailsViewModel: AccountDetailsViewModel by hiltNavGraphViewModels(R.id.account_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {
            with(accountDetailsViewModel) {
                tvPrimaryMsisdnTitle.text =
                    if (selectedEnrolledAccount.segment == AccountSegment.Broadband) getString(R.string.account_number) else getString(
                        R.string.mobile_number
                    )
                tvPrimaryMsisdn.text = selectedEnrolledAccount.primaryMsisdn
                tvBillBrandTitle.text =
                    AccountBrand.GhpPostpaid.toUserFriendlyBrandName(selectedEnrolledAccount.segment)
                accountDetails.observe(viewLifecycleOwner, {
                    tvAccountName.text =
                        getString(R.string.account_name_placeholder, it.firstName, it.lastName)
                    tvMinimumAmount.text = getString(
                        R.string.minimum_amount_is_100,
                        if (it.statusDescription.extractStatus() == AccountStatus.Disconnected)
                            getString(R.string.pay_in_full)
                        else ""
                    )
                })

                billingDetails.observe(viewLifecycleOwner, {
                    var balance = it.outstandingBalance.toFloatOrNull() ?: 0f
                    if (balance < MINIMUM_AMOUNT) {
                        balance = MINIMUM_AMOUNT
                    }
                    tvAmountToPay.text = balance.toPezosFormattedDisplayBalance()
                    etAmount.setText(balance.toFormattedDisplayBalance())
                })

                etAmount.addTextChangedListener {
                    val amount = it.toString().replace(",", "").toFloatOrNull() ?: 0.0f
                    if (amount >= MINIMUM_AMOUNT) {
                        btnProceed.isEnabled = true
                        requireContext().hideError(tilAmount, etAmount)
                        tilAmount.setPrefixTextColor(
                            ColorStateList.valueOf(resources.getColor(R.color.neutral_A_0))
                        )
                    } else {
                        requireContext().showError(
                            tilAmount,
                            etAmount,
                            getString(R.string.minimum_amount_error)
                        )
                        tilAmount.setPrefixTextColor(
                            ColorStateList.valueOf(resources.getColor(R.color.error_text_red))
                        )
                        btnProceed.isEnabled = false
                    }
                }

                btnProceed.setOnClickListener {
                    val amount = etAmount.text.toString().pesosToDouble()
                    accountDetails.observe(viewLifecycleOwner, {
                        val checkout = checkoutToPaymentParams(
                            paymentType = PAY_BILLS,
                            transactionType = BILL,
                            mobileNumber = it.mobileNumber ?: "",
                            accountNumber = it.accountNumber,
                            accountName = "${it.firstName} ${it.lastName}",
                            emailAddress = it.email,
                            amount = amount,
                            paymentName = brandValue?.toUserFriendlyBrandName(selectedEnrolledAccount.segment) ?: "",
                            price = amount,
                            provisionByServiceId = false,
                            accountStatus = it.statusDescription,
                            billingFullPayment = tvAmountToPay.text.toString()
                                .pesosToDouble() == amount
                        )
                        findNavController().safeNavigate(
                            BillingsChooseAmountFragmentDirections.actionBillingsChooseAmountFragmentToPaymentSubgraph(
                                checkout
                            )
                        )

                    })
                }

                btnCancel.setOnClickListener {
                    findNavController().navigateUp()
                }

                wfChooseAmount.onBack {
                    findNavController().navigateUp()
                }

                wfChooseAmount.onClose {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override val logTag = "BillingsChooseAmountFragment"
}

const val MINIMUM_AMOUNT = 100.0f
