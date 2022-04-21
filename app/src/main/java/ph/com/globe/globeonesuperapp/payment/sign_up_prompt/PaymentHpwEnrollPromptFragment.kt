/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.sign_up_prompt

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.ADD_ACCOUNT_KEY
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.AUTH_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentHpwEnrollPromptFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.convertToPrefixNumberFormat
import ph.com.globe.globeonesuperapp.utils.formatPhoneNumber
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class PaymentHpwEnrollPromptFragment :
    NoBottomNavViewBindingFragment<PaymentHpwEnrollPromptFragmentBinding>(
        bindViewBy = { PaymentHpwEnrollPromptFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val args by navArgs<PaymentHpwEnrollPromptFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            tvAddNumber.text = getString(
                R.string.add_now_in_the_app_for_faster_transactions,
                args.numberToEnroll.formatPhoneNumber()
            )
            btnAddAccount.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, BUTTON, ADD_ACCOUNT
                    )
                )
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    ADD_ACCOUNT_KEY,
                    R.id.addAccountNumberFragment,
                    bundleOf("numberToEnrollForBroadband" to args.numberToEnroll.convertToPrefixNumberFormat())
                )
            }
            btnMaybeLater.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, BUTTON, MAYBE_LATER
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
