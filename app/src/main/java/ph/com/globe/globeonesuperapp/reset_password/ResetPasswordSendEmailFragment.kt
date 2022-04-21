/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.reset_password

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.ResetPasswordSendEmailFragmentBinding
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.isOnBackStack
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class ResetPasswordSendEmailFragment :
    NoBottomNavViewBindingFragment<ResetPasswordSendEmailFragmentBinding>(bindViewBy = {
        ResetPasswordSendEmailFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val viewModel: ResetPasswordViewModel by viewModels()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:forgot password screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {

            if (!arguments?.getString(EMAIL_ADDRESS).isNullOrEmpty()) {
                etEmailAddress.setText(requireArguments()[EMAIL_ADDRESS].toString())
                requireArguments().clear()
                isSendEnable(true)
            } else {
                isSendEnable(false)
            }

            btnSend.setOnClickListener {
                viewModel.sendPasswordResetEmail(viewBinding.etEmailAddress.text.toString())
            }

            etEmailAddress.addTextChangedListener {
                isSendEnable(it.isNullOrEmpty().not())

                requireContext().hideError(
                    tilEmailSent,
                    etEmailAddress
                )
                tilEmailSent.editText?.setOutlinedBackground()
            }

            clCloseWrapper.setOnClickListener {
                if (findNavController().isOnBackStack(R.id.loginFragment))
                    findNavController().popBackStack(R.id.loginFragment, false)
                else
                    findNavController().popBackStack(R.id.selectSignMethodFragment, false)
            }

            viewModel.resetPasswordResult.observe(viewLifecycleOwner, {
                it.handleEvent { value ->
                    when (value) {

                        is ResetPasswordViewModel.ResetPasswordResult.EmailFormatIsIncorrect -> {
                            requireContext().showError(
                                tilEmailSent,
                                etEmailAddress,
                                getString(R.string.error_invalid_email_format),
                                textColor = R.color.functional_primary
                            )
                            etEmailAddress.setOutlinedErrorBackground()
                        }

                        is ResetPasswordViewModel.ResetPasswordResult.NoUserWithThisEmailAddress -> {
                            requireContext().showError(
                                tilEmailSent,
                                etEmailAddress,
                                getString(R.string.error_no_account_with_this_email),
                                textColor = R.color.functional_primary
                            )
                            etEmailAddress.setOutlinedErrorBackground()
                        }

                        is ResetPasswordViewModel.ResetPasswordResult.EmailSentSuccessfully -> {
                            logUiActionEvent("Send")
                            findNavController().safeNavigate(
                                R.id.action_resetPasswordSendEmailFragment_to_resetPasswordEmailSentFragment,
                                bundleOf(EMAIL_ADDRESS to viewBinding.etEmailAddress.text.toString())
                            )
                        }

                        is ResetPasswordViewModel.ResetPasswordResult.EmailIsForSocialLogin -> {
                            requireContext().showError(
                                tilEmailSent,
                                etEmailAddress,
                                getString(R.string.error_email_is_for_socialLogin),
                                textColor = R.color.functional_primary
                            )
                            etEmailAddress.setOutlinedErrorBackground()
                        }
                    }
                }
            })
        }
    }

    private fun isSendEnable(isEnable: Boolean) {
        viewBinding.btnSend.isEnabled = isEnable
    }

    override val logTag = "ResetPasswordSendEmailFragment"

    override val analyticsScreenName: String = "enrollment.forgot_password"
}

const val EMAIL_ADDRESS = "emailAddress"
