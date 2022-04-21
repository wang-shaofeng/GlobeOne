/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.broadband.verification

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AddAccountBroadbandEnrollMethodsFragmentBinding
import ph.com.globe.util.subStringWithChecks
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.verify_otp.VerifyOtpViewModel
import ph.com.globe.model.auth.OtpType
import ph.com.globe.model.util.brand.AccountSegment

@AndroidEntryPoint
class AddAccountBroadbandEnrollMethodsFragment :
    NoBottomNavViewBindingFragment<AddAccountBroadbandEnrollMethodsFragmentBinding>(
        bindViewBy = {
            AddAccountBroadbandEnrollMethodsFragmentBinding.inflate(it)
        }
    ) {

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModels()

    private val addAccountMethodsArgs: AddAccountBroadbandEnrollMethodsFragmentArgs by navArgs()

    private val sentToEmailString: String? by lazy {
        addAccountMethodsArgs.emailAddress?.let { emailAddress ->
            getString(
                R.string.email_address_place_holder,
                emailAddress.subStringWithChecks(
                    emailAddress.indexOf("@") - NUMBER_OF_LAST_CHARACTERS_DISPLAYED,
                    emailAddress.length
                )
            )
        }
    }
    private val sentToMobileString: String? by lazy {
        addAccountMethodsArgs.alternativeMobileNumber?.let { mobileNumber ->
            getString(
                R.string.msisdn_place_holder,
                mobileNumber.subStringWithChecks(
                    mobileNumber.length - NUMBER_OF_LAST_DIGITS_DISPLAYED,
                    mobileNumber.length
                )
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }
            ivBack.setOnClickListener {
                findNavController().navigateUp()
            }
            addAccountMethodsArgs.alternativeMobileNumber?.let { mobileNumber ->
                if (mobileNumber.isNotEmpty()) {
                    vSendSms.visibility = View.VISIBLE
                    tvPhoneNumber.text = sentToMobileString
                    clSendSms.visibility = View.VISIBLE
                    clSendSms.setOnClickListener {
                        verifyOtpViewModel.addAccountSendOtp(
                            msisdn = addAccountMethodsArgs.accountNumber ?: "",
                            rawBrand = addAccountMethodsArgs.brand,
                            segment = AccountSegment.Broadband
                        )
                    }
                }
            }
            addAccountMethodsArgs.emailAddress?.let { emailAddress ->
                if (emailAddress.isNotEmpty()) {
                    vSendEmail.visibility = View.VISIBLE
                    tvEmailAddress.text = sentToEmailString
                    clSendEmail.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            verifyOtpViewModel.addAccountSendOtp(
                                msisdn = addAccountMethodsArgs.accountNumber ?: "",
                                segment = AccountSegment.Broadband,
                                rawBrand = addAccountMethodsArgs.brand,
                                sendOtpType = OtpType.Email
                            )
                        }
                    }
                }
            }
            clSecurityQuestions.setOnClickListener {
                findNavController().safeNavigate(
                    AddAccountBroadbandEnrollMethodsFragmentDirections.actionAddAccountBroadbandEnrollMethodsFragmentToAddAccountSecurityQuestionsProcessingFragment(
                        AddAccountProcessingFragmentEntryPoint.GetSecurityQuestionsEntryPoint(
                            addAccountMethodsArgs.msisdn,
                            addAccountMethodsArgs.brand
                        )
                    )
                )
            }
            verifyOtpViewModel.sendOtpResult.observe(viewLifecycleOwner) {
                it.handleEvent { result ->
                    when (result) {
                        is VerifyOtpViewModel.SendOtpResult.SentOtpSuccess -> {
                            findNavController().safeNavigate(
                                AddAccountBroadbandEnrollMethodsFragmentDirections.actionAddAccountBroadbandEnrollMethodsFragmentToAddAccountEnterOtpFragment(
                                    msisdn = addAccountMethodsArgs.accountNumber ?: "",
                                    targetMobileNumber = (if (result.sendOtpType is OtpType.SMS) addAccountMethodsArgs.alternativeMobileNumber else null),
                                    referenceId = result.referenceId,
                                    brand = result.brand,
                                    brandType = result.brandType,
                                    segment = AccountSegment.Broadband,
                                    targetEmailAddressString = (if (result.sendOtpType is OtpType.Email) sentToEmailString else null)
                                )
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    companion object {
        const val NUMBER_OF_LAST_DIGITS_DISPLAYED = 4
        const val NUMBER_OF_LAST_CHARACTERS_DISPLAYED = 4
    }

    override val logTag = "AddAccountBroadbandEnrollMethodsFragment"
}
