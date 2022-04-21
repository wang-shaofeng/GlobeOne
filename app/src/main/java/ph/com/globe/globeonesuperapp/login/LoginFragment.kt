/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.events.custom.LoginWithEmail
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.LoginFragmentBinding
import ph.com.globe.globeonesuperapp.login.LoginViewModel.LoginResult
import ph.com.globe.globeonesuperapp.register.utils.EMAIL_MAX_LENGTH
import ph.com.globe.globeonesuperapp.register.utils.EmailValidator
import ph.com.globe.globeonesuperapp.register.utils.setupEmailInputFilter
import ph.com.globe.globeonesuperapp.reset_password.EMAIL_ADDRESS
import ph.com.globe.globeonesuperapp.termsandprivacypolicy.TERMS_AND_CONDITIONS_URL
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.hideError
import ph.com.globe.globeonesuperapp.utils.hideErrorOnStartIcon
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.spannedLinkString
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment :
    NoBottomNavViewBindingFragment<LoginFragmentBinding>(bindViewBy = {
        LoginFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val viewModel: LoginViewModel by viewModels()

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()
    private val appDataViewModel: AppDataViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:login screen"))
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
            tvPrivacyPolicyLink.text = spannedLinkString(
                getString(R.string.privacy_policy_info_login) + " ",
                getString(R.string.privacy_policy_link),
                AppCompatResources.getColorStateList(
                    requireContext(),
                    R.color.primary
                ).defaultColor
            ) {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_AND_CONDITIONS_URL))
                    startActivity(intent)
                }
            }

            btnForgotPassword.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Acquisition(ACQUISITION_LOGIN),
                        LOGIN_SCREEN, CLICKABLE_TEXT, FORGOT_PASSWORD
                    )
                )
                findNavController().safeNavigate(
                    R.id.action_loginFragment_to_resetPasswordSendEmailFragment, bundleOf(
                        EMAIL_ADDRESS to viewModel.validEmail
                    )
                )
            }

            btnLogin.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Acquisition(ACQUISITION_LOGIN),
                        LOGIN_SCREEN,
                        labelKeyword = KEYWORD_TYPE,
                        loginSignUpMethod = EMAIL_KEY
                    )
                )
                viewModel.login(
                    etEmailAddress.text.toString(),
                    etPassword.text.toString()
                )
            }

            tvSignUp.movementMethod = LinkMovementMethod.getInstance()
            tvSignUp.text = spannedLinkString(
                getString(R.string.don_t_have_an_account) + " ",
                getString(R.string.create_one_now),
                tvSignUp.currentTextColor
            ) { findNavController().safeNavigate(R.id.action_loginFragment_to_registerFragment) }

            etEmailAddress.apply {
                setupEmailInputFilter { maxLengthReached ->
                    cvEmailErrorHolder.isVisible = maxLengthReached
                    if (maxLengthReached) {
                        tvEmailErrorMessage.text = getString(
                            R.string.error_email_max_length,
                            EMAIL_MAX_LENGTH
                        )
                    }
                }
                addTextChangedListener {
                    viewModel.saveEmailIfValid(it.toString())
                    cvErrorHolder.visibility = View.GONE
                    requireContext().hideError(
                        tilEmailAddress,
                        etEmailAddress
                    ).hideErrorOnStartIcon(tilEmailAddress)
                }
                setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus && viewModel.emailValid.value == EmailValidator.Status.EmailIsInvalid) {
                        val messageId = R.string.error_invalid_email_format

                        cvEmailErrorHolder.visibility = View.VISIBLE
                        tvEmailErrorMessage.text = getString(messageId)
                    } else {
                        cvEmailErrorHolder.visibility = View.GONE
                    }
                }
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

            viewModel.loginResult.observe(viewLifecycleOwner, {
                it.handleEvent { loginResult ->
                    when (loginResult) {
                        is LoginResult.EmailOrPasswordAreNotEntered -> {
                            cvErrorHolder.visibility = View.VISIBLE
                            tvErrorMessage.text =
                                getString(R.string.error_please_enter_email_password)
                        }

                        is LoginResult.EmailOrPasswordAreInvalid -> {
                            findNavController().safeNavigate(R.id.action_loginFragment_to_loginProblemFragment)
                        }

                        is LoginResult.TooManyFailedLogins -> {
                            findNavController().safeNavigate(LoginFragmentDirections.actionLoginFragmentToTooManyAttemptsFragment())
                        }

                        is LoginResult.LoginFailed -> {
                            cvErrorHolder.visibility = View.VISIBLE
                            tvErrorMessage.text =
                                getString(
                                    R.string.error_unable_to_login
                                )
                        }

                        is LoginResult.EmailOrPasswordFormatsAreIncorrect, LoginResult.EmailOrPasswordAreNotEntered -> {
                            cvErrorHolder.visibility = View.VISIBLE
                            tvErrorMessage.text =
                                getString(R.string.error_incorrect_email_address_or_password)
                        }

                        is LoginResult.LoginSuccessful -> {
                            logCustomEvent(LoginWithEmail)
                            appDataViewModel.fetchAllInfo()
                            crossBackstackNavigator.crossNavigateWithoutHistory(
                                BaseActivity.DASHBOARD_KEY,
                                R.id.dashboardFragment
                            )
                        }

                        is LoginResult.LoginUnverified -> {
                            logCustomEvent(LoginWithEmail)
                            appDataViewModel.fetchAllInfo()
                            findNavController().navigate(R.id.action_loginFragment_to_emailVerificationFragment)
                        }
                    }
                }
            })
        }
    }

    override val logTag = "LoginFragment"

    override val analyticsScreenName: String = "sign_up.login"
}
