/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.pos

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.PAY
import ph.com.globe.analytics.events.PAYMENT_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.PayPointsFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.balance.toFormattedDisplayBalance
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.toDateWithTimeZoneOrNull
import ph.com.globe.util.toFormattedStringOrEmpty
import javax.inject.Inject

@AndroidEntryPoint
class PayPointsFragment : NoBottomNavViewBindingFragment<PayPointsFragmentBinding>({
    PayPointsFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val posViewModel by navGraphViewModels<POSViewModel>(R.id.pos_subgraph) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding) {
            ivBack.setOnClickListener { findNavController().navigateUp() }

            (posViewModel.chosenAccount
                ?: posViewModel.selectedAccount?.enrolledAccountWithPoints)?.let {

                tvAccountName.text = it.enrolledAccount.accountAlias
                tvAccountNumber.text =
                    it.enrolledAccount.primaryMsisdn.toDisplayUINumberFormat()
                tvBrand.text = it.brand?.name

                tvUserPts.text = getString(
                    R.string.pts_placeholder,
                    it.points.toFormattedDisplayBalance()
                )

                tvExpDate.text = getString(
                    R.string.rewards_expires_on,
                    it.expiringAmount,
                    it.expirationDate?.toDateWithTimeZoneOrNull()
                        ?.toFormattedStringOrEmpty(GlobeDateFormat.RewardPointsExpiry)
                )

                btnPay.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Conversion,
                            PAYMENT_SCREEN, BUTTON, PAY
                        )
                    )
                    tilPoints.error = ""
                    posViewModel.redeemPoints(etPoints.text.toString().toFloatOrNull() ?: 0f)
                }
            }

            posViewModel.merchantDetails?.let {
                tvRewardCost.text = getString(R.string.mock_minimum_x_points, it.minimumPoints)
                tvRewardTitle.text = it.merchantName
            }

            posViewModel.errorPoints.oneTimeEventObserve(viewLifecycleOwner) {
                tilPoints.error = if (it) {
                    getString(R.string.you_have_exceeded_your_points)
                } else {
                    getString(
                        R.string.please_enter_at_least,
                        posViewModel.merchantDetails?.minimumPoints
                    )
                }
            }

            posViewModel.posSuccessStatus.oneTimeEventObserve(viewLifecycleOwner) { resultStatus ->
                when (resultStatus) {
                    is POSSuccessStatus.POSSuccessful -> {
                        findNavController().safeNavigate(
                            PayPointsFragmentDirections.actionPayPointsFragmentToPOSRedeemPointsSuccessfulFragment(
                                "GLAM${resultStatus.transactionId}"
                            )
                        )
                    }
                    is POSSuccessStatus.POSUnsuccessful -> {
                        findNavController().safeNavigate(
                            PayPointsFragmentDirections.actionPayPointsFragmentToPOSRedeemPointsUnsuccessfulFragment(
                                State.SOMETHING_WENT_WRONG
                            )
                        )
                    }
                }
            }
        }
    }

    override val logTag: String = "PayPointsFragment"

    override val analyticsScreenName = "pos.pay_points"
}
