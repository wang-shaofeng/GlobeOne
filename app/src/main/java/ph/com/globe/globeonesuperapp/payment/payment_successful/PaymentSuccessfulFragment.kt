/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_successful

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.PaymentSuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.group.OWNER_ALIAS
import ph.com.globe.globeonesuperapp.group.OWNER_NUMBER
import ph.com.globe.globeonesuperapp.group.SKELLING_CATEGORY
import ph.com.globe.globeonesuperapp.group.SKELLING_WALLET
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.intToPesos
import ph.com.globe.globeonesuperapp.utils.payment.setValidityText
import ph.com.globe.globeonesuperapp.utils.payment.toPesosWithDecimal
import ph.com.globe.globeonesuperapp.utils.permissions.registerActivityResultForStoragePermission
import ph.com.globe.globeonesuperapp.utils.permissions.requestStoragePermissionsIfNeededAndPerformSuccessAction
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.account.extractStatus
import ph.com.globe.model.payment.*
import ph.com.globe.model.profile.domain_models.isPostpaid
import ph.com.globe.model.util.AccountStatus
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.isNoExpiry
import ph.com.globe.util.toEndDate
import ph.com.globe.util.toFormattedStringOrEmpty
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class PaymentSuccessfulFragment :
    NoBottomNavViewBindingFragment<PaymentSuccessfulFragmentBinding>(
        bindViewBy = { PaymentSuccessfulFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)
    private val contactsViewModel: ContactsViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val successArgs by navArgs<PaymentSuccessfulFragmentArgs>()

    private val viewModel: PaymentSuccessfulViewModel by viewModels()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorageActivityLauncher = registerActivityResultForStoragePermission {
            takeScreenshotFlow(viewBinding)
        }
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:payment success screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            ivCopyToClipboard.setOnClickListener {
                logUiActionEvent("Order no. copy option")
                requireContext().copyToClipboard(
                    tvReferenceNumberValue.text.toString(),
                    COPIED_REFERENCE_NUMBER
                )

                val snackbarViewBinding =
                    GlobeSnackbarLayoutBinding
                        .inflate(LayoutInflater.from(requireContext()))
                snackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.copied_to_clipboard)
                snackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.you_have_copied_the_order_number)

                showSnackbar(snackbarViewBinding)
            }
            ivDownloadReceipt.setOnClickListener {

                logUiActionEvent("Download receipt option")
                if (requestStorageActivityLauncher != null) {
                    requestStoragePermissionsIfNeededAndPerformSuccessAction(
                        requestStorageActivityLauncher!!
                    )
                } else {
                    takeScreenshotFlow(viewBinding)
                }
            }

            with(navigationViewModel.paymentParameters) {
                btnViewFreebie.isVisible = isFreebieVoucher
                freebieGroup.isVisible = isFreebieVoucher
                tvFreebieContent.text = freebieName
                when {
                    this.isExclusivePromo -> {
                        lavReceipt.setAnimation(R.raw.success)
                        tvSuccessReceipt.setText(R.string.were_now_processing_your_promo)
                        tvSuccessReceiptDescription.setText(R.string.exclusive_promo_description)
                    }
                    (!provisionByServiceId || selectedBoosters?.any { !provisionByServiceId } == true) -> {
                        tvSuccessReceipt.setText(
                            when (purchaseType) {
                                is PurchaseType.BuyPromo -> {
                                    R.string.your_promo_is_on_the_way
                                }
                                is PurchaseType.BuyContentRegular -> {
                                    R.string.your_content_subscription_is_on_the_way
                                }
                                else -> {
                                    R.string.success
                                }
                            }
                        )
                    }
                    else -> {
                        tvSuccessReceipt.setText(
                            if (purchaseType is PurchaseType.BuyPromo && ((purchaseType as? PurchaseType.BuyPromo)?.isGoPlus == true) &&
                                globePaymentMethod is GlobePaymentMethod.ChargeToLoad
                            )
                                R.string.your_promo_is_on_the_way
                            else
                                R.string.success
                        )
                    }
                }

                if (accountStatus.extractStatus() == AccountStatus.Disconnected) {
                    clBillInfo.visibility = View.VISIBLE
                    tvBillMessage.text = getString(
                        if (billingFullPayment == true) R.string.full_payment_bill_info
                        else R.string.partial_payment_bill_info
                    )
                }

                contactsViewModel.enrolledAccounts.observe(
                    viewLifecycleOwner
                ) { list ->
                    val enrolledAccount =
                        list.filter { !it.isPostpaid() }
                            .find { it.primaryMsisdn == primaryMsisdn.convertToPrefixNumberFormat() }
                    // for freebie voucher service, direct to account detail voucher screen
                    btnViewFreebie.setOnClickListener {
                        enrolledAccount?.let {
                            findNavController().safeNavigate(
                                PaymentSuccessfulFragmentDirections.actionPaymentSuccessfulFragmentToVouchersSubgraph(
                                    enrolledAccount = it,
                                    isShowContentTab = true
                                )
                            )
                        }
                    }
                    if (enrolledAccount != null && isGroupDataPromo && !navigationViewModel.paymentParameters.isExclusivePromo) {
                        tvActionAlternative.text = resources.getString(R.string.setup_later)
                        tvActionAlternative.visibility = View.VISIBLE
                        btnSetupGroupData.visibility = View.VISIBLE
                        btnAction.visibility = View.GONE

                        btnSetupGroupData.setOnClickListener {
                            logCustomEvent(
                                analyticsEventsProvider.provideEvent(
                                    EventCategory.Engagement,
                                    SETUP_SCREEN, BUTTON, SETUP_GROUP_DATA
                                )
                            )
                            findNavController().safeNavigate(
                                R.id.action_paymentSuccessfulFragment_to_group_subgraph,
                                bundleOf(
                                    OWNER_NUMBER to enrolledAccount.primaryMsisdn,
                                    OWNER_ALIAS to enrolledAccount.accountAlias,
                                    SKELLING_WALLET to (skelligWallet ?: ""),
                                    SKELLING_CATEGORY to (skelligCategory ?: "")
                                )
                            )
                        }

                        tvActionAlternative.setOnClickListener {
                            crossBackstackNavigator.crossNavigateWithoutHistory(
                                BaseActivity.DASHBOARD_KEY,
                                R.id.dashboardFragment
                            )
                        }
                    }
                }

                btnAction.apply {
                    text = getString(
                        when (purchaseType) {
                            is PurchaseType.BuyContentRegular -> {
                                R.string.activate_now
                            }
                            is PurchaseType.BuyContentVoucher -> {
                                if (getVoucherCode() != null) {
                                    R.string.use_code_in_partner_app
                                } else {
                                    R.string.activate_in_partner_app
                                }
                            }
                            else -> R.string.done
                        }
                    )
                    setOnClickListener {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Conversion,
                                SUBSCRIPTION_SUCCESS_SCREEN, BUTTON, DONE
                            )
                        )

                        when (purchaseType) {
                            is PurchaseType.BuyContent -> {
                                logCustomEvent(
                                    analyticsEventsProvider.provideEvent(
                                        EventCategory.Conversion,
                                        ACTIVATION_SCREEN, BUTTON, PROCEED_TO_ACTIVATION,
                                        productName = paymentName
                                    )
                                )
                                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(partnerRedirectionLink)
                                    )
                                    startActivity(intent)

                                    if (isLoggedIn) {
                                        crossBackstackNavigator.crossNavigateWithoutHistory(
                                            BaseActivity.DASHBOARD_KEY,
                                            R.id.dashboardFragment
                                        )
                                    } else {
                                        crossBackstackNavigator.crossNavigateWithoutHistory(
                                            BaseActivity.AUTH_KEY,
                                            R.id.selectSignMethodFragment
                                        )
                                    }
                                }
                            }
                            else -> {
                                when (isLoggedIn) {
                                    true -> when {
                                        successArgs.isHpwAndChargeToLoad && !navigationViewModel.paymentParameters.isEnrolledAccount -> {
                                            // if the user is logged-in but is buying Hpw offer via charge-to-load for an non-enrolled account
                                            // we prompt him with a screen to enroll that account
                                            findNavController().safeNavigate(
                                                PaymentSuccessfulFragmentDirections.actionPaymentSuccessfulFragmentToPaymentHpwEnrollPromptFragment(
                                                    numberToEnroll = navigationViewModel.paymentParameters.primaryMsisdn
                                                )
                                            )
                                        }
                                        else -> {
                                            // in every other case we navigate to the dashboard
                                            crossBackstackNavigator.crossNavigateWithoutHistory(
                                                BaseActivity.DASHBOARD_KEY,
                                                R.id.dashboardFragment
                                            )
                                        }
                                    }
                                    false -> when {
                                        // if this is the first time of the session that the user is buying an HPW offer as a non logged-in
                                        // we are prompting him with the screen to sign up
                                        successArgs.isHpwAndChargeToLoad && viewModel.shouldShowSingUpPromptHpw() -> {
                                            findNavController().safeNavigate(R.id.action_paymentSuccessfulFragment_to_paymentHpwSignUpPromptFragment)
                                        }
                                        // if this is the first time of the session that the user is successfully buying anything as a non logged-in
                                        // we are prompting him with the different screen to sign up too
                                        viewModel.shouldShowSingUpPrompt() -> {
                                            findNavController().safeNavigate(R.id.action_paymentSuccessfulFragment_to_paymentSignUpPromptFragment)
                                        }
                                        // in all other cases we navigate to the select user screen
                                        else -> {
                                            crossBackstackNavigator.crossNavigateWithoutHistory(
                                                BaseActivity.AUTH_KEY,
                                                R.id.selectSignMethodFragment
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (purchaseType is PurchaseType.BuyContentVoucher) {
                    getVoucherCode()?.let { voucherCode ->
                        clVoucherCode.visibility = View.VISIBLE
                        tvVoucherCodeValue.text = voucherCode

                        ivCopyVoucherCode.setOnClickListener {
                            logUiActionEvent("Voucher code copy option")
                            requireContext().copyToClipboard(voucherCode, COPIED_VOUCHER_CODE)

                            val snackbarViewBinding =
                                GlobeSnackbarLayoutBinding
                                    .inflate(LayoutInflater.from(requireContext()))
                            snackbarViewBinding.tvGlobeSnackbarTitle.setText(R.string.copied_to_clipboard)
                            snackbarViewBinding.tvGlobeSnackbarDescription.setText(R.string.you_have_copied_the_voucher_code)

                            showSnackbar(snackbarViewBinding)
                        }
                    }
                }

                tvActionAlternative.apply {
                    when (purchaseType) {
                        is PurchaseType.BuyContentRegular -> {
                            if (isLoggedIn && isEnrolledAccount && monitoredInApp) {
                                visibility = View.VISIBLE
                                text = getString(R.string.activate_later)
                                setOnClickListener {
                                    logCustomEvent(
                                        analyticsEventsProvider.provideEvent(
                                            EventCategory.Conversion,
                                            SUBSCRIPTION_SUCCESS_SCREEN, BUTTON, ACTIVATE_LATER
                                        )
                                    )
                                    crossBackstackNavigator.crossNavigateWithoutHistory(
                                        BaseActivity.DASHBOARD_KEY,
                                        R.id.dashboardFragment
                                    )
                                }
                            }
                        }
                        is PurchaseType.BuyContentVoucher -> {
                            visibility = View.VISIBLE
                            text = getString(R.string.done)
                            setOnClickListener {
                                logCustomEvent(
                                    analyticsEventsProvider.provideEvent(
                                        EventCategory.Conversion,
                                        SUBSCRIPTION_SUCCESS_SCREEN, BUTTON, DONE
                                    )
                                )
                                viewModel.showVoucherActivationInfoDialog(getVoucherCode(), {
                                    if (isLoggedIn) {
                                        crossBackstackNavigator.crossNavigateWithoutHistory(
                                            BaseActivity.DASHBOARD_KEY,
                                            R.id.dashboardFragment
                                        )
                                    } else {
                                        crossBackstackNavigator.crossNavigateWithoutHistory(
                                            BaseActivity.AUTH_KEY,
                                            R.id.selectSignMethodFragment
                                        )
                                    }
                                    logCustomEvent(
                                        analyticsEventsProvider.provideEvent(
                                            EventCategory.Engagement,
                                            PAYMENT_SUCCESS_SCREEN, BUTTON, OKAY
                                        )
                                    )
                                }, {
                                    logCustomEvent(
                                        analyticsEventsProvider.provideEvent(
                                            EventCategory.Engagement,
                                            PAYMENT_SUCCESS_SCREEN, BUTTON, BACK
                                        )
                                    )
                                })
                            }
                        }
                        else -> { //nothing
                        }
                    }
                }

                selectedBoosters?.let { list ->
                    if (list.isNotEmpty()) {
                        rvSelectedBoosters.visibility = View.VISIBLE
                        rvSelectedBoosters.adapter =
                            SelectedBoostersRecyclerViewAdapter().apply {
                                submitList(list.filter {
                                    // filer out the boosters that haven't been purchased successfully
                                    currentTransactionsResult.checkIfThisBoosterIsPurchased(
                                        it.serviceId,
                                        it.nonChargeServiceId,
                                        it.productKeyword
                                    )
                                })
                            }
                    }
                }
                if (currentTransactionsResult.checkIfSomeBoostersHaveFailed()) {
                    // change UI(text, image) if the purchase is partially successful (some boosters haven't been provisioned)
                    val numberOfProvisionedBoosters =
                        currentTransactionsResult.getNumOfSuccessfullyProvisionedBoosters()
                    val numberOfTotalBoosters = ((currentTransactionsResult?.size ?: 0) - 1)

                    lavReceipt.setAnimation(R.raw.failure)

                    if (numberOfProvisionedBoosters == 0) {
                        tvSuccessReceipt.text = getString(
                            R.string.we_werent_able_to_register_your_boosters
                        )
                        tvSuccessReceiptDescription.text =
                            getString(
                                R.string.payment_complete_none_of_the_boosters,
                                paymentName,
                                convertToDescriptionPaymentMethod(
                                    method = globePaymentMethod,
                                    refundSuccessful = refundSuccessful ?: true
                                ),
                                if (successArgs.isShareFlow || globePaymentMethod == GlobePaymentMethod.ChargeToLoad)
                                    getString(R.string.havent_earned_points)
                                else
                                    getString(R.string.earned_points)
                            )

                    } else {
                        tvSuccessReceipt.text = getString(
                            R.string.n_of_n_booster_registered,
                            numberOfProvisionedBoosters,
                            numberOfTotalBoosters
                        )
                        tvSuccessReceiptDescription.text = getString(
                            R.string.payment_complete_n_of_n_boosters,
                            paymentName,
                            numberOfProvisionedBoosters,
                            numberOfTotalBoosters,
                            convertToDescriptionPaymentMethod(
                                method = globePaymentMethod,
                                refundSuccessful = refundSuccessful ?: true
                            ),
                            if (successArgs.isShareFlow
                                || (selectedBoosters?.isNotEmpty() == true)
                                || globePaymentMethod == GlobePaymentMethod.ChargeToLoad
                            )
                                getString(R.string.havent_earned_points)
                            else
                                getString(R.string.earned_points)
                        )
                    }
                } else {
                    tvSuccessReceiptDescription.text = when (purchaseType) {
                        is PurchaseType.BuyContentRegular -> {
                            if (!provisionByServiceId)
                                getString(R.string.we_will_text_you_about_the_status)
                            else
                                getString(
                                    R.string.payment_complete_emailed_and_texted_confirmation,
                                    if (globePaymentMethod == GlobePaymentMethod.ChargeToLoad) "" else
                                        getString(R.string.earned_points)
                                )
                        }
                        is PurchaseType.BuyContentVoucher -> {
                            StringBuilder(
                                getString(
                                    R.string.payment_complete_texted_confirmation
                                )
                            ).append(
                                if (globePaymentMethod == GlobePaymentMethod.ChargeToLoad) "" else getString(
                                    R.string.earned_points
                                )
                            )
                        }
                        is PurchaseType.BuyGoCreatePromo -> {
                            getString(R.string.payment_complete_texted_confirmation)
                        }
                        is PurchaseType.PayBill -> {
                            getString(R.string.payment_complete_emailed_and_texted_confirmation, "")
                        }
                        else -> {
                            if (purchaseType is PurchaseType.BuyPromo && ((purchaseType as? PurchaseType.BuyPromo)?.isGoPlus == true)) {
                                if (globePaymentMethod is GlobePaymentMethod.ChargeToLoad) {
                                    SpannableStringBuilder(
                                        getString(R.string.we_will_text_you_a_confirmation)
                                    ).also {
                                        if (isFreebieVoucher) {
                                            it.append(" ")
                                            it.append(getString(R.string.when_successful_see_in_the_pocket))
                                            val startIndex = it.indexOf(getString(R.string.pocket))
                                            it.setSpan(
                                                StyleSpan(Typeface.BOLD),
                                                startIndex,
                                                startIndex + getString(
                                                    R.string.pocket
                                                ).length,
                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                            )
                                        }
                                    }
                                } else {
                                    SpannableStringBuilder(
                                        getString(R.string.payment_complete_texted_confirmation)
                                    ).also {
                                        if (isFreebieVoucher) {
                                            it.append(" ")
                                            it.append(getString(R.string.see_in_the_pocket))
                                            val startIndex = it.indexOf(getString(R.string.pocket))
                                            it.setSpan(
                                                StyleSpan(Typeface.BOLD),
                                                startIndex,
                                                startIndex + getString(
                                                    R.string.pocket
                                                ).length,
                                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                            )
                                        }
                                        it.append(
                                            getString(
                                                if (successArgs.isShareFlow
                                                    || (selectedBoosters?.isNotEmpty() == true)
                                                    || globePaymentMethod == GlobePaymentMethod.ChargeToLoad
                                                )
                                                    R.string.havent_earned_points
                                                else
                                                    R.string.earned_points
                                            )
                                        )
                                    }
                                }
                            } else if (purchaseType is PurchaseType.BuyPromo && (!provisionByServiceId || selectedBoosters?.any { !provisionByServiceId } == true))
                                getString(R.string.we_will_text_you_about_the_status)
                            else
                                StringBuilder(getString(R.string.payment_complete_texted_confirmation)).append(
                                    getString(
                                        if (successArgs.isShareFlow
                                            || (selectedBoosters?.isNotEmpty() == true)
                                            || (globePaymentMethod == GlobePaymentMethod.ChargeToLoad && purchaseType !is PurchaseType.BuyLoad)
                                        )
                                            R.string.havent_earned_points
                                        else
                                            if (purchaseType is PurchaseType.BuyLoad) {
                                                if (currentAmount != null) {
                                                    R.string.earned_points
                                                } else {
                                                    R.string.check_by_calling
                                                }
                                            } else {
                                                R.string.earned_points
                                            }
                                    )
                                )
                        }
                    }
                }
                tvPurchaseType.text = getString(
                    when (purchaseType) {
                        is PurchaseType.BuyLoad -> R.string.shop_tab_load
                        is PurchaseType.BuyPromo -> R.string.promo
                        is PurchaseType.BuyContent -> R.string.shop_tab_content
                        is PurchaseType.BuyGoCreatePromo -> R.string.promo
                        is PurchaseType.PayBill -> R.string.biller
                    }
                )

                val isPaidForLoad = purchaseType is PurchaseType.BuyLoad

                if (isPaidForLoad) {
                    tvPurchaseType.isVisible = false
                }

                // 'sent to' section is only visible if the user is paying bills, load or successfully purchased
                // at least one booster
                val isSentToSectionVisible =
                    (purchaseType is PurchaseType.PayBill) || currentTransactionsResult.getNumOfSuccessfullyProvisionedBoosters() > 0 || successArgs.isShareFlow
                            || isPaidForLoad
                tvSentTo.text = getString(
                    if (purchaseType is PurchaseType.PayBill) R.string.account
                    else R.string.sent_to
                )
                tvSentTo.isVisible = isSentToSectionVisible
                tvSentToNumber.text = if (purchaseType is PurchaseType.PayBill) {
                    SpannableStringBuilder().bold { append(accountName) }.append("\n")
                        .append(primaryMsisdn.toDisplayUINumberFormat())
                } else primaryMsisdn.toDisplayUINumberFormat()
                tvSentToNumber.isVisible = isSentToSectionVisible

                tvPurchaseName.text =
                    if (purchaseType is PurchaseType.PayBill) getString(R.string.globe) else paymentName
                tvAmountValue.text = price.toPesosWithDecimal()

                if (isPaidForLoad) {
                    tvPurchaseName.isVisible = false
                }

                if (successArgs.isShareFlow) {
                    tvServiceFee.text = getString(R.string.service_fee)
                    tvServiceFee.visibility = View.VISIBLE
                    tvServiceFeeValue.text = getShareFee().toPesosWithDecimal()
                    tvServiceFeeValue.visibility = View.VISIBLE
                    tvDeductedFrom.visibility = View.VISIBLE
                    tvDeductedFromNumber.text = successArgs.deductedFrom
                    if (purchaseType is PurchaseType.BuyLoad) {
                        tvPurchaseType.visibility = View.GONE
                        tvPurchaseName.visibility = View.GONE
                    }
                }

                if (purchaseType is PurchaseType.PayBill) {
                    tvDatePaid.visibility = View.VISIBLE
                    tvDatePaidValue.visibility = View.VISIBLE
                    tvDatePaidValue.text =
                        Date(System.currentTimeMillis()).toFormattedStringOrEmpty(GlobeDateFormat.MonthFullNameCustomDate)
                }

                val sumOfBoostersThatAreNotProvisioned = (selectedBoosters?.sumOf {
                    if (currentTransactionsResult.checkIfThisBoosterIsPurchased(
                            it.serviceId,
                            it.nonChargeServiceId,
                            it.productKeyword
                        )
                    ) 0 else it.boosterPrice.toInt()
                } ?: 0)

                if (sumOfBoostersThatAreNotProvisioned > 0 && globePaymentMethod !is GlobePaymentMethod.ChargeToLoad) {
                    // If there are non provisioned boosters and the method is GCash or Credit Card (meaning the user already paid for the offer)
                    // we should display the refund text to the user
                    tvRefund.visibility = View.VISIBLE
                    tvRefundAmount.visibility = View.VISIBLE
                    tvRefundAmount.text = sumOfBoostersThatAreNotProvisioned.intToPesos()
                }

                tvTotalAmount.text =
                    if (currentAmount != null && globePaymentMethod !is GlobePaymentMethod.ChargeToLoad) {
                        currentAmount.toPesosWithDecimal()
                    } else {
                        (totalAmount - sumOfBoostersThatAreNotProvisioned + getShareFee()).toPesosWithDecimal()
                    }

                discount.let {
                    if (it == 0.0) {
                        tvDiscount.visibility = View.GONE
                        tvDiscountAmount.visibility = View.GONE
                    } else {
                        tvDiscountAmount.text =
                            getString(R.string.discount_value, it.toPesosWithDecimal())
                    }
                }

                if (currentAmount != null && globePaymentMethod !is GlobePaymentMethod.ChargeToLoad) {
                    tvDiscount.visibility = View.VISIBLE
                    tvDiscountAmount.visibility = View.VISIBLE
                    tvDiscountAmount.text = getString(
                        R.string.discount_value,
                        (amount - currentAmount).toPesosWithDecimal()
                    )
                }

                validity?.let {
                    if (purchaseType !is PurchaseType.BuyContent) {
                        tvValidity.visibility = View.VISIBLE
                        tvValidityTimeFrame.visibility = View.VISIBLE
                        if (validity.isNoExpiry()) {
                            tvValidityTimeFrame.text = getString(R.string.valid_for_no_expiry)
                        } else {
                            tvValidityTimeFrame.text =
                                getString(
                                    R.string.validity_format,
                                    resources.setValidityText(validity),
                                    validity.toEndDate()
                                )
                        }
                    }
                }

                if (!referenceId.isNullOrEmpty()) {
                    tvReferenceNumberValue.text = referenceId
                } else {
                    groupReceiptId.visibility = View.GONE
                }
            }
        }

        // Disable back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
    }

    private fun getShareFee(): Double =
        if (successArgs.isShareFlow) {
            if (navigationViewModel.paymentParameters.purchaseType is PurchaseType.BuyPromo) navigationViewModel.paymentParameters.shareFee else HARDCODED_SHARELOAD_FEE
        } else 0.0

    private fun Fragment.convertToDescriptionPaymentMethod(
        method: GlobePaymentMethod?,
        refundSuccessful: Boolean
    ): String =
        when {
            method is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && refundSuccessful -> getString(
                R.string.gcash_with_refund
            )
            method is GlobePaymentMethod.ThirdPartyPaymentMethod.GCash && !refundSuccessful -> getString(
                R.string.gcash_without_refund
            )
            method is GlobePaymentMethod.ThirdPartyPaymentMethod.Adyen -> getString(R.string.credit_card_refund)
            else -> getString(R.string.no_refund)
        }

    private fun getVoucherCode(): String? =
        navigationViewModel.paymentParameters.currentTransactionsResult?.firstOrNull()?.voucherDetails?.voucherCode

    override val logTag = "PaymentSuccessfulFragment"

    override val analyticsScreenName: String = "pay.results_success"
}

const val RECEIPT = "globe_receipt"
