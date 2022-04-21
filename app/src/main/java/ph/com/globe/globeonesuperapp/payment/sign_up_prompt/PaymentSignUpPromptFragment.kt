/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.sign_up_prompt

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.AUTH_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentSignUpPromptFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class PaymentSignUpPromptFragment :
    NoBottomNavViewBindingFragment<PaymentSignUpPromptFragmentBinding>(
        bindViewBy = { PaymentSignUpPromptFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: here status bar should be transparent according to figma
        setDarkStatusBar()

        with(viewBinding) {
            btnSignUp.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        SIGN_UP_SCREEN, BUTTON, SIGN_UP
                    )
                )
                crossBackstackNavigator.crossNavigateWithoutHistory(AUTH_KEY, R.id.selectSignMethodFragment)
            }
            btnMaybeLater.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        SIGN_UP_SCREEN, BUTTON, MAYBE_LATER
                    )
                )
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    AUTH_KEY,
                    R.id.selectSignMethodFragment
                )
            }
        }
        // Adding a callback on back pressed to replace the standard up navigation with popBackStack
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(R.id.paymentLandingFragment, false)
                }
            }
        )
    }

    override val logTag = "PaymentSignUpPromptFragment"

    override val analyticsScreenName: String = "pay.sign_in_prompt"
}
