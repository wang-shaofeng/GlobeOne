/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards

import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.dashboard.raffle.RaffleViewModel
import ph.com.globe.globeonesuperapp.databinding.RewardsFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.RewardsAdapter
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setWhiteStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.BottomNavViewBindingFragment
import ph.com.globe.model.rewards.RewardsCatalogItem
import ph.com.globe.model.rewards.RewardsCatalogStatus
import ph.com.globe.model.rewards.RewardsCategory
import javax.inject.Inject

@AndroidEntryPoint
class RewardsFragment : BottomNavViewBindingFragment<RewardsFragmentBinding>(bindViewBy = {
    RewardsFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val rewardsViewModel: RewardsViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val raffleViewModel: RaffleViewModel by hiltNavGraphViewModels(R.id.rewards_subgraph)

    private val rewardsAdapter = RewardsAdapter {
        findNavController().safeNavigate(
            RewardsFragmentDirections.actionRewardsFragmentToRewardDetailsFragment(
                it
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:rewards screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setWhiteStatusBar()

        with(viewBinding) {
            with(rewardsViewModel) {

                logCustomEvent(
                    analyticsEventsProvider.provideCustomGAEvent(
                        GAEventCategory.Rewards,
                        GET_LOYALTY_REWARDS,
                        encryptedUserEmail
                    )
                )

                rvRewards.adapter = rewardsAdapter

                btnPos.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            REWARDS_SCREEN, CLICKABLE_ICON, CAMERA_SCANNING
                        )
                    )
                    findNavController().safeNavigate(RewardsFragmentDirections.actionRewardsFragmentToNavigationPos())
                }

                ivRedeem.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            REWARDS_SCREEN, CLICKABLE_ICON, REWARDS
                        )
                    )
                    findNavController().safeNavigate(R.id.action_rewardsFragment_to_AllRewradsFragment)
                }

                ivDonate.setOnClickListener {
                    findNavController().safeNavigate(
                        RewardsFragmentDirections.actionRewardsFragmentToAllRewardsInnerFragment(
                            RewardsCategory.DONATION
                        )
                    )
                }

                ivRedeemPayWithPoints.setOnClickListener {
                    findNavController().safeNavigate(
                        RewardsFragmentDirections.actionRewardsFragmentToNavigationPos()
                    )
                }

                mcvExplore.setOnClickListener {
                    findNavController().safeNavigate(R.id.action_rewardsFragment_to_AllRewradsFragment)
                }

                dacEnabled.observe(viewLifecycleOwner, { dacEnabled ->
                    ivConvertData.setEnabledState(dacEnabled)
                })

                accountsCount.observe(viewLifecycleOwner, { accountsCount ->
                    ivConvertData.setOnClickListener {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Engagement,
                                REWARDS_SCREEN, CLICKABLE_ICON, CONVERT_DATA
                            )
                        )
                        findNavController().safeNavigate(
                            if (accountsCount > 0) {
                                R.id.action_rewardsFragment_to_dataAsCurrencyFragment
                            } else {
                                R.id.action_rewardsFragment_to_noAccountsFragment
                            }
                        )
                    }
                })

                raffleViewModel.raffleInProgressLiveData.observe(
                    viewLifecycleOwner,
                    { (hasRaffleRewards, raffleInProgress) ->
                        ivExploreRaffles.isVisible = hasRaffleRewards && raffleInProgress
                        ivExploreRaffles.setOnClickListener {
                            findNavController().safeNavigate(
                                RewardsFragmentDirections.actionRewardsFragmentToAllRewardsInnerFragment(
                                    RewardsCategory.RAFFLE
                                )
                            )
                        }
                    })

                val rewardsObserver = Observer<List<RewardsCatalogItem>> { rewards ->
                    rewardsAdapter.submitList(rewards)
                    tvUseYourPoints.isVisible = rewards.isNotEmpty()
                }

                rewardsCatalogStatus.observe(viewLifecycleOwner) { status ->

                    // Setup rewards LiveData observer
                    with(displayRewards) {
                        when (status) {
                            RewardsCatalogStatus.Success -> observe(
                                viewLifecycleOwner,
                                rewardsObserver
                            )
                            else -> {
                                rewardsAdapter.submitList(emptyList())
                                removeObserver(rewardsObserver)
                            }
                        }
                    }

                    incLoading.root.isVisible = status is RewardsCatalogStatus.Loading
                    incError.root.isVisible = status is RewardsCatalogStatus.Error

                    groupMoreRewards.isVisible = status !is RewardsCatalogStatus.Error

                    // Handle subsections
                    (status is RewardsCatalogStatus.Success).let { isSuccess ->
                        ivRedeem.setEnabledState(isSuccess)
                        ivDonate.setEnabledState(isSuccess)
                    }
                }

                // Manual refreshing
                srlRewards.setRefreshListener {
                    fetchRewardsCatalog()
                }

                isRefreshingData.observe(viewLifecycleOwner) { refreshing ->
                    srlRewards.setRefreshing(refreshing)
                }

                // Reload
                incError.btnReload.setOnClickListener {
                    fetchRewardsCatalog()
                }
            }
        }
    }

    private fun MaterialButton.setEnabledState(enabled: Boolean) {
        isEnabled = enabled
        alpha = ResourcesCompat.getFloat(
            resources,
            if (enabled) R.dimen.enabled_view_alpha else R.dimen.disabled_view_alpha
        )
    }

    override val logTag = "RewardsFragment"

    override val analyticsScreenName = "rewards.main"
}
