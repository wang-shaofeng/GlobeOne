package ph.com.globe.globeonesuperapp.account.bill

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.databinding.BillDetailsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.balance.toPezosFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.account.BillStatus
import ph.com.globe.model.account.PostpaidPaymentStatus
import ph.com.globe.model.profile.domain_models.isPostpaidMobile
import ph.com.globe.model.util.AccountStatus
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateOrNull
import ph.com.globe.util.toFormattedStringOrEmpty

@AndroidEntryPoint
class BillDetailsFragment :
    NoBottomNavViewBindingFragment<BillDetailsFragmentBinding>(bindViewBy = {
        BillDetailsFragmentBinding.inflate(it)
    }) {

    private val accountDetailsViewModel: AccountDetailsViewModel by hiltNavGraphViewModels(R.id.account_subgraph)
    private val billDetailsViewModel: BillDetailsViewModel by hiltNavGraphViewModels(R.id.account_subgraph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        billDetailsViewModel.fetchBillStatements(accountDetailsViewModel.selectedEnrolledAccount)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {

            vpBills.adapter = SimplePagerAdapter(
                listOf(getString(R.string.current), getString(R.string.previous)),
                isPostpaidMobileAccount()
            )
            tlBills.setupWithViewPager(vpBills)
            tlBills.isVisible = isPostpaidMobileAccount()

            with(accountDetailsViewModel) {
                wfBillDetails.onBack {
                    findNavController().navigateUp()
                }

                with(incCurrent) {
                    btnPay.setOnClickListener {
                        findNavController().safeNavigate(BillDetailsFragmentDirections.actionBillDetailsFragmentToBillingsChooseAmountFragment())
                    }
                    accountAlias.observe(viewLifecycleOwner) { alias ->
                        tvAccountName.text = alias
                        incBillInfo.tvAccountAlias.text = alias
                    }

                    accountStatus.observe(viewLifecycleOwner) { accountStatus ->
                        if (accountStatus == AccountStatus.Disconnected) {
                            tvMissedPaymentDescription.text =
                                getString(R.string.account_details_bill_missed_payment_description_disconnected_state)
                            tvWarning.text =
                                getString(R.string.account_details_bill_missed_payment_warning_disconnected_state)
                        }
                    }

                    billingDetails.observe(viewLifecycleOwner) {

                        clTotalAmount.isVisible =
                            (it.outstandingBalance.toFloatOrNull() ?: 0f) > 0f
                        clAllSet.isVisible =
                            (it.outstandingBalance.toFloatOrNull() ?: 1f) <= 0f

                        clDetails.isVisible =
                            (it.outstandingBalance.toFloatOrNull() ?: 0f) > 0f
                        clDetailsAllset.isVisible =
                            (it.outstandingBalance.toFloatOrNull() ?: 0f) < 0f
                        groupLowerDecorations.isVisible =
                            (it.outstandingBalance.toFloatOrNull() ?: 1f) != 0f

                        btnPay.text =
                            getString(
                                if ((it.outstandingBalance.toFloatOrNull() ?: 1f) > 0)
                                    R.string.account_details_bill_pay_now
                                else
                                    R.string.account_details_bill_pay_in_advance
                            )
                        if ((it.outstandingBalance.toFloatOrNull() ?: 0f) > 0f)
                            with(billDetailsViewModel) {
                                showBubbleIfFirstEntrance()
                                bubbleVisibilityState.observe(viewLifecycleOwner) {
                                    if (it) {
                                        ivPdfBubble.visibility = View.VISIBLE
                                        val alphaAnimation = AlphaAnimation(0.0f, 1.0f)
                                        alphaAnimation.duration = 500
                                        ivPdfBubble.startAnimation(alphaAnimation)
                                    } else {
                                        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)
                                        alphaAnimation.duration = 500
                                        ivPdfBubble.startAnimation(alphaAnimation)
                                        ivPdfBubble.visibility = View.GONE
                                    }
                                }
                            }

                        tvExcessPaymentAmount.text = getString(
                            R.string.pezos_prefix,
                            (it.outstandingBalance.toFloat() * -1f).toString()
                        )

                        tvTotalAmount.text =
                            (it.outstandingBalance.toFloatOrNull()
                                ?: 0f).toPezosFormattedDisplayBalance()
                        tvBillAmount.text =
                            (it.billAmount?.toFloatOrNull() ?: 0f).toPezosFormattedDisplayBalance()

                        /**
                         * 1. if account is not postpaid-mobile, load bill duration from billing details
                         * 2. other accounts will load bill duration from bill statements
                         */
                        if (isPostpaidMobileAccount().not()) {
                            if (it.billStartDate?.toDateOrNull() != null &&
                                it.billEndDate?.toDateOrNull() != null
                            ) {
                                tvTimeSpan.apply {
                                    visibility = View.VISIBLE
                                    text = getString(
                                        R.string.account_details_template_bill_time_span,
                                        it.billStartDate?.toDateOrNull()
                                            .toFormattedStringOrEmpty(GlobeDateFormat.Default),
                                        it.billEndDate?.toDateOrNull()
                                            .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                                    )
                                }
                            }
                        }
                    }

                    postpaidBillStatus.observe(viewLifecycleOwner) { billStatus ->
                        if (billStatus is BillStatus.Success) {
                            billStatus.paymentStatus?.let { postpaidPaymentStatus ->
                                tvDueDate.text = getString(
                                    if (postpaidPaymentStatus is PostpaidPaymentStatus.BillOverdue)
                                        R.string.account_details_template_bill_due_last
                                    else
                                        R.string.account_details_template_bill_due_on,
                                    billStatus.dueDate.toFormattedStringOrEmpty(GlobeDateFormat.Default)
                                )
                                bscBillStatus.setPaymentStatus(postpaidPaymentStatus)
                                cvOverdueWarning.isVisible =
                                    postpaidPaymentStatus is PostpaidPaymentStatus.BillOverdue
                                tvWarning.isVisible =
                                    postpaidPaymentStatus is PostpaidPaymentStatus.BillOverdue
                                tvViewPdfBill.setOnClickListener { view ->
                                    findNavController().safeNavigate(
                                        BillDetailsFragmentDirections.actionBillDetailsFragmentToORStatementFragment(
                                            accountDetailsViewModel.selectedEnrolledAccount,
                                            ""
                                        )
                                    )
                                }
                            }
                        }
                    }

                    tvViewPdfBill.setOnClickListener {
                        findNavController().safeNavigate(
                            BillDetailsFragmentDirections.actionBillDetailsFragmentToORStatementFragment(
                                accountDetailsViewModel.selectedEnrolledAccount,
                                ""
                            )
                        )
                    }
                }
            }

            if (isPostpaidMobileAccount()) {
                /**
                 * only postpaid-mobile account can show previous page and load bill statements data
                 * see https://lotusflare.atlassian.net/wiki/spaces/GlobeOne/pages/3457548632/GOREQ+S16+SS+1+Postpaid+Mobile+Account+Details
                 */
                with(billDetailsViewModel) {
                    billStatementsStatus.observe(viewLifecycleOwner) { billStatementsStatus ->
                        with(billStatementsStatus is BillDetailsViewModel.BillStatementsStatus.Loading) {
                            incCurrent.pbCurrentLoading.isVisible = this
                            pbPreviousLoading.isVisible = this
                            incCurrent.groupDataLoadSuccess.isVisible = this.not()
                        }

                        if (billStatementsStatus is BillDetailsViewModel.BillStatementsStatus.Success) {
                            billStatementsStatus.billStatements.let {
                                it.getOrNull(0)?.let { currentBill ->
                                    with(incCurrent) {
                                        if (currentBill.billStartDate?.toDateOrNull() != null &&
                                            currentBill.billEndDate?.toDateOrNull() != null
                                        ) {
                                            tvTimeSpan.apply {
                                                visibility = View.VISIBLE
                                                text = getString(
                                                    R.string.account_details_template_bill_time_span,
                                                    currentBill.billStartDate?.toDateOrNull()
                                                        .toFormattedStringOrEmpty(GlobeDateFormat.Default),
                                                    currentBill.billEndDate?.toDateOrNull()
                                                        .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                                                )
                                            }
                                        }
                                    }
                                }

                                it.getOrNull(1)?.let { previousBill ->
                                    with(incBillInfo) {
                                        root.isVisible = true

                                        tvDates.text = getString(
                                            R.string.account_details_template_bill_time_span,
                                            previousBill.billStartDate?.toDateOrNull()
                                                .toFormattedStringOrEmpty(GlobeDateFormat.Default),
                                            previousBill.billEndDate?.toDateOrNull()
                                                .toFormattedStringOrEmpty(GlobeDateFormat.Default)
                                        )

                                        tvBillAmount.text =
                                            previousBill.totalAmount?.toPezosFormattedDisplayBalance()

                                        btnViewBill.setOnClickListener {
                                            previousBill.id?.let { id ->
                                                findNavController().safeNavigate(
                                                    BillDetailsFragmentDirections.actionBillDetailsFragmentToORStatementFragment(
                                                        accountDetailsViewModel.selectedEnrolledAccount,
                                                        id,
                                                        previousBill.verificationToken
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (billStatementsStatus is BillDetailsViewModel.BillStatementsStatus.Error) {
                            incCurrent.vDivider.visibility = View.VISIBLE
                        }
                    }
                }

                btnViewHistory.setOnClickListener {
                    findNavController().safeNavigate(
                        BillDetailsFragmentDirections.actionBillDetailsFragmentToAccountActivitiesSubgraph(
                            accountDetailsViewModel.selectedEnrolledAccount
                        )
                    )
                }
            } else {
                // other accounts don't load bill statements data
                incCurrent.pbCurrentLoading.isVisible = false
                /**
                 * if account is ICCBS, don't show the bill amount and bill duration views
                 * see https://lotusflare.atlassian.net/wiki/spaces/GlobeOne/pages/3723198465/GOREQ+S19+5+ICCBS+API+mapping+issue
                 */
                incCurrent.groupDataLoadSuccess.isVisible = isICCBSAccount().not()
            }
        }
    }

    private fun isICCBSAccount() = accountDetailsViewModel.isICCBSAccount()

    private fun isPostpaidMobileAccount() = accountDetailsViewModel.selectedEnrolledAccount.isPostpaidMobile()

    override val logTag = "BillDetailsFragment"
}

class SimplePagerAdapter(private val tabs: List<String>, private val showPreviousTab: Boolean) :
    PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var resId = 0
        when (position) {
            CURRENT_TAB -> resId = R.id.inc_current
            PREVIOUS_TAB -> resId = R.id.sv_previous
        }
        return container.findViewById(resId)
    }

    override fun getPageTitle(position: Int): CharSequence = tabs[position]

    // if account is postpaid-mobile, show the previous tab
    override fun getCount(): Int = if (showPreviousTab) SHOW_PREVIOUS_TAB_COUNT else HIDE_PREVIOUS_TAB_COUNT

    override fun isViewFromObject(view: View, any: Any): Boolean = view == any

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        // this method has to be empty
    }

    companion object {
        private const val CURRENT_TAB = 0
        private const val PREVIOUS_TAB = 1
        private const val HIDE_PREVIOUS_TAB_COUNT = 1
        private const val SHOW_PREVIOUS_TAB_COUNT = 2
    }
}
