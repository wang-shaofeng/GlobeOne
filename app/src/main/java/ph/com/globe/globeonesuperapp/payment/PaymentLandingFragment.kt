/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
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
import ph.com.globe.analytics.logger.eLog
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentLandingFragmentBinding
import ph.com.globe.globeonesuperapp.payment.charge_to_load.ChargeToLoadPaymentResult
import ph.com.globe.globeonesuperapp.payment.charge_to_load.ChargeToLoadViewModel
import ph.com.globe.globeonesuperapp.payment.customer_details.CustomerDetailsViewModel
import ph.com.globe.globeonesuperapp.payment.payment_processing.ProcessingEntryPoint
import ph.com.globe.globeonesuperapp.shop.select_other_account.BUYING_LOAD_ON_CHARGE_AMOUNT_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.BUYING_LOAD_ON_CHARGE_TO_LOAD_NUMBER_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.LOGGED_IN_STATUS_KEY
import ph.com.globe.globeonesuperapp.shop.select_other_account.TITLE_KEY
import ph.com.globe.globeonesuperapp.shop.util.ContactsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayPrice
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.payment.floatToPesos
import ph.com.globe.globeonesuperapp.utils.payment.intToPesos
import ph.com.globe.globeonesuperapp.utils.payment.toPesosWithDecimal
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel.SendOtpResult.SentOtpSuccess
import ph.com.globe.model.payment.GlobePaymentMethod
import ph.com.globe.model.payment.PurchaseType.*
import ph.com.globe.model.payment.checkIfAllFailed
import ph.com.globe.model.profile.response_models.GetCustomerDetailsParams
import javax.inject.Inject

@AndroidEntryPoint
class PaymentLandingFragment :
    NoBottomNavViewBindingFragment<PaymentLandingFragmentBinding>(
        bindViewBy = { PaymentLandingFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val contactsViewModel: ContactsViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val customerDetailsViewModel: CustomerDetailsViewModel by viewModels()

    private val paymentParams: PaymentLandingFragmentArgs by navArgs()

    private val chargeToLoadViewModel: ChargeToLoadViewModel by viewModels()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:payment options screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLightStatusBar()

        with(viewBinding) {
            with(navigationViewModel) {
                // HERE we will receive the details about the purchase from promo or load fragments
                initPaymentWithParameters(paymentParams.paymentParameter)
                initiatePaymentLandingScreen()
                ivClose.setOnClickListener {
                    logUiActionEvent(X)
                    findNavController().navigateUp()
                }
                ivBack.setOnClickListener {
                    logUiActionEvent(BACK_BUTTON)
                    findNavController().navigateUp()
                }
                with(paymentParameters) {
                    clGCash.setOnClickListener {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Conversion,
                                CONVERSION_SCREEN, BUTTON, GCASH
                            )
                        )
                        if (purchaseType is BuyContent) {
                            if (!isLoggedIn || !isEnrolledAccount) {
                                verifyOtpViewModel.sendOtp(
                                    primaryMsisdn, listOf(
                                        OTP_KEY_GET_CUSTOMER_DETAILS
                                    )
                                )
                            } else {
                                customerDetailsViewModel.getCustomerDetails(
                                    GetCustomerDetailsParams(primaryMsisdn)
                                )
                            }
                        } else {
                            findNavController().safeNavigate(
                                PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentLoadingSessionFragment(
                                    GlobePaymentMethod.ThirdPartyPaymentMethod.GCash
                                )
                            )
                        }
                    }
                    clCreditCard.setOnClickListener {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Conversion,
                                CONVERSION_SCREEN, BUTTON, CREDIT_CARD
                            )
                        )
                        findNavController().safeNavigate(
                            PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentLoadingSessionFragment(
                                GlobePaymentMethod.ThirdPartyPaymentMethod.Adyen
                            )
                        )
                    }
                    clChargeToLoad.setOnClickListener {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Conversion,
                                CONVERSION_SCREEN, BUTTON, CHARGE_TO_LOAD,
                                productName = paymentName
                            )
                        )
                        when {
                            (shareablePromo && paymentParameters.selectedBoosters?.isNotEmpty() == false) || purchaseType is BuyLoad -> {
                                // If the promo is shareable and doesn't have boosters selected or we are buying the load, we need to choose the account we want to charge
                                goToChoseAccount()
                            }
                            purchaseType is BuyPromo -> {
                                chargeToLoadViewModel.checkPrepaidBalanceSufficiency(
                                    primaryMsisdn,
                                    totalAmount.toString(),
                                    isExclusivePromo
                                )
                            }
                            purchaseType is BuyContent -> {
                                // Navigate to payment processing fragment with 'CheckBalanceSufficiency' entry point
                                findNavController().safeNavigate(
                                    PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentProcessingFragment(
                                        ProcessingEntryPoint.CheckBalanceSufficiency
                                    )
                                )
                            }
                            purchaseType is BuyGoCreatePromo -> {
                                // Navigate to payment processing fragment with 'ChargeToLoadPurchasePromo' entry point
                                findNavController().safeNavigate(
                                    PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentProcessingFragment(
                                        ProcessingEntryPoint.ChargeToLoadPurchasePromo(
                                            mobileNumber = primaryMsisdn,
                                            purchaseType = purchaseType
                                        )
                                    )
                                )
                            } // We are expecting more purchase types to come to the flow
                        }
                    }
                    landingScreenSetup.observe(viewLifecycleOwner, {
                        when (it) {
                            is BuyLoad -> {
                                tvToolbarTitle.setText(R.string.shop_tab_load)
                                tvPaymentLeadTitle.setText(R.string.you_are_buying)
                                tvPaymentLeadValue.setText(R.string.shop_tab_load)
                                if (currentAmount != null && currentAmount > 0f) {
                                    tvPaymentLeadPrice.isVisible = false

                                    tvPaymentCurrentPrice.isVisible = true
                                    tvPaymentCurrentPrice.text = getString(
                                        R.string.pezos_prefix,
                                        currentAmount.toFormattedDisplayPrice()
                                    )

                                    tvPaymentLeadPriceOld.isVisible = true
                                    tvPaymentLeadPriceOld.text = totalAmount.toInt().intToPesos()
                                    tvPaymentLeadPriceOld.paintFlags =
                                        tvPaymentLeadPriceOld.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                                    tvGCashInfo.isVisible = true
                                    tvCreditCardInfo.isVisible = true
                                } else {
                                    tvPaymentLeadPrice.text = totalAmount.toInt().intToPesos()
                                }
                            }
                            is BuyPromo -> {
                                if (paymentParameters.selectedBoosters?.isNotEmpty() == true) {
                                    tvPaymentLeadValue.text =
                                        getString(R.string.with_booster, paymentName)
                                } else {
                                    tvPaymentLeadValue.text = paymentName
                                }
                                setupPrice(totalAmount, discount)
                            }
                            is BuyContent -> {
                                tvPaymentLeadValue.text = paymentName
                                setupPrice(totalAmount, discount)
                            }
                            is BuyGoCreatePromo -> {
                                tvPaymentLeadValue.text = paymentName
                                setupPrice(totalAmount, discount)
                            }
                            is PayBill -> {
                                tvPaymentLeadValue.text = paymentName
                                // we are showing decimal points only when paying a bill
                                tvPaymentLeadPrice.text = totalAmount.toPesosWithDecimal()
                            }
                            else -> Unit
                        }
                        if (!paymentParameters.canDisplayGCash()) {
                            vGCashDash.visibility = View.GONE
                            clGCash.visibility = View.GONE
                        }
                        if (!paymentParameters.canDisplayCreditDebitCard()) {
                            vGCashDash.visibility = View.GONE
                            vCreditCardDash.visibility = View.GONE
                            clCreditCard.visibility = View.GONE
                        }
                        if (!paymentParameters.canDisplayChargeToLoad()) {
                            clChargeToLoad.visibility = View.GONE
                            vCreditCardDash.visibility = View.GONE
                        } else {
                            viewBinding.tvChargeToLoadInfo.apply {
                                text = if (paymentParameters.canOnlyChargeToOwnLoad()) {
                                    getString(
                                        R.string.will_be_charged_to,
                                        primaryMsisdn.convertToPrefixNumberFormat()
                                            .formatPhoneNumber()
                                    )
                                } else {
                                    getString(R.string.ask_to_charge_to_another_numbers_load)
                                }
                            }
                        }
                        displayColor?.let { color ->
                            try {
                                clPaymentLeadInfo.setBackgroundColor(Color.parseColor(color))
                            } catch (e: Exception) {
                                clPaymentLeadInfo.setBackgroundColor(Color.parseColor("#000000"))
                                eLog(Exception("displayColor has a bad format: $color"))
                            }
                        }
                    })
                }
            }
        }
        contactsViewModel.numberSelectedOneTimeEvent.observe(viewLifecycleOwner, {
            it.handleEvent {
                if (
                    navigationViewModel.paymentParameters.purchaseType is BuyPromo &&
                    contactsViewModel.selectedNumber.value?.convertToClassicNumberFormat() == navigationViewModel.paymentParameters.primaryMsisdn.convertToClassicNumberFormat()
                ) {
                    chargeToLoadViewModel.checkPrepaidBalanceSufficiency(
                        contactsViewModel.selectedNumber.value ?: "",
                        navigationViewModel.paymentParameters.totalAmount.toString()
                    )
                } else {
                    chargeToLoadViewModel.tryPurchaseViaShareFlow(
                        sourceNumber = contactsViewModel.selectedNumber.value?.convertToClassicNumberFormat()
                            ?: "",
                        targetNumber = navigationViewModel.paymentParameters.primaryMsisdn.convertToClassicNumberFormat(),
                        purchaseType = navigationViewModel.paymentParameters.purchaseType,
                        otpReferenceId = ""
                    )
                }
            }
        })
        chargeToLoadViewModel.paymentResult.observe(viewLifecycleOwner, {
            it.handleEvent { result ->
                when (result) {
                    is ChargeToLoadPaymentResult.ShareLoadPromoOTPSent -> {
                        navigationViewModel.paymentParameters.setCurrentTransactionsResult(
                            referenceId = "GLAS${result.referenceId}",
                            paymentMethod = GlobePaymentMethod.ChargeToLoad
                        )
                        contactsViewModel.lastCheckedNumberValidation.value?.brand?.let { accountBrand ->
                            findNavController().safeNavigate(
                                PaymentLandingFragmentDirections.actionPaymentLandingFragmentToChargeToLoadFragment(
                                    SentOtpSuccess(
                                        msisdn = contactsViewModel.selectedNumber.value ?: "",
                                        brand = accountBrand,
                                        brandType = accountBrand.brandType,
                                        referenceId = result.referenceId,
                                        false
                                    ), true
                                )
                            )
                        }
                    }
                    is ChargeToLoadPaymentResult.SufficientBalanceForExclusive -> {
                        chargeToLoadViewModel.purchaseExclusivePromo(navigationViewModel.paymentParameters)
                    }
                    is ChargeToLoadPaymentResult.SufficientBalance -> {
                        with(navigationViewModel.paymentParameters) {
                            if (isEnrolledAccount) {
                                findNavController().safeNavigate(
                                    PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentProcessingFragment(
                                        ProcessingEntryPoint.ChargeToLoadPurchasePromo(
                                            mobileNumber = primaryMsisdn,
                                            purchaseType = purchaseType,
                                            brand = brand
                                        )
                                    )
                                )
                            } else {
                                verifyOtpViewModel.sendOtp(
                                    primaryMsisdn, listOf(
                                        OTP_KEY_MULTIPLE_PURCHASE_PROMO
                                    )
                                )
                            }
                        }
                    }
                    is ChargeToLoadPaymentResult.NotEnoughBalance -> {
                        when (navigationViewModel.paymentParameters.purchaseType) {
                            is BuyPromo, is BuyGoCreatePromo -> {
                                navigateOnPaymentFailed(
                                    getString(R.string.insufficient_funds_buy_promo),
                                    getString(R.string.insufficient_funds_buy_promo_info)
                                )
                            }
                            is BuyContent -> {
                                navigateOnPaymentFailed(
                                    getString(R.string.insufficient_funds_buy_content),
                                    getString(R.string.insufficient_funds_buy_content_info)
                                )
                            }
                            else -> {
                                navigateOnPaymentFailed(
                                    getString(R.string.insufficient_funds),
                                    getString(R.string.insufficient_funds_info)
                                )
                            }
                        }
                    }
                    is ChargeToLoadPaymentResult.GenericPaymentError -> {
                        navigateOnPaymentFailed()
                    }
                    is ChargeToLoadPaymentResult.PurchaseSuccessful -> {
                        navigationViewModel.paymentParameters.setCurrentTransactionsResult(
                            transactions = result.result.promos,
                            referenceId = result.result.promos[0].transactionId
                                .replace("superapp_", "GLAC", ignoreCase = true),
                            paymentMethod = GlobePaymentMethod.ChargeToLoad
                        )
                        if (navigationViewModel.paymentParameters.currentTransactionsResult.checkIfAllFailed()) {
                            findNavController().safeNavigate(
                                PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentFailedFragment(
                                    getString(R.string.payment_sorry_something_went_wrong)
                                )
                            )
                        } else {
                            findNavController().safeNavigate(
                                PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentSuccessfulFragment()
                            )
                        }
                    }
                    is ChargeToLoadPaymentResult.PurchaseFailed -> findNavController().safeNavigate(
                        PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentFailedFragment(
                            getString(R.string.your_payment_declined)
                        )
                    )
                }
            }
        })
        verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner, {
            it.handleEvent { result ->
                when (result) {
                    is SentOtpSuccess -> {
                        when (navigationViewModel.paymentParameters.purchaseType) {
                            is BuyContentVoucher -> {
                                findNavController().safeNavigate(
                                    PaymentLandingFragmentDirections.actionPaymentLandingFragmentToCustomerDetailsOtpFragment(
                                        result
                                    )
                                )
                            }
                            else -> {
                                findNavController().safeNavigate(
                                    PaymentLandingFragmentDirections.actionPaymentLandingFragmentToChargeToLoadFragment(
                                        result
                                    )
                                )
                            }
                        }
                    }
                    else -> {
                        navigateOnPaymentFailed()
                    }
                }
            }
        })
        customerDetailsViewModel.customerDetailsResult.observe(viewLifecycleOwner, {
            it.handleEvent { customerDetails ->
                navigationViewModel.paymentParameters.purchaseType.let { type ->
                    (type as? BuyContentVoucher)?.customerDetails = customerDetails
                }
                findNavController().safeNavigate(
                    PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentLoadingSessionFragment(
                        GlobePaymentMethod.ThirdPartyPaymentMethod.GCash
                    )
                )
            }
        })
    }

    private fun setupPrice(totalAmount: Double, discount: Double = 0.0) = with(viewBinding) {
        tvPaymentLeadPrice.text = totalAmount.toInt().intToPesos()
        tvPaymentLeadPriceOld.apply {
            text = (totalAmount + discount).toInt().intToPesos()
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            isVisible = discount != 0.0
        }
    }

    private fun goToChoseAccount() {
        findNavController().safeNavigate(
            R.id.action_paymentLandingFragment_to_selectOtherAccountFragmentPayment,
            bundleOf(
                LOGGED_IN_STATUS_KEY to navigationViewModel.paymentParameters.isLoggedIn,
                CURRENT_NAV_GRAPH to R.id.payment_subgraph.toString(),
                TITLE_KEY to getString(R.string.charge_to_load),
                BUYING_LOAD_ON_CHARGE_TO_LOAD_NUMBER_KEY to if (navigationViewModel.paymentParameters.purchaseType is BuyLoad) navigationViewModel.paymentParameters.primaryMsisdn else null,
                BUYING_LOAD_ON_CHARGE_AMOUNT_KEY to if (navigationViewModel.paymentParameters.purchaseType is BuyLoad) navigationViewModel.paymentParameters.totalAmount.toFloat() else null
            )
        )
    }

    private fun navigateOnPaymentFailed(message: String? = null, messageInfo: String? = null) {
        findNavController().safeNavigate(
            PaymentLandingFragmentDirections.actionPaymentLandingFragmentToPaymentFailedFragment(
                message = (message ?: getString(R.string.your_payment_declined)),
                messageInfo = (messageInfo ?: getString(R.string.please_try_again))
            )
        )
    }

    override val logTag = "PaymentLandingFragment"

    override val analyticsScreenName: String = "pay.main_page"
}

const val CURRENT_NAV_GRAPH = "CurrentNavGraph"
