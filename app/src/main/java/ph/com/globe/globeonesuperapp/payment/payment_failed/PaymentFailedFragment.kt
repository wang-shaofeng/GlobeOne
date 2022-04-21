/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.payment_failed

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PaymentFailedFragmentBinding
import ph.com.globe.globeonesuperapp.payment.PaymentNavigationViewModel
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class PaymentFailedFragment :
    NoBottomNavViewBindingFragment<PaymentFailedFragmentBinding>(
        bindViewBy = { PaymentFailedFragmentBinding.inflate(it) }
    ), AnalyticsScreen {

    private val navigationViewModel: PaymentNavigationViewModel by hiltNavGraphViewModels(R.id.payment_subgraph)

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val paymentFailedFragmentArgs: PaymentFailedFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(navigationViewModel) {
            with(viewBinding) {
                viewBinding.tvHeaderMessage.text = paymentFailedFragmentArgs.message
                if (paymentFailedFragmentArgs.messageInfo != null) {
                    viewBinding.tvInfoMessage.text = paymentFailedFragmentArgs.messageInfo
                }
                btnTryAgain.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            SUBSCRIPTION_SCREEN, CLICKABLE_TEXT, TRY_AGAIN,
                            productName = paymentParameters.paymentName
                        )
                    )
                    findNavController().popBackStack(R.id.paymentLandingFragment, false)
                }
                btnCancel.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            SUBSCRIPTION_SCREEN, CLICKABLE_TEXT, CANCEL_TRANSACTION,
                            productName = paymentParameters.paymentName
                        )
                    )
                    if (paymentParameters.isLoggedIn) {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    } else {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.AUTH_KEY,
                            R.id.selectSignMethodFragment
                        )
                    }
                }
            }
        }

        // Disable back button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
    }

    override val logTag = "PaymentFailedFragment"

    override val analyticsScreenName: String = "pay.results_failed"
}
