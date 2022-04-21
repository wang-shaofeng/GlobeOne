/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.email_verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.AUTH_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.EmailVerificationFragmentBinding
import ph.com.globe.globeonesuperapp.databinding.GlobeSnackbarLayoutBinding
import ph.com.globe.globeonesuperapp.utils.eventWithResultObserve
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.showSnackbar
import ph.com.globe.globeonesuperapp.utils.ui.DeepLinkAction
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class EmailVerificationFragment : NoBottomNavViewBindingFragment<EmailVerificationFragmentBinding>({
    EmailVerificationFragmentBinding.inflate(it)
}) {

    private val emailVerificationViewModel: EmailVerificationViewModel by viewModels()
    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val checkDeepLink = arguments?.getBoolean(WAIT_FOR_DEEP_LINK, false) ?: false

        emailVerificationViewModel.waitForDeepLink(checkDeepLink)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            emailVerificationViewModel.forceLogout()
        }

        with(viewBinding) {
            btnCancel.setOnClickListener {
                emailVerificationViewModel.forceLogout()
            }

            with(emailVerificationViewModel) {
                canResend.observe(viewLifecycleOwner) {
                    btnTryAgain.isEnabled = it
                }

                timer.observe(viewLifecycleOwner) {
                    if (it.secondsRemaining == 0L) {
                        btnTryAgain.text = getString(R.string.resend_link)
                    } else {
                        val min = it.secondsRemaining / 60
                        val sec = it.secondsRemaining % 60
                        btnTryAgain.text = getString(
                            R.string.resend_in,
                            String.format("%d", min),
                            String.format("%02d", sec)
                        )
                    }
                }

                verificationEmailIsSent.observe(viewLifecycleOwner) {
                    it.handleEvent {
                        if (it) createAndShowSuccessSnackbar()
                    }
                }

                email.observe(viewLifecycleOwner) {
                    etEmailAddress.setText(it)
                }

                isVerificationSuccess.observe(viewLifecycleOwner) {
                    it.handleEvent {
                        findNavController().safeNavigate(
                            EmailVerificationFragmentDirections.actionEmailVerificationFragmentToEmailVerificationStatusFragment(
                                it
                            )
                        )
                    }
                }

                emailIsAlreadyVerified.observe(viewLifecycleOwner) {
                    it.handleEvent {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            AUTH_KEY,
                            R.id.loginFragment
                        )
                    }
                }
            }

            btnTryAgain.setOnClickListener {
                emailVerificationViewModel.tryToSendEmail(true)
            }

            tvLogout.setOnClickListener { emailVerificationViewModel.forceLogout() }

            generalEventsViewModel.handleDeepLink.eventWithResultObserve(viewLifecycleOwner) {
                if (it.deepLinkAction is DeepLinkAction.EmailVerification) {
                    emailVerificationViewModel.verifyEmail(it.deepLinkAction.verificationCode)
                    true
                } else
                    false
            }
        }
    }

    fun createAndShowSuccessSnackbar() {
        val emailSentSuccessfullySnackbar =
            GlobeSnackbarLayoutBinding.inflate(LayoutInflater.from(requireContext())).apply {
                tvGlobeSnackbarTitle.text =
                    getString(R.string.verification_email_sent)
                tvGlobeSnackbarDescription.text =
                    getString(R.string.verification_email_sent_description)
            }
        showSnackbar(emailSentSuccessfullySnackbar)
    }

    override val logTag: String = "EmailVerificationFragment"

    companion object {
        const val WAIT_FOR_DEEP_LINK = "wait_for_deep_link"
    }
}
