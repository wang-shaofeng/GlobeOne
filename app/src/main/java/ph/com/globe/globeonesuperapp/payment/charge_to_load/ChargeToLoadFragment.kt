/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.charge_to_load

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.BuildConfig
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.PaymentChargeToLoadOtpBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.payment.charge_to_load.ChargeToLoadPaymentResult.*
import ph.com.globe.globeonesuperapp.payment.payment_processing.ProcessingEntryPoint
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.inputcontroller.ChainedTextListener
import ph.com.globe.globeonesuperapp.utils.inputcontroller.EditTextChainedInputController
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.payment.PurchaseType
import ph.com.globe.model.util.brand.AccountBrand
import javax.inject.Inject

@AndroidEntryPoint
class ChargeToLoadFragment :
    NoBottomNavViewBindingFragment<PaymentChargeToLoadOtpBinding>(
        bindViewBy = { PaymentChargeToLoadOtpBinding.inflate(it) }
    ), AnalyticsScreen {
    private lateinit var digitsManager: EditTextChainedInputController

    private val appDataViewModel: AppDataViewModel by activityViewModels()

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val addAccountEnterOtpMobileNumberFragmentArgs by navArgs<ChargeToLoadFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val listOfInputFields
        get(): List<TextInputEditText> = mutableListOf(
            viewBinding.etFirstDigit,
            viewBinding.etSecondDigit,
            viewBinding.etThirdDigit,
            viewBinding.etFourthDigit,
            viewBinding.etFifthDigit,
            viewBinding.etSixthDigit
        )
    private val listOfTextInputLayouts
        get(): List<TextInputLayout> = mutableListOf(
            viewBinding.tilFirstDigitParent,
            viewBinding.tilSecondDigitParent,
            viewBinding.tilThirdDigitParent,
            viewBinding.tilFourthDigitParent,
            viewBinding.tilFifthDigitParent,
            viewBinding.tilSixthDigitParent
        )

    private val categoryIdentifier: String by lazy {
        when (navigationViewModel.paymentParameters.purchaseType) {
            is PurchaseType.BuyContent -> OTP_KEY_PROVISION_CONTENT_PROMO
            is PurchaseType.BuyPromo -> OTP_KEY_MULTIPLE_PURCHASE_PROMO
            else -> ""
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        val sentOtpArgs = addAccountEnterOtpMobileNumberFragmentArgs.sentOtpArgs
        var referenceId = sentOtpArgs.referenceId

        with(viewBinding) {

            if (sentOtpArgs.brand == AccountBrand.Hpw) {
                clBroadbandOtpCode.visibility = View.VISIBLE
                tvGoToModemInbox.setOnClickListener {
                    findNavController().safeNavigate(ChargeToLoadFragmentDirections.actionChargeToLoadFragmentToBroadbandModemInboxWebViewFragment())
                }
                tvFindUsernamePassword.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            OTP_SCREEN, CLICKABLE_TEXT, FIND_MY_USERNAME_AND_PASSWORD
                        )
                    )
                    findNavController().safeNavigate(ChargeToLoadFragmentDirections.actionChargeToLoadFragmentToFindModemUsernamePasswordFragment())
                }
            }

            val formattedNumber = sentOtpArgs.msisdn.formatPhoneNumberOtp()
            tvSentCode.text = getString(R.string.sent_code, formattedNumber)

            val allDigits = when {
                addAccountEnterOtpMobileNumberFragmentArgs.isSharing ->
                    listOfInputFields.subList(0, 4)
                else -> listOfInputFields
            }
            val digitBackgrounds = when {
                (addAccountEnterOtpMobileNumberFragmentArgs.isSharing) -> {
                    tilFifthDigitParent.visibility = View.GONE
                    tilSixthDigitParent.visibility = View.GONE
                    tvOtpCode.visibility = View.GONE
                    listOfTextInputLayouts.subList(0, 4)
                }
                else -> {
                    listOfTextInputLayouts
                }
            }

            digitsManager = EditTextChainedInputController(allDigits, false).apply {
                addOnChangedTextListener(object : ChainedTextListener {
                    override fun onChangedText(
                        editText: EditText,
                        changeTypeEvent: ChainedTextListener.ChainedTextEvent
                    ) {
                        if (changeTypeEvent == ChainedTextListener.ChainedTextEvent.CHAIN_FULL) {
                            verifyOtpViewModel.confirmOtp(
                                msisdn = sentOtpArgs.msisdn,
                                referenceId = referenceId,
                                brandType = sentOtpArgs.brandType,
                                otpCode = otpCode,
                                categoryIdentifiers = listOf(categoryIdentifier)
                            )
                        } else requireContext().hideOtpError(
                            digitBackgrounds,
                            allDigits,
                            tvOtpCodeError
                        )
                    }
                })
            }

            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnCancel.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        OTP_SCREEN, CLICKABLE_TEXT, CANCEL
                    )
                )
                findNavController().navigateUp()
            }

            with(verifyOtpViewModel) {

                // this is added for test purposes only
                // ========================================================================
                if (BuildConfig.FLAVOR_servers == "staging") {
                    tvOtpCode.visibility = View.VISIBLE
                    getOtp(
                        sentOtpArgs.msisdn,
                        sentOtpArgs.referenceId,
                        listOf(categoryIdentifier)
                    )

                    getOtpLiveData.observe(viewLifecycleOwner, {
                        tvOtpCode.visibility = View.VISIBLE
                        tvOtpCode.text = getString(R.string.otp_code_is, it)
                    })
                }
                // ========================================================================

                closeKeyboardSignal.observe(viewLifecycleOwner, {
                    closeKeyboard(view, requireContext())
                })

                resendOtpStatus.observe(viewLifecycleOwner, { isResendOtpEnabled ->
                    btnResend.isEnabled = isResendOtpEnabled
                })

                btnResend.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            OTP_SCREEN, CLICKABLE_TEXT, RESEND
                        )
                    )
                    resendOtpCode(
                        phoneNumber = sentOtpArgs.msisdn,
                        brandType = sentOtpArgs.brandType,
                        categoryIdentifiers = listOf(categoryIdentifier)
                    )
                    val customSnackbarViewBinding =
                        GlobeSnackbarLayoutBinding.inflate(LayoutInflater.from(requireContext()))
                    showSnackbar(customSnackbarViewBinding)
                }

                verifyOtpResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->

                        logUiActionEvent(
                            "Last digit", ACTION_DIGIT_INPUT, additionalParams =
                            mapOf(
                                "state" to (result is VerifyOtpViewModel.VerifyOtpResult.VerifyOtpSuccess).toString()
                            )
                        )

                        when (result) {
                            is VerifyOtpViewModel.VerifyOtpResult.VerifyOtpSuccess -> {
                                with(navigationViewModel.paymentParameters) {
                                    if (
                                        purchaseType is PurchaseType.BuyPromo &&
                                        sentOtpArgs.msisdn.convertToClassicNumberFormat() == primaryMsisdn.convertToClassicNumberFormat()
                                    ) {
                                        // Navigate to payment processing fragment with 'ChargeToLoadPurchasePromo' entry point to purchase
                                        // the promo for ourselves
                                        findNavController().safeNavigate(
                                            ChargeToLoadFragmentDirections.actionChargeToLoadFragmentToPaymentProcessingFragment(
                                                ProcessingEntryPoint.ChargeToLoadPurchasePromo(
                                                    mobileNumber = primaryMsisdn.convertToClassicNumberFormat(),
                                                    purchaseType = purchaseType,
                                                    otpReferenceId = sentOtpArgs.referenceId,
                                                    brand = sentOtpArgs.brand
                                                )
                                            )
                                        )
                                    } else if (purchaseType is PurchaseType.BuyContent) {
                                        // Navigate to payment processing fragment with 'Provision content promo' entry point
                                        findNavController().safeNavigate(
                                            ChargeToLoadFragmentDirections.actionChargeToLoadFragmentToPaymentProcessingFragment(
                                                ProcessingEntryPoint.ProvisionContentPromoWithOTP(
                                                    otpReferenceId = sentOtpArgs.referenceId
                                                )
                                            )
                                        )
                                    } else {
                                        appDataViewModel.refreshDataAfterTransaction(primaryMsisdn)
                                        // If we are buying the promo or load for others, we should navigate to successful screen.
                                        findNavController().safeNavigate(
                                            ChargeToLoadFragmentDirections.actionChargeToLoadFragmentToPaymentSuccessfulFragment(
                                                isShareFlow = true,
                                                deductedFrom = addAccountEnterOtpMobileNumberFragmentArgs.sentOtpArgs.msisdn
                                            )
                                        )
                                    }
                                }
                            }

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeIncorrect ->
                                requireContext().showOtpError(
                                    digitBackgrounds,
                                    allDigits,
                                    tvOtpCodeError,
                                    getString(R.string.incorrect_otp_code)
                                )

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeExpired ->
                                requireContext().showOtpError(
                                    digitBackgrounds,
                                    allDigits,
                                    tvOtpCodeError,
                                    getString(R.string.expired_otp_code)
                                )

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeAlreadyVerified ->
                                requireContext().showOtpError(
                                    digitBackgrounds,
                                    allDigits,
                                    tvOtpCodeError,
                                    getString(R.string.otp_already_verified)
                                )

                            else -> {
                            }
                        }
                    }
                })

                resendOtpResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->
                        if (result is VerifyOtpViewModel.ResendOtpResult.ResendOtpSuccess) {
                            referenceId = result.referenceId
                            val customSnackbarViewBinding =
                                GlobeSnackbarLayoutBinding.inflate(
                                    LayoutInflater.from(requireContext())
                                )
                            showSnackbar(customSnackbarViewBinding)

                        } else {
                            println(result)
                        }
                    }
                })
                startOtpTimer()
            }
        }
    }

    private val otpCode: String get() = digitsManager.chainedEditTextString

    override val logTag = "ChargeToLoadFragment"

    override val analyticsScreenName: String = "pay.charge_to_load"
}

const val CHARGE_TO_LOAD_EVENT = "charge_to_load"
