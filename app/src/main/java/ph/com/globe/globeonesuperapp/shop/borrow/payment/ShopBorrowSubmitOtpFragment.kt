/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.borrow.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.ACTION_DIGIT_INPUT
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BuildConfig
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.ShopBorrowSubmitOtpFragmentBinding
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.ShopItemDetailsViewModel
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.inputcontroller.ChainedTextListener
import ph.com.globe.globeonesuperapp.utils.inputcontroller.EditTextChainedInputController
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ShopBorrowSubmitOtpFragment :
    NoBottomNavViewBindingFragment<ShopBorrowSubmitOtpFragmentBinding>(
        bindViewBy = {
            ShopBorrowSubmitOtpFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    private val shopItemDetailsViewModel: ShopItemDetailsViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }
    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val shopBorrowSubmitOtpFragmentArgs by navArgs<ShopBorrowSubmitOtpFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    private lateinit var digitsManager: EditTextChainedInputController

    private lateinit var referenceId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()

        with(viewBinding) {
            referenceId = shopBorrowSubmitOtpFragmentArgs.referenceId
            val formattedNumber = shopBorrowSubmitOtpFragmentArgs.phoneNumber.formatPhoneNumber()
            tvSentCode.text = getString(R.string.sent_code, formattedNumber)

            val allDigits = listOf(
                etFirstDigit, etSecondDigit, etThirdDigit,
                etFourthDigit, etFifthDigit, etSixthDigit
            )

            val digitBackgrounds = listOf(
                tilFirstDigitParent, tilSecondDigitParent, tilThirdDigitParent,
                tilFourthDigitParent, tilFifthDigitParent, tilSixthDigitParent
            )

            digitsManager = EditTextChainedInputController(allDigits, false).apply {
                addOnChangedTextListener(object : ChainedTextListener {
                    override fun onChangedText(
                        editText: EditText,
                        changeTypeEvent: ChainedTextListener.ChainedTextEvent
                    ) {
                        if (!etFirstDigit.text.isNullOrEmpty() && !etSecondDigit.text.isNullOrEmpty()
                            && !etThirdDigit.text.isNullOrEmpty() && !etFourthDigit.text.isNullOrEmpty()
                            && !etFifthDigit.text.isNullOrEmpty() && !etSixthDigit.text.isNullOrEmpty()
                        ) {
                            verifyOtpViewModel.confirmOtp(
                                msisdn = shopBorrowSubmitOtpFragmentArgs.phoneNumber,
                                referenceId = referenceId,
                                brandType = shopBorrowSubmitOtpFragmentArgs.brandType,
                                otpCode = otpCode,
                                categoryIdentifiers = OTP_KEY_SET_BORROW
                            )
                        } else requireContext().hideOtpError(
                            digitBackgrounds,
                            allDigits,
                            tvOtpCodeError
                        )
                    }
                })
            }

            ivClose.setOnClickListener {
                shopItemDetailsViewModel.cancelAddingAccount(
                    {
                        findNavController().popBackStack(R.id.shopItemDetailsFragment, false)
                    },
                    {}
                )
            }

            btnCancel.setOnClickListener {
                shopItemDetailsViewModel.cancelAddingAccount(
                    {
                        findNavController().popBackStack(R.id.shopItemDetailsFragment, false)
                    },
                    {}
                )
            }

            with(verifyOtpViewModel) {

                // this is added for test purposes only
                // ========================================================================
                if (BuildConfig.FLAVOR_servers == "staging") {
                    tvOtpCode.visibility = View.VISIBLE
                    getOtp(
                        shopBorrowSubmitOtpFragmentArgs.phoneNumber,
                        shopBorrowSubmitOtpFragmentArgs.referenceId,
                        OTP_KEY_SET_BORROW
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
                        phoneNumber = shopBorrowSubmitOtpFragmentArgs.phoneNumber,
                        brandType = shopBorrowSubmitOtpFragmentArgs.brandType,
                        categoryIdentifiers = OTP_KEY_SET_BORROW
                    )
                    val customSnackbarViewBinding =
                        GlobeSnackbarLayoutBinding.inflate(LayoutInflater.from(requireContext()))
                    showSnackbar(customSnackbarViewBinding)
                }

                verifyOtpResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->

                        logUiActionEvent(
                            target = "Last digit",
                            action = ACTION_DIGIT_INPUT,
                            additionalParams = mapOf(
                                "state" to (result is VerifyOtpViewModel.VerifyOtpResult.VerifyOtpSuccess).toString()
                            )
                        )

                        when (result) {
                            is VerifyOtpViewModel.VerifyOtpResult.VerifyOtpSuccess ->
                                findNavController().safeNavigate(
                                    ShopBorrowSubmitOtpFragmentDirections.actionShopBorrowSubmitOtpFragmentToShopBorrowProcessingFragment(
                                        referenceId
                                    )
                                )

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

    override val logTag = "ShopBorrowSubmitOtpFragment"

    override val analyticsScreenName: String = "enrollment.input_OTP"
}
