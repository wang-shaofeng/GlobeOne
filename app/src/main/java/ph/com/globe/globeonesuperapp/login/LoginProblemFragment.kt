/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.login

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.LoginProblemFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.globeonesuperapp.web_view_components.PROVIDER
import ph.com.globe.globeonesuperapp.web_view_components.Provider
import javax.inject.Inject

@AndroidEntryPoint
class LoginProblemFragment : NoBottomNavViewBindingFragment<LoginProblemFragmentBinding>(
    bindViewBy = { LoginProblemFragmentBinding.inflate(it) }
) {

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var analyticsLogger: GlobeAnalyticsLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:email login error screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            btnTryAgain.setOnClickListener {
                findNavController().popBackStack(R.id.loginFragment, false)
            }

            incSocialButtons.btnAppleSignIn.setOnClickListener { setResultAndNavigate(Provider.Apple) }
            incSocialButtons.btnGoogleSignIn.setOnClickListener { setResultAndNavigate(Provider.Google) }
            incSocialButtons.btnFacebookSignIn.setOnClickListener { setResultAndNavigate(Provider.Facebook) }
            incSocialButtons.btnYahooSignIn.setOnClickListener { setResultAndNavigate(Provider.Yahoo) }
        }
    }

    private fun setResultAndNavigate(provider: Provider) {
        setFragmentResult(LOGIN_SOCIAL_KEY, bundleOf(PROVIDER to provider))
        findNavController().popBackStack(R.id.loginFragment, true)
    }

    override val logTag = "LoginProblemFragment"
}

const val LOGIN_SOCIAL_KEY = "LoginProblemFragment_key"
