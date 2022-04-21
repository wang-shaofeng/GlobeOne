/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.customer_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.ACTION_DIGIT_INPUT
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BuildConfig
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.CustomerDetailsOtpFragmentBinding
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.inputcontroller.ChainedTextListener
import ph.com.globe.globeonesuperapp.utils.inputcontroller.EditTextChainedInputController
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.payment.GlobePaymentMethod
import ph.com.globe.model.payment.PurchaseType
import ph.com.globe.model.profile.response_models.GetCustomerDetailsParams
import javax.inject.Inject

@AndroidEntryPoint
class CustomerDetailsOtpFragment :
    NoBottomNavViewBindingFragment<CustomerDetailsOtpFragmentBinding>(
        bindViewBy = { CustomerDetailsOtpFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    private lateinit var digitsManager: EditTextChainedInputController

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val customerDetailsViewModel: CustomerDetailsViewModel by viewModels()

    private val customerDetailsOtpArgs: CustomerDetailsOtpFragmentArgs by navArgs()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    private val listOfInputFields
        get(): List<TextInputEditText> = listOf(
            viewBinding.etFirstDigit,
            viewBinding.etSecondDigit,
            viewBinding.etThirdDigit,
            viewBinding.etFourthDigit,
            viewBinding.etFifthDigit,
            viewBinding.etSixthDigit
        )
    private val listOfTextInputLayouts
        get(): List<TextInputLayout> = listOf(
            viewBinding.tilFirstDigitParent,
            viewBinding.tilSecondDigitParent,
            viewBinding.tilThirdDigitParent,
            viewBinding.tilFourthDigitParent,
            viewBinding.tilFifthDigitParent,
            viewBinding.tilSixthDigitParent
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        val sentOtpArgs = customerDetailsOtpArgs.sentOtpArgs
        var referenceId = sentOtpArgs.referenceId

        with(viewBinding) {

            val formattedNumber = sentOtpArgs.msisdn.formatPhoneNumber()
            tvSentCode.text = getString(R.string.sent_code, formattedNumber)

            digitsManager = EditTextChainedInputController(listOfInputFields, false).apply {
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
                                categoryIdentifiers = listOf(OTP_KEY_GET_CUSTOMER_DETAILS)
                            )
                        } else requireContext().hideOtpError(
                            listOfTextInputLayouts,
                            listOfInputFields,
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
                        listOf(OTP_KEY_GET_CUSTOMER_DETAILS)
                    )

                    getOtpLiveData.observe(viewLifecycleOwner, {
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
                    logUiActionEvent("Resend code")
                    resendOtpCode(
                        phoneNumber = sentOtpArgs.msisdn,
                        brandType = sentOtpArgs.brandType,
                        categoryIdentifiers = listOf(OTP_KEY_GET_CUSTOMER_DETAILS)
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
                                customerDetailsViewModel.getCustomerDetails(
                                    GetCustomerDetailsParams(
                                        sentOtpArgs.msisdn.convertToClassicNumberFormat(),
                                        sentOtpArgs.referenceId
                                    )
                                )
                            }

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeIncorrect ->
                                requireContext().showOtpError(
                                    listOfTextInputLayouts,
                                    listOfInputFields,
                                    tvOtpCodeError,
                                    getString(R.string.incorrect_otp_code)
                                )

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeExpired ->
                                requireContext().showOtpError(
                                    listOfTextInputLayouts,
                                    listOfInputFields,
                                    tvOtpCodeError,
                                    getString(R.string.expired_otp_code)
                                )

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeAlreadyVerified ->
                                requireContext().showOtpError(
                                    listOfTextInputLayouts,
                                    listOfInputFields,
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

        customerDetailsViewModel.customerDetailsResult.observe(viewLifecycleOwner, {
            it.handleEvent { customerDetails ->
                navigationViewModel.paymentParameters.purchaseType.let { type ->
                    (type as? PurchaseType.BuyContentVoucher)?.customerDetails = customerDetails
                }
                findNavController().safeNavigate(
                    CustomerDetailsOtpFragmentDirections.actionCustomerDetailsOtpFragmentToPaymentLoadingSessionFragment(
                        GlobePaymentMethod.ThirdPartyPaymentMethod.GCash
                    )
                )
            }
        })
    }

    private val otpCode: String get() = digitsManager.chainedEditTextString

    override val logTag = "CustomerDetailsOtpFragment"

    override val analyticsScreenName: String = "pay.customer_details"
}
