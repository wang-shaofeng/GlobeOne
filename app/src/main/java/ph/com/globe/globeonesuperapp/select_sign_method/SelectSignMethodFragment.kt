/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.select_sign_method

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.events.custom.AccountRegisteredBySocial
import ph.com.globe.analytics.events.custom.LoginWithSocial
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.errors.GeneralError
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.SelectSignMethodFragmentBinding
import ph.com.globe.globeonesuperapp.login.LOGIN_SOCIAL_KEY
import ph.com.globe.globeonesuperapp.login.LoginViewModel
import ph.com.globe.globeonesuperapp.merge_login.LOGIN_MIGRATION_CASES
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.PROMO_ID
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.social_sign_in_controller.SignInException
import ph.com.globe.globeonesuperapp.utils.social_sign_in_controller.SocialSignInController
import ph.com.globe.globeonesuperapp.utils.spannedLinkString
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.PROVIDER
import ph.com.globe.globeonesuperapp.web_view_components.Provider
import ph.com.globe.globeonesuperapp.web_view_components.RESULT_TOKEN
import ph.com.globe.globeonesuperapp.web_view_components.SOCIAL_SIGN_IN_REQUEST_KEY
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject


@AndroidEntryPoint
class SelectSignMethodFragment :
    NoBottomNavViewBindingFragment<SelectSignMethodFragmentBinding>(
        bindViewBy = {
            SelectSignMethodFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()
    private val viewModel: SelectSignMethodViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private val appDataViewModel: AppDataViewModel by activityViewModels()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    lateinit var socialSignInController: SocialSignInController

    private var loginType: Provider? = null

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:select sign method screen"))

        socialSignInController.registerCallback {
            it.onSuccess {
                dLog("Social login: token is fetched.")
                lifecycleScope.launchWithLoadingOverlay(handler) {
                    withContext(Dispatchers.Default) {
                        when (it) {
                            is SocialSignInController.SignInData.Facebook -> {
                                loginViewModel.exchangeSocialAccessTokenWithGlobeSocialTokenAndSocialLogin(
                                    it.token,
                                    it.toSignInMethod()
                                )
                            }
                            is SocialSignInController.SignInData.Google.GoogleWithToken -> {
                                loginViewModel.exchangeSocialAccessTokenWithGlobeSocialTokenAndSocialLogin(
                                    it.token,
                                    it.toSignInMethod()
                                )
                            }
                            is SocialSignInController.SignInData.Google.GoogleWithEmail -> {
                                socialSignInController.generateSocialAccessTokenFromEmail(
                                    this@SelectSignMethodFragment,
                                    it
                                )
                            }
                        }
                    }
                }
            }.onFailure {
                dLog("Social login: social login failed.")
                if (it !is SignInException.CancelException) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        handler.handleGeneralError(GeneralError.General)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        socialSignInController.unregisterCallback()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDarkStatusBar()

        parentFragmentManager.setFragmentResultListener(
            SOCIAL_SIGN_IN_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val (token, provider) =
                bundle.getString(RESULT_TOKEN) to bundle.getParcelable<Provider>(PROVIDER)

            if (!token.isNullOrBlank() && provider != null)
                loginViewModel.loginSocial(token, provider)
        }

        parentFragmentManager.setFragmentResultListener(
            LOGIN_SOCIAL_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getParcelable<Provider>(PROVIDER)) {
                Provider.Apple -> viewBinding.incSocialButtons.btnAppleSignIn.performClick()
                Provider.Facebook -> viewBinding.incSocialButtons.btnFacebookSignIn.performClick()
                Provider.Google -> viewBinding.incSocialButtons.btnGoogleSignIn.performClick()
                Provider.Yahoo -> viewBinding.incSocialButtons.btnYahooSignIn.performClick()
                else -> Unit
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            generalEventsViewModel.lastNavHostFragmentKey(null, null, null)
            this.isEnabled = false

            requireActivity().onBackPressed()
        }

        with(viewBinding) {
            btnLogin.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        LOGIN_SCREEN, BUTTON, I_HAVE_AN_EXISTING_ACCOUNT
                    )
                )
                findNavController().safeNavigate(R.id.action_selectSignMethodFragment_to_loginFragment)
            }

            tvSignUp.movementMethod = LinkMovementMethod.getInstance()
            tvSignUp.text = spannedLinkString(
                getString(R.string.don_t_have_an_account) + " ",
                getString(R.string.create_one_now),
                tvSignUp.currentTextColor
            ) { findNavController().safeNavigate(R.id.action_selectSignMethodFragment_to_registerFragment) }

            incSocialButtons.btnFacebookSignIn.setOnClickListener {
                socialSignInController.signIn(
                    this@SelectSignMethodFragment,
                    SocialSignInController.SignInMethod.Facebook
                )
            }
            incSocialButtons.btnGoogleSignIn.setOnClickListener {
                socialSignInController.signIn(
                    this@SelectSignMethodFragment,
                    SocialSignInController.SignInMethod.Google
                )
            }
            incSocialButtons.btnYahooSignIn.setOnClickListener { openSocialLoginWebView(Provider.Yahoo) }
            incSocialButtons.btnAppleSignIn.setOnClickListener { openSocialLoginWebView(Provider.Apple) }

            btnShop.setOnClickListener {
                logUiActionEvent("Shop page")
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        LOGIN_SCREEN, CLICKABLE_ICON, SHOP
                    )
                )
                findNavController().safeNavigate(
                    SelectSignMethodFragmentDirections.actionSelectSignMethodFragmentToShopSubgraph(
                        tabToSelect = PROMO_ID
                    )
                )
            }

            btnGetRewards.setOnClickListener {
                logUiActionEvent("Get rewards page")
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        SIGN_UP_SCREEN, BUTTON, REWARDS
                    )
                )
                crossBackstackNavigator.crossNavigate(
                    BaseActivity.REWARDS_KEY,
                    R.id.allRewardsFragment
                )
            }

            btnDiscover.setOnClickListener {
                logUiActionEvent("Discover more page")
                findNavController().safeNavigate(
                    SelectSignMethodFragmentDirections.actionSelectSignMethodFragmentToDiscoverMoreFragment(
                        previousScreenTitle = getString(R.string.get_started)
                    )
                )
            }

            // Bubble visibility
            viewModel.bubbleVisibilityState.observe(viewLifecycleOwner, { isVisible ->
                ivQuickLinksBubble.isVisible = isVisible
            })

            loginViewModel.loginResult.oneTimeEventObserve(viewLifecycleOwner, {
                when (it) {
                    is LoginViewModel.LoginResult.RegisterSocialSuccessful -> {
                        logCustomEvent(AccountRegisteredBySocial(loginType.toString()))
                        appDataViewModel.fetchAllInfo()
                        findNavController().safeNavigate(
                            R.id.action_selectSignMethodFragment_to_navigation_add_account
                        )
                    }

                    is LoginViewModel.LoginResult.SocialLoginSuccessful -> {
                        logCustomEvent(LoginWithSocial(loginType.toString()))
                        appDataViewModel.fetchAllInfo()
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    }

                    /**
                     * if moreInfo is one of [LOGIN_MIGRATION_CASES], will navigate to the login migration
                     */
                    is LoginViewModel.LoginResult.LoginWithThisEmailAlreadyExists -> {
                        if (it.moreInfo == null || LOGIN_MIGRATION_CASES.contains(it.moreInfo).not()) {
                            findNavController().safeNavigate(
                                R.id.action_selectSignMethodFragment_to_socialLoginExistsFragment
                            )
                            return@oneTimeEventObserve
                        }

                        findNavController().safeNavigate(
                            SelectSignMethodFragmentDirections.actionSelectSignMethodFragmentToMergeLoginFragment(
                                it.loginSocialParams.socialProvider,
                                it.loginSocialParams.socialToken,
                                it.moreInfo
                            )
                        )
                    }
                    else -> Unit
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        socialSignInController.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openSocialLoginWebView(provider: Provider) {
        logSocialProviderClickedEvent(provider)
        findNavController().safeNavigate(
            R.id.action_selectSignMethodFragment_to_webViewFragment,
            bundleOf(PROVIDER to provider)
        )
    }

    private fun logSocialProviderClickedEvent(provider: Provider) {
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Acquisition(ACQUISITION_SIGNUP),
                LOGIN_SCREEN,
                labelKeyword = KEYWORD_TYPE,
                loginSignUpMethod = provider.toString()
            )
        )
    }

    override val logTag = "SelectUserFragment"

    override val analyticsScreenName: String = "get_started.landing_page"
}

private fun SocialSignInController.SignInData.toProvider() = when (this) {
    is SocialSignInController.SignInData.Facebook -> Provider.Facebook
    is SocialSignInController.SignInData.Google -> Provider.Google
}

private fun SocialSignInController.SignInData.toSignInMethod() = when (this) {
    is SocialSignInController.SignInData.Facebook -> SocialSignInController.SignInMethod.Facebook
    is SocialSignInController.SignInData.Google -> SocialSignInController.SignInMethod.Google
}
