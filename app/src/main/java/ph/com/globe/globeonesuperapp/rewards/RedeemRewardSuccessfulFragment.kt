/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.CONFIRMATION_SCREEN
import ph.com.globe.analytics.events.DONE
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.DASHBOARD_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.voucher_pocket.ENROLLED_ACCOUNT
import ph.com.globe.globeonesuperapp.account.voucher_pocket.IS_SHOW_CONTENT
import ph.com.globe.globeonesuperapp.databinding.RedeemRewardsSuccessfulFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.permissions.registerActivityResultForStoragePermission
import ph.com.globe.globeonesuperapp.utils.permissions.requestStoragePermissionsIfNeededAndPerformSuccessAction
import ph.com.globe.globeonesuperapp.utils.takeScreenshotFlow
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.profile.domain_models.isPostpaidBroadband
import ph.com.globe.model.rewards.RewardsCategory.DONATION
import ph.com.globe.model.rewards.RewardsCategory.PROMO
import javax.inject.Inject

@AndroidEntryPoint
class RedeemRewardSuccessfulFragment :
    NoBottomNavViewBindingFragment<RedeemRewardsSuccessfulFragmentBinding>({
        RedeemRewardsSuccessfulFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val args by navArgs<RedeemRewardSuccessfulFragmentArgs>()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    var requestStorageActivityLauncher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestStorageActivityLauncher = registerActivityResultForStoragePermission {
            takeScreenshotFlow(viewBinding)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}

        with(viewBinding) {

            btnViewPocket.setOnClickListener {
                crossBackstackNavigator.crossNavigate(
                    DASHBOARD_KEY,
                    R.id.vouchers_subgraph,
                    bundleOf(
                        ENROLLED_ACCOUNT to args.enrolledAccount,
                        IS_SHOW_CONTENT to false
                    )
                )
            }

            btnDone.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        CONFIRMATION_SCREEN, CLICKABLE_TEXT, DONE,
                        productName = args.rewardItem.name
                    )
                )
                findNavController().popBackStack(R.id.rewardDetailsFragment, true)
            }

            ivDownloadReward.setOnClickListener {

                logUiActionEvent("Download receipt option")
                if (requestStorageActivityLauncher != null) {
                    requestStoragePermissionsIfNeededAndPerformSuccessAction(
                        requestStorageActivityLauncher!!
                    )
                } else {
                    takeScreenshotFlow(viewBinding)
                }
            }

            with(args.redeemRewords) {
                tvTotalPts.text = "${-loyaltyPoints[0].toInt()} ${getString(R.string.pts)}"
                tvRemainingPts.text =
                    "${args.points.toInt()} ${getString(R.string.pts)}"
            }

            with(args.rewardItem) {
                tvRewardName.text = name

                tvSuccessRewardDescription.text = when (category) {
                    PROMO, DONATION -> getString(
                        if (args.enrolledAccount.isPostpaidBroadband())
                            R.string.you_just_redeemed_a_reward
                        else
                            R.string.you_just_redeemed_a_reward_we_also_texted_you_the_confirmation
                    )
                    else -> {
                        btnViewPocket.isVisible = true
                        getString(
                            R.string.you_just_redeemed_a_reward_you_may_check_your_voucher_in_the_pocket
                        )
                    }
                }
            }

            tvAccountName.text = args.enrolledAccount.accountAlias
        }
    }

    override val logTag: String = "RedeemRewardsSuccessfulFragment"
    override val analyticsScreenName: String = "rewards.success"
}
