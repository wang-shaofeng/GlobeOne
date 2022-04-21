/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PosRedeemPointsUnsuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.pos.State.*
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class POSRedeemPointsUnsuccessfulFragment :
    NoBottomNavViewBindingFragment<PosRedeemPointsUnsuccessfulFragmentBinding>({
        PosRedeemPointsUnsuccessfulFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val args by navArgs<POSRedeemPointsUnsuccessfulFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {

            val goBackAnalyticsEvent = analyticsEventsProvider.provideEvent(
                EventCategory.Engagement,
                REDEEM_REWARDS_SCREEN, BUTTON, GO_BACK
            )

            when (args.state) {
                SOMETHING_WENT_WRONG -> {
                    btnBack.setOnClickListener { findNavController().navigateUp() }
                }
                QR_CODE_FAILURE_10_SECONDS -> {
                    tvHeaderMessage.text = getString(R.string.were_note_familiar_with_this_qr_code)
                    tvInfoMessage.text = getString(R.string.we_couldnt_scan_the_qr_code)
                    btnGoBack.isVisible = true
                    btnGoBack.setOnClickListener {
                        logCustomEvent(goBackAnalyticsEvent)
                        findNavController().popBackStack(R.id.payWithPointsFragment, true)
                    }
                    btnBack.setOnClickListener { findNavController().safeNavigate(R.id.action_POSRedeemPointsUnsuccessfulFragment_to_enterMerchantCodeFragment) }

                    btnBack.text = getString(R.string.enter_merchant_code)
                }
                QR_CODE_NOT_MATCH -> {
                    tvHeaderMessage.text = getString(R.string.were_note_familiar_with_this_qr_code)
                    tvInfoMessage.text = getString(R.string.scan_the_qr_code_again)
                    btnGoBack.isVisible = true
                    btnGoBack.setOnClickListener {
                        logCustomEvent(goBackAnalyticsEvent)
                        findNavController().popBackStack(R.id.payWithPointsFragment, true)
                    }
                    btnEnterMerchantCode.setOnClickListener {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Engagement,
                                REDEEM_REWARDS_SCREEN, BUTTON, ENTER_MERCHANT_CODE
                            )
                        )
                        findNavController().safeNavigate(R.id.action_POSRedeemPointsUnsuccessfulFragment_to_enterMerchantCodeFragment)
                    }
                    btnEnterMerchantCode.isVisible = true
                    btnBack.text = getString(R.string.try_again)
                    btnBack.setOnClickListener { findNavController().navigateUp() }
                }
            }
        }
    }

    override val logTag: String = "POSRedeemPointsUnsuccessfulFragment"

    override val analyticsScreenName = "pos.unsuccessfully"
}

enum class State {
    SOMETHING_WENT_WRONG, QR_CODE_FAILURE_10_SECONDS, QR_CODE_NOT_MATCH
}
