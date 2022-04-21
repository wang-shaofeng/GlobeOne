/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.allrewards

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ConcatAdapter
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.AUTH_KEY
import ph.com.globe.globeonesuperapp.BaseActivity.Companion.REWARDS_KEY
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.dashboard.raffle.RaffleViewModel
import ph.com.globe.globeonesuperapp.databinding.AllRewardsFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.RewardsViewModel
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.EntryPoint
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.SelectEnrolledAccountFragment
import ph.com.globe.globeonesuperapp.rewards.pos.EnrolledAccountWithPoints
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.ButtonAdapter
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.CategoryAdapter
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.RewardsAdapter
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.TextAdapter
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.toDisplayUINumberFormat
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.rewards.RewardsCategory
import javax.inject.Inject

@AndroidEntryPoint
class AllRewardsFragment : NoBottomNavViewBindingFragment<AllRewardsFragmentBinding>(bindViewBy = {
    AllRewardsFragmentBinding.inflate(it)
}), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val rewardsViewModel: RewardsViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val raffleViewModel: RaffleViewModel by hiltNavGraphViewModels(R.id.rewards_subgraph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (!rewardsViewModel.isLoggedIn()) {
                crossBackstackNavigator.navigateToPreviousBackstack(
                    AUTH_KEY,
                    R.id.selectSignMethodFragment
                )
            } else {
                findNavController().navigateUp()
            }
        }

        parentFragmentManager.setFragmentResultListener(
            SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val result =
                bundle.getSerializable(SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_NUMBER_KEY) as EnrolledAccount

            rewardsViewModel.setEnrolledAccount(result)
        }

        with(rewardsViewModel) {
            with(viewBinding) {
                if (isLoggedIn()) {
                    cvRewardsSignUp.isVisible = false
                    tvBuyingFor.isVisible = true
                    tilMobileNumber.isVisible = true
                    btnPos.isVisible = true
                } else {
                    cvRewardsSignUp.isVisible = true
                    tvBuyingFor.isVisible = false
                    tilMobileNumber.isVisible = false
                    btnPos.isVisible = false
                }

                wfRewards.onBack {
                    if (!isLoggedIn()) {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            AUTH_KEY,
                            R.id.selectSignMethodFragment
                        )
                    } else {
                        findNavController().navigateUp()
                    }
                }

                cvRewardsSignUp.setOnClickListener {
                    generalEventsViewModel.lastNavHostFragmentKey(
                        REWARDS_KEY,
                        R.id.allRewardsFragment
                    )
                    crossBackstackNavigator.crossNavigate(
                        AUTH_KEY,
                        R.id.selectSignMethodFragment,
                        shouldPopToStartDestinationFromCurrentGraph = false
                    )
                }

                btnPos.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            REWARDS_SCREEN, CLICKABLE_ICON, CAMERA_SCANNING
                        )
                    )
                    val account = rewardsViewModel.enrolledAccount.value
                    if (account != null) {
                        findNavController().safeNavigate(
                            AllRewardsFragmentDirections.actionAllRewardsFragmentToPosSubgraph(
                                EnrolledAccountWithPoints(
                                    account.enrolledAccount,
                                    account.brand,
                                    account.points,
                                    account.expirationDate,
                                    account.expirationAmount
                                )
                            )
                        )
                    } else {
                        findNavController().safeNavigate(
                            AllRewardsFragmentDirections.actionAllRewardsFragmentToPosSubgraph()
                        )
                    }
                }

                tilMobileNumber.setOnClickListener { chooseNumber() }
                etMobileNumber.setOnClickListener { chooseNumber() }

                val categoryAdapter = CategoryAdapter {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            REWARDS_SCREEN, CLICKABLE_ICON, it.name
                        )
                    )
                    findNavController().safeNavigate(
                        AllRewardsFragmentDirections.actionAllRewardsFragmentToAllRewardsInnerFragment(
                            it
                        )
                    )
                }

                val checkTheseOutTextAdapter = TextAdapter(getString(R.string.check_these_out))
                val exclusivesTextAdapter = TextAdapter(getString(R.string.exclusives))

                val checkTheseOutRewardsAdapter = RewardsAdapter {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            REWARDS_SCREEN, CLICKABLE_TEXT, it.name
                        )
                    )
                    findNavController().safeNavigate(
                        AllRewardsFragmentDirections.actionAllRewardsFragmentToRewardDetailsFragment(
                            it
                        )
                    )
                }
                val threeFreeRandomRewardsAdapter = RewardsAdapter {
                    findNavController().safeNavigate(
                        AllRewardsFragmentDirections.actionAllRewardsFragmentToRewardDetailsFragment(
                            it
                        )
                    )
                }
                val buttonAdapter = ButtonAdapter {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Engagement,
                            REWARDS_SCREEN, BUTTON, VIEW_ALL_REWARDS
                        )
                    )
                    findNavController().safeNavigate(
                        AllRewardsFragmentDirections.actionAllRewardsFragmentToAllRewardsInnerFragment(
                            RewardsCategory.NONE
                        )
                    )
                }

                rvRewards.adapter = ConcatAdapter(
                    categoryAdapter,
                    checkTheseOutTextAdapter,
                    checkTheseOutRewardsAdapter,
                    exclusivesTextAdapter,
                    threeFreeRandomRewardsAdapter,
                    buttonAdapter
                )
                raffleViewModel.raffleInProgressLiveData.observe(viewLifecycleOwner) { (hasRaffleRewards, raffleInProgress) ->
                    categoryAdapter.raffleInProgress = raffleInProgress && hasRaffleRewards
                }

                checkTheseOutCatalogRewards.observe(
                    viewLifecycleOwner
                ) {
                    checkTheseOutRewardsAdapter.submitList(it)
                    checkTheseOutTextAdapter.isVisible = it.isNotEmpty()
                }

                threeFreeRandomRewards.observe(viewLifecycleOwner) {
                    threeFreeRandomRewardsAdapter.submitList(it)
                    exclusivesTextAdapter.isVisible = it.isNotEmpty()
                }

                enrolledAccount.observe(viewLifecycleOwner) {
                    if (it != null) {
                        etMobileNumber.setText(
                            it.enrolledAccount.primaryMsisdn.toDisplayUINumberFormat()
                        )
                        tilMobileNumber.hint = it.enrolledAccount.accountAlias
                        tvPts.isVisible = true
                        ivStar.isVisible = true
                        tvPts.text = "${it.points.toInt()} ${getString(R.string.pts)}"
                    } else {
                        etMobileNumber.setText("")
                        tilMobileNumber.hint = getString(R.string.mobile_number)
                        tvPts.isVisible = false
                        ivStar.isVisible = false
                    }
                }
            }
        }
    }

    private fun chooseNumber() {
        findNavController().safeNavigate(AllRewardsFragmentDirections
            .actionAllRewardsFragmentToSelectEnrolledAccountFragment(EntryPoint.REWARDS))
    }

    override val logTag: String = "AllRewardsFragment"

    override val analyticsScreenName = "rewards.all"
}
