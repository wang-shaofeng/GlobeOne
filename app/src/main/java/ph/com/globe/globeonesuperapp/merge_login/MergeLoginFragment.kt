/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.merge_login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.events.custom.AccountRegisteredBySocial
import ph.com.globe.analytics.events.custom.LoginWithSocial
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.errors.GeneralError
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.MergeLoginFragmentBinding
import ph.com.globe.globeonesuperapp.login.LoginViewModel
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.social_sign_in_controller.SignInException
import ph.com.globe.globeonesuperapp.utils.social_sign_in_controller.SocialSignInController
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.PROVIDER
import ph.com.globe.globeonesuperapp.web_view_components.Provider
import ph.com.globe.globeonesuperapp.web_view_components.RESULT_TOKEN
import ph.com.globe.globeonesuperapp.web_view_components.SOCIAL_SIGN_IN_REQUEST_KEY
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

val LOGIN_MIGRATION_CASES = listOf("capture", "googleplus", "facebook", "yahoo-oauth2", "apple")

@AndroidEntryPoint
class MergeLoginFragment : NoBottomNavViewBindingFragment<MergeLoginFragmentBinding>(bindViewBy = {
    MergeLoginFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var socialSignInController: SocialSignInController

    private var loginType: Provider? = null

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val loginViewModel: LoginViewModel by viewModels()
    private val appDataViewModel: AppDataViewModel by activityViewModels()
    private val args: MergeLoginFragmentArgs by navArgs()

    private lateinit var initialProvider: Provider
    private var rightIconRes: Int = 0
    private var leftIconRes: Int = 0
    private var providerText = ""
    private var moreInfo = ""
    private var alreadyLoggedText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:merge login screen"))

        fillProvider()
        fillMoreInfo()

        socialSignInController.registerCallback {
            it.onSuccess {
                dLog("Social login: token is fetched.")
                lifecycleScope.launchWithLoadingOverlay(handler) {
                    withContext(Dispatchers.Default) {
                        when (it) {
                            is SocialSignInController.SignInData.Facebook -> {
                                // login migration
                                loginViewModel.exchangeSocialAccessTokenWithGlobeSocialTokenAndSocialLogin(
                                    it.token,
                                    it.toSignInMethod(),
                                    merge = true,
                                    initialProvider = initialProvider,
                                    mergeToken = args.socialToken
                                )
                            }
                            is SocialSignInController.SignInData.Google.GoogleWithToken -> {
                                loginViewModel.exchangeSocialAccessTokenWithGlobeSocialTokenAndSocialLogin(
                                    it.token,
                                    it.toSignInMethod(),
                                    merge = true,
                                    initialProvider = initialProvider,
                                    mergeToken = args.socialToken
                                )
                            }
                            is SocialSignInController.SignInData.Google.GoogleWithEmail -> {
                                socialSignInController.generateSocialAccessTokenFromEmail(
                                    this@MergeLoginFragment,
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

    private fun fillProvider() {
        when (args.loginSocialProvider) {
            Provider.Google.getParam() -> {
                initialProvider = Provider.Google
                rightIconRes = R.drawable.ic_login_via_google
                providerText = GOOGLE
            }
            Provider.Facebook.getParam() -> {
                initialProvider = Provider.Facebook
                rightIconRes = R.drawable.ic_login_via_facebook
                providerText = FACEBOOK
            }
            Provider.Yahoo.getParam() -> {
                initialProvider = Provider.Yahoo
                rightIconRes = R.drawable.ic_login_via_yahoo
                providerText = YAHOO
            }
            Provider.Apple.getParam() -> {
                initialProvider = Provider.Apple
                rightIconRes = R.drawable.ic_login_via_apple
                providerText = APPLE
            }
            else -> {
                initialProvider = Provider.Google
                rightIconRes = R.drawable.ic_login_via_google
                providerText = GOOGLE
            }
        }
    }

    private fun fillMoreInfo() {
        moreInfo = args.moreInfo
        leftIconRes = when (moreInfo) {
            LOGIN_MIGRATION_CASES[0] -> R.drawable.ic_login_via_email
            LOGIN_MIGRATION_CASES[1] -> R.drawable.ic_login_via_google
            LOGIN_MIGRATION_CASES[2] -> R.drawable.ic_login_via_facebook
            LOGIN_MIGRATION_CASES[3] -> R.drawable.ic_login_via_yahoo
            LOGIN_MIGRATION_CASES[4] -> R.drawable.ic_login_via_apple
            else -> R.drawable.ic_login_via_google
        }
        alreadyLoggedText = when (moreInfo) {
            LOGIN_MIGRATION_CASES[0] -> getString(R.string.your_email)
            LOGIN_MIGRATION_CASES[1] -> GOOGLE
            LOGIN_MIGRATION_CASES[2] -> FACEBOOK
            LOGIN_MIGRATION_CASES[3] -> YAHOO
            LOGIN_MIGRATION_CASES[4] -> APPLE
            else -> GOOGLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        parentFragmentManager.setFragmentResultListener(
            SOCIAL_SIGN_IN_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val (token, provider) =
                bundle.getString(RESULT_TOKEN) to bundle.getParcelable<Provider>(PROVIDER)

            if (!token.isNullOrBlank() && provider != null)
                loginViewModel.loginSocial(
                    token,
                    initialProvider,
                    merge = true,
                    mergeToken = args.socialToken
                )
        }

        with(viewBinding) {
            wfMergeLogin.onBack {
                findNavController().navigateUp()
            }

            ivLeftIcon.setImageResource(leftIconRes)
            ivRightIcon.setImageResource(rightIconRes)

            tvAlreadyConnectedText.text =
                getString(R.string.looks_like_already_login, providerText)
            tvMergeAccountText.text =
                getString(R.string.merge_email_account, providerText, alreadyLoggedText)

            tvAlreadyConnectToAAccount.isVisible = moreInfo != LOGIN_MIGRATION_CASES[0]
            val suffix =
                if (moreInfo == LOGIN_MIGRATION_CASES[4]) getString(R.string.an) else getString(R.string.a)
            tvAlreadyConnectToAAccount.text =
                getString(
                    R.string.email_already_connected,
                    "$suffix $alreadyLoggedText"
                )

            btnVerify.setOnClickListener {
                when (moreInfo) {
                    LOGIN_MIGRATION_CASES[0] -> {
                        findNavController().safeNavigate(
                            MergeLoginFragmentDirections.actionMergeLoginFragmentToLinkEmailAccountFragment(
                                args.socialToken, args.loginSocialProvider, providerText
                            )
                        )
                    }
                    LOGIN_MIGRATION_CASES[1] -> {
                        socialSignInController.signIn(
                            this@MergeLoginFragment,
                            SocialSignInController.SignInMethod.Google
                        )
                    }
                    LOGIN_MIGRATION_CASES[2] -> {
                        socialSignInController.signIn(
                            this@MergeLoginFragment,
                            SocialSignInController.SignInMethod.Facebook
                        )
                    }
                    LOGIN_MIGRATION_CASES[3] -> {
                        openSocialLoginWebView(Provider.Yahoo)
                    }
                    LOGIN_MIGRATION_CASES[4] -> {
                        openSocialLoginWebView(Provider.Apple)
                    }
                }
            }
        }

        loginViewModel.loginResult.oneTimeEventObserve(viewLifecycleOwner, {
            when (it) {
                is LoginViewModel.LoginResult.RegisterSocialSuccessful -> {
                    logCustomEvent(AccountRegisteredBySocial(loginType.toString()))
                    appDataViewModel.fetchAllInfo()
                    findNavController().safeNavigate(
                        MergeLoginFragmentDirections.actionMergeLoginFragmentToLinkSuccessfulFragment(
                            providerText
                        )
                    )
                }

                is LoginViewModel.LoginResult.SocialLoginSuccessful -> {
                    logCustomEvent(LoginWithSocial(loginType.toString()))
                    appDataViewModel.fetchAllInfo()
                    findNavController().safeNavigate(
                        MergeLoginFragmentDirections.actionMergeLoginFragmentToLinkSuccessfulFragment(
                            providerText
                        )
                    )
                }

                else -> handler.handleGeneralError(GeneralError.General)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        socialSignInController.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openSocialLoginWebView(provider: Provider) {
        logSocialProviderClickedEvent(provider)
        findNavController().safeNavigate(
            R.id.action_mergeLoginFragment_to_webViewFragment,
            bundleOf(PROVIDER to provider)
        )
    }

    private fun logSocialProviderClickedEvent(provider: Provider) {
        logCustomEvent(
            analyticsEventsProvider.provideEvent(
                EventCategory.Acquisition(ACQUISITION_SIGNUP),
                MERGE_LOGIN_SCREEN,
                labelKeyword = KEYWORD_TYPE,
                loginSignUpMethod = provider.toString()
            )
        )
    }

    override fun onDestroy() {
        socialSignInController.unregisterCallback()
        super.onDestroy()
    }

    override val logTag = "MergeLoginFragment"

    override val analyticsScreenName: String = "sign_up.merge_login"

    companion object {
        private const val GOOGLE = "Google"
        private const val FACEBOOK = "Facebook"
        private const val YAHOO = "Yahoo!"
        private const val APPLE = "Apple"
    }
}

private fun SocialSignInController.SignInData.toSignInMethod() = when (this) {
    is SocialSignInController.SignInData.Facebook ->
        SocialSignInController.SignInMethod.Facebook

    is SocialSignInController.SignInData.Google ->
        SocialSignInController.SignInMethod.Google
}
