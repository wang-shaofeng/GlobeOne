/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.enterotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.BuildConfig
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.confirmaccount.ConfirmAccountArgs
import ph.com.globe.globeonesuperapp.databinding.AddAccountEnterOtpFragmentBinding
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.inputcontroller.ChainedTextListener
import ph.com.globe.globeonesuperapp.utils.inputcontroller.EditTextChainedInputController
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.auth.OtpType
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.model.util.brand.AccountSegment
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountEnterOtpFragment :
    NoBottomNavViewBindingFragment<AddAccountEnterOtpFragmentBinding>(
        bindViewBy = {
            AddAccountEnterOtpFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val addAccountEnterOtpMobileNumberViewModel: AddAccountEnterOtpMobileNumberViewModel by viewModels()

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private lateinit var digitsManager: EditTextChainedInputController

    private val addAccountEnterOtpMobileNumberFragmentArgs by navArgs<AddAccountEnterOtpFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setDarkStatusBar()

        with(viewBinding) {
            // we should show the broadband modem instructions if the user is enrolling POSTPAID & BROADBAND account
            clBroadbandOtpCode.isVisible =
                addAccountEnterOtpMobileNumberFragmentArgs.segment == AccountSegment.Broadband && addAccountEnterOtpMobileNumberFragmentArgs.brandType == AccountBrandType.Prepaid

            val sentToDestination =
                if (addAccountEnterOtpMobileNumberFragmentArgs.targetEmailAddressString == null)
                    addAccountEnterOtpMobileNumberFragmentArgs.targetMobileNumber
                        ?.convertToClassicNumberFormat()
                        ?.formatPhoneNumberOtp()
                else
                    addAccountEnterOtpMobileNumberFragmentArgs.targetEmailAddressString

            tvSentCode.text = getString(R.string.sent_code, sentToDestination)

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
                                addAccountEnterOtpMobileNumberFragmentArgs.msisdn,
                                verifyOtpViewModel.referenceId,
                                addAccountEnterOtpMobileNumberFragmentArgs.brandType,
                                otpCode,
                                addAccountEnterOtpMobileNumberFragmentArgs.segment,
                                OTP_KEY_SET_ENROLL_ACCOUNT
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
                findNavController().popBackStack(R.id.addAccountNumberFragment, false)
            }

            ivClose.setOnClickListener {
                addAccountEnterOtpMobileNumberViewModel.cancelAddingAccount(
                    {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    },
                    {}
                )
            }

            btnCancel.setOnClickListener {
                addAccountEnterOtpMobileNumberViewModel.cancelAddingAccount(
                    {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    },
                    {}
                )
            }

            tvFindUsernamePassword.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        OTP_SCREEN, CLICKABLE_TEXT, FIND_MY_USERNAME_AND_PASSWORD
                    )
                )
                findNavController().safeNavigate(AddAccountEnterOtpFragmentDirections.actionAddAccountEnterOtpFragmentToAddAccountFindUsernamePasswordFragment())
            }

            tvGoToModemInbox.setOnClickListener {
                findNavController().safeNavigate(AddAccountEnterOtpFragmentDirections.actionAddAccountEnterOtpFragmentToBroadbandModemInboxWebViewFragment())
            }
            addAccountEnterOtpMobileNumberViewModel.loadAccountPlanDetailsResult.observe(
                viewLifecycleOwner
            ) {
                when (it) {
                    true -> findNavController().safeNavigate(
                        AddAccountEnterOtpFragmentDirections.actionAddAccountEnterOtpFragmentToAddAccountConfirmFragment(
                            ConfirmAccountArgs(
                                mobileNumber = if (addAccountEnterOtpMobileNumberFragmentArgs.segment == AccountSegment.Mobile) addAccountEnterOtpMobileNumberFragmentArgs.msisdn else null,
                                brand = addAccountEnterOtpMobileNumberFragmentArgs.brand,
                                brandType = addAccountEnterOtpMobileNumberFragmentArgs.brandType,
                                segment = addAccountEnterOtpMobileNumberFragmentArgs.segment,
                                referenceId = verifyOtpViewModel.referenceId,
                                isPremiumAccount = addAccountEnterOtpMobileNumberViewModel.isPremiumAccount,
                                accountStatus = addAccountEnterOtpMobileNumberViewModel.accountStatus,
                                accountNumber = addAccountEnterOtpMobileNumberViewModel.accountNumber,
                                landlineNumber = addAccountEnterOtpMobileNumberViewModel.landlineNumber,
                                accountName = addAccountEnterOtpMobileNumberViewModel.accountName
                            )
                        )
                    )
                    false -> findNavController().navigateUp()
                }
            }
            with(verifyOtpViewModel) {

                // this is added for test purposes only
                // ========================================================================
                if (BuildConfig.FLAVOR_servers == "staging") {
                    tvOtpCode.visibility = View.VISIBLE
                    getOtp(
                        addAccountEnterOtpMobileNumberFragmentArgs.msisdn,
                        referenceId,
                        OTP_KEY_SET_ENROLL_ACCOUNT
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
                        addAccountEnterOtpMobileNumberFragmentArgs.msisdn,
                        addAccountEnterOtpMobileNumberFragmentArgs.brandType,
                        addAccountEnterOtpMobileNumberFragmentArgs.segment,
                        OTP_KEY_SET_ENROLL_ACCOUNT,
                        if (addAccountEnterOtpMobileNumberFragmentArgs.targetEmailAddressString == null) OtpType.SMS else OtpType.Email
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
                        logCustomEvent(
                            analyticsEventsProvider.provideCustomGAEvent(
                                GAEventCategory.Registration,
                                VERIFY_OTP,
                                encryptedUserEmail,
                                encryptedMsisdn,
                                addAccountEnterOtpMobileNumberFragmentArgs.brandType.toString(),
                                result.cxsMessageId
                            )
                        )

                        when (result) {
                            is VerifyOtpViewModel.VerifyOtpResult.VerifyOtpSuccess -> {
                                if (addAccountEnterOtpMobileNumberFragmentArgs.brandType == AccountBrandType.Postpaid) {
                                    addAccountEnterOtpMobileNumberViewModel.loadAccountAndPlanDetails(
                                        msisdn = addAccountEnterOtpMobileNumberFragmentArgs.msisdn,
                                        segment = addAccountEnterOtpMobileNumberFragmentArgs.segment,
                                        referenceId = referenceId
                                    )
                                } else {
                                    findNavController().safeNavigate(
                                        AddAccountEnterOtpFragmentDirections.actionAddAccountEnterOtpFragmentToAddAccountConfirmFragment(
                                            ConfirmAccountArgs(
                                                mobileNumber = addAccountEnterOtpMobileNumberFragmentArgs.msisdn,
                                                brand = addAccountEnterOtpMobileNumberFragmentArgs.brand,
                                                brandType = addAccountEnterOtpMobileNumberFragmentArgs.brandType,
                                                segment = addAccountEnterOtpMobileNumberFragmentArgs.segment,
                                                referenceId = referenceId
                                            )
                                        )
                                    )
                                }
                            }

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeIncorrect ->
                                requireContext().showOtpError(
                                    digitBackgrounds,
                                    allDigits,
                                    tvOtpCodeError,
                                    getString(R.string.incorrect_otp_code)
                                )

                            is VerifyOtpViewModel.VerifyOtpResult.OtpCodeExpired -> {
                                requireContext().showOtpError(
                                    digitBackgrounds,
                                    allDigits,
                                    tvOtpCodeError,
                                    getString(R.string.expired_otp_code)
                                )
                                btnResend.isEnabled = true
                            }

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
                        when (result) {
                            is VerifyOtpViewModel.ResendOtpResult.ResendOtpSuccess -> {
                                val customSnackbarViewBinding =
                                    GlobeSnackbarLayoutBinding.inflate(
                                        LayoutInflater.from(requireContext())
                                    )
                                showSnackbar(customSnackbarViewBinding)

                            }
                            is VerifyOtpViewModel.ResendOtpResult.MaxOtpResendReached -> {
                                maxOtpAttemptsReached {
                                    findNavController().navigateUp()
                                }
                            }
                        }
                    }
                })

                startOtpTimer()
            }
        }
    }

    private val otpCode: String get() = digitsManager.chainedEditTextString

    override val logTag = "AddAccountEnterOtpMobileNumberFragment"

    override val analyticsScreenName: String = "enrollment.input_OTP"
}
