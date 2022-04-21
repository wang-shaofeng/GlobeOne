/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.BUTTON
import ph.com.globe.analytics.events.CONFIRMATION_SCREEN
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.REDEEM
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.AppDataViewModel
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.RewardConfirmationFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.oneTimeEventObserve
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.model.util.brand.toUserFriendlyBrandName
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.rewards.RewardsCategory
import javax.inject.Inject

@AndroidEntryPoint
class RewardConfirmationFragment :
    NoBottomNavViewBindingFragment<RewardConfirmationFragmentBinding>({
        RewardConfirmationFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val appDataViewModel: AppDataViewModel by activityViewModels()
    private val rewardsViewModel: RewardsViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val args by navArgs<RewardConfirmationFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {

            wfRewardConfirmation.onBack {
                findNavController().navigateUp()
            }

            with(args.rewardItem) {
                tvPts.text =
                    if (pointsCost != "0") ("$pointsCost ${getString(R.string.pts)}") else getString(
                        R.string.free
                    )
                tvRewardName.text = name

                when (category) {
                    RewardsCategory.OTHER -> {
                        tvRewardType.text =
                            if (pointsCost == "0") getString(R.string.freebie_category)
                            else getString(R.string.other_category)

                    }
                    RewardsCategory.DONATION -> {
                        tvRewardType.setText(R.string.donation_category)
                    }
                    RewardsCategory.PROMO -> {
                        tvRewardType.setText(R.string.promo_category)
                    }
                }
            }

            with(rewardsViewModel) {
                enrolledAccount.observe(viewLifecycleOwner) {
                    if (it != null) {
                        tvAccountName.text = it.enrolledAccount.accountAlias
                        tvAccountNumber.text =
                            it.enrolledAccount.primaryMsisdn.toDisplayUINumberFormat()
                        tvBrand.text = it.brand.toUserFriendlyBrandName(it.segment)

                        tvUserPts.text = "${it?.points.toInt()} ${getString(R.string.pts)}"
                    }
                }

                redeemRewardsSuccessful.oneTimeEventObserve(viewLifecycleOwner) { (result, model) ->
                    appDataViewModel.refreshDataAfterTransaction(model.enrolledAccount.primaryMsisdn)
                    findNavController().safeNavigate(
                        RewardConfirmationFragmentDirections.actionRewardConfirmationFragmentToRedeemRewardsSuccessfulFragment(
                            result, args.rewardItem, model.enrolledAccount, model.points
                        )
                    )
                }

                redeemRewardsUnsuccessful.oneTimeEventObserve(viewLifecycleOwner) {
                    findNavController().safeNavigate(RewardConfirmationFragmentDirections.actionRewardConfirmationFragmentToRedeemRewardsUnsuccessfulFragment())
                }
            }

            btnRedeem.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        CONFIRMATION_SCREEN, BUTTON, REDEEM,
                        productName = args.rewardItem.name
                    )
                )
                rewardsViewModel.redeemReward(args.rewardItem)
            }
        }
    }

    override val logTag: String = "RewardConfirmationFragment"

    override val analyticsScreenName = "rewards.confirmation"
}
