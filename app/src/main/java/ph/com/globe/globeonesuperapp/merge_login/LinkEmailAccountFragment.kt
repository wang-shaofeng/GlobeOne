/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.merge_login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.events.custom.LoginWithEmail
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.LinkEmailAccountFragmentBinding
import ph.com.globe.globeonesuperapp.login.LoginViewModel
import ph.com.globe.globeonesuperapp.reset_password.EMAIL_ADDRESS
import ph.com.globe.globeonesuperapp.termsandprivacypolicy.TERMS_AND_CONDITIONS_URL
import ph.com.globe.globeonesuperapp.utils.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class LinkEmailAccountFragment :
    NoBottomNavViewBindingFragment<LinkEmailAccountFragmentBinding>(bindViewBy = {
        LinkEmailAccountFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val viewModel: LoginViewModel by viewModels()

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()
    private val appDataViewModel: AppDataViewModel by activityViewModels()
    private val args: LinkEmailAccountFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:link email account screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        with(viewBinding) {

            clBackWrapper.setOnClickListener {
                findNavController().navigateUp()
            }

            tvGetStarted.setOnClickListener {
                findNavController().navigateUp()
            }

            tvPrivacyPolicyLink.movementMethod = LinkMovementMethod.getInstance()
            tvPrivacyPolicyLink.text = buildSpannedString {
                append(getString(R.string.privacy_policy_info_login))
                append("  ")
                bold {
                    color(
                        AppCompatResources.getColorStateList(
                            requireContext(),
                            R.color.primary
                        ).defaultColor
                    ) {
                        onClick({
                            generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_AND_CONDITIONS_URL))
                                startActivity(intent)
                            }
                        }) {
                            append(getString(R.string.privacy_policy_link))
                        }
                    }
                }
            }

            btnForgotPassword.visibility = View.GONE

            btnForgotPassword.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Acquisition(ACQUISITION_LOGIN),
                        LINK_EMAIL_SCREEN, CLICKABLE_TEXT, FORGOT_PASSWORD
                    )
                )
                findNavController().safeNavigate(
                    R.id.action_linkEmailAccountFragment_to_resetPasswordSendEmailFragment,
                    bundleOf(
                        EMAIL_ADDRESS to viewModel.validEmail
                    )
                )
            }

            btnLogin.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Acquisition(ACQUISITION_LOGIN),
                        LINK_EMAIL_SCREEN,
                        labelKeyword = KEYWORD_TYPE,
                        loginSignUpMethod = EMAIL_KEY
                    )
                )
                viewModel.login(
                    etEmailAddress.text.toString(),
                    etPassword.text.toString(),
                    merge = true,
                    socialToken = args.socialToken,
                    provider = args.provider
                )
            }

            etEmailAddress.addTextChangedListener {
                viewModel.saveEmailIfValid(it.toString())
                cvErrorHolder.visibility = View.GONE
                requireContext().hideError(
                    tilEmailAddress,
                    etEmailAddress
                ).hideErrorOnStartIcon(tilEmailAddress)
            }

            etPassword.addTextChangedListener {
                cvErrorHolder.visibility = View.GONE
                tilPassword.isPasswordVisibilityToggleEnabled = !it.isNullOrEmpty()
                requireContext().hideError(
                    tilPassword,
                    etPassword
                ).hideErrorOnStartIcon(tilPassword)
            }

            viewModel.canClickOnLogin.observe(viewLifecycleOwner) {
                btnLogin.isClickable = it
            }

            viewModel.loginResult.oneTimeEventObserve(viewLifecycleOwner, { loginResult ->
                when (loginResult) {
                    is LoginViewModel.LoginResult.TooManyFailedLogins -> {
                        findNavController().safeNavigate(LinkEmailAccountFragmentDirections.actionLinkEmailAccountFragmentToTooManyAttemptsFragment())
                    }

                    is LoginViewModel.LoginResult.LoginFailed -> {
                        cvErrorHolder.visibility = View.VISIBLE
                        tvErrorMessage.text =
                            getString(
                                R.string.error_unable_to_login
                            )
                    }

                    is LoginViewModel.LoginResult.EmailOrPasswordAreInvalid, LoginViewModel.LoginResult.EmailOrPasswordFormatsAreIncorrect, LoginViewModel.LoginResult.EmailOrPasswordAreNotEntered -> {
                        cvErrorHolder.visibility = View.VISIBLE
                        tvErrorMessage.text =
                            getString(R.string.error_incorrect_email_address_or_password)
                    }

                    is LoginViewModel.LoginResult.LoginSuccessful -> {
                        logCustomEvent(LoginWithEmail)
                        appDataViewModel.fetchAllInfo()
                        findNavController().safeNavigate(
                            LinkEmailAccountFragmentDirections.actionLinkEmailAccountFragmentToLinkSuccessfulFragment(
                                args.providerText
                            )
                        )
                    }
                }
            })
        }
    }

    override val logTag = "LinkEmailAccountFragment"

    override val analyticsScreenName: String = "sign_up.link email account"
}
