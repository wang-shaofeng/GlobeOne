/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards.allrewards

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.CLICKABLE_TEXT
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.REWARDS_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.AllRewardsInnerFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.RewardsViewModel
import ph.com.globe.globeonesuperapp.rewards.SortType
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.EntryPoint
import ph.com.globe.globeonesuperapp.rewards.allrewards.enrolled_accounts.SelectEnrolledAccountFragment
import ph.com.globe.globeonesuperapp.rewards.pos.EnrolledAccountWithPoints
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.EmptyStateAdapter
import ph.com.globe.globeonesuperapp.rewards.rewards_adapters.RewardsAdapter
import ph.com.globe.globeonesuperapp.shop.promo.search.SEARCH_TYPE_REWARDS
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
class AllRewardsInnerFragment :
    NoBottomNavViewBindingFragment<AllRewardsInnerFragmentBinding>(bindViewBy = {
        AllRewardsInnerFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val rewardsViewModel: RewardsViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    private val fragmentArgs by navArgs<AllRewardsInnerFragmentArgs>()

    private var selectedTabPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:rewards screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        fragmentArgs.selectedEnrolledAccount?.let { account ->
            rewardsViewModel.setEnrolledAccount(account)
        }

        parentFragmentManager.setFragmentResultListener(
            SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val result =
                bundle.getSerializable(SelectEnrolledAccountFragment.ENROLLED_ACCOUNT_NUMBER_KEY) as EnrolledAccount

            rewardsViewModel.setEnrolledAccount(result)
        }

        with(viewBinding) {
            if (rewardsViewModel.isLoggedIn()) {
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

            cvRewardsSignUp.setOnClickListener {
                generalEventsViewModel.lastNavHostFragmentKey(
                    BaseActivity.REWARDS_KEY,
                    R.id.allRewardsInnerFragment
                )
                crossBackstackNavigator.crossNavigate(
                    BaseActivity.AUTH_KEY,
                    R.id.selectSignMethodFragment,
                    shouldPopToStartDestinationFromCurrentGraph = false
                )
            }

            ivFilter.setOnClickListener {
                findNavController().safeNavigate(R.id.action_allRewardsInnerFragment_to_rewardsFilterFragment)
            }

            tvSearchButton.setOnClickListener {
                findNavController().safeNavigate(
                    AllRewardsInnerFragmentDirections.actionAllRewardsInnerFragmentToSearchFragment(
                        SEARCH_TYPE_REWARDS, 0
                    )
                )
            }

            tilMobileNumber.setOnClickListener { chooseNumber() }
            etMobileNumber.setOnClickListener { chooseNumber() }

            wfRedeemRewards.onBack { findNavController().navigateUp() }

            btnPos.setOnClickListener {
                val account = rewardsViewModel.enrolledAccount.value
                if (account != null) {
                    findNavController().safeNavigate(
                        AllRewardsInnerFragmentDirections.actionAllRewardsInnerFragmentToPosSubgraph(
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
                        AllRewardsInnerFragmentDirections.actionAllRewardsInnerFragmentToPosSubgraph()
                    )
                }
            }

            spSortDropdown.adapter = ArrayAdapter(
                requireContext(),
                R.layout.sort_dropdown_item_layout,
                resources.getStringArray(R.array.rewards_sort_array)
            )

            tlBrands.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                private fun refresh(tab: TabLayout.Tab?) {
                    selectedTabPosition = tab!!.position
                    rewardsViewModel.setCurrentTab(RewardsCategory.toCategory(selectedTabPosition))
                }

                override fun onTabSelected(tab: TabLayout.Tab?) = refresh(tab)
                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = refresh(tab)
            })

            spSortDropdown.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        logCustomEvent(
                            analyticsEventsProvider.provideEvent(
                                EventCategory.Engagement,
                                REWARDS_SCREEN,
                                CLICKABLE_TEXT,
                                SortType.toAnalyticsTextValue(position)
                            )
                        )
                        rewardsViewModel.sortRewards(SortType.toSortType(position))
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }

            tlBrands.selectTab(
                tlBrands.getTabAt(
                    if (selectedTabPosition == -1) RewardsCategory.toInt(fragmentArgs.tab)
                    else selectedTabPosition
                )
            )
            val rewardsAdapter = RewardsAdapter {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        REWARDS_SCREEN, CLICKABLE_TEXT, it.name
                    )
                )
                findNavController().safeNavigate(
                    AllRewardsInnerFragmentDirections.actionAllRewardsInnerFragmentToRewardDetailsFragment(
                        it
                    )
                )
            }

            val emptyStateAdapter = EmptyStateAdapter()

            rvRewards.adapter = ConcatAdapter(rewardsAdapter, emptyStateAdapter)

            with(rewardsViewModel) {
                sortRewards.observe(viewLifecycleOwner) {
                    spSortDropdown.setSelection(it.ordinal)
                }


                rewardsForCategory.observe(viewLifecycleOwner) {
                    if (it.isNotEmpty()) {
                        emptyStateAdapter.visibility = View.GONE
                        tvNumOfRewards.text = SpannableString(
                            resources.getQuantityString(
                                R.plurals.displaying_x_results,
                                it.size,
                                it.size
                            )
                        ).apply {
                            setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.neutral_A_0
                                    )
                                ),
                                resources.getString(R.string.displaying).length,
                                length - resources.getString(R.string.rewards).length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        tvNumOfRewards.visibility = View.VISIBLE
                        rewardsAdapter.submitList(it)
                        rvRewards.postDelayed(200) {
                            if (getView() != null) {
                                rvRewards.layoutManager?.scrollToPosition(0)
                            }
                        }
                    } else {
                        tvNumOfRewards.text = ""
                        rewardsAdapter.submitList(it)
                        emptyStateAdapter.visibility = View.VISIBLE
                    }
                }

                enrolledAccount.observe(viewLifecycleOwner) {
                    if (it != null) {
                        etMobileNumber.setText(
                            it.enrolledAccount.primaryMsisdn.toDisplayUINumberFormat()
                        )
                        tilMobileNumber.hint = it.enrolledAccount.accountAlias

                        tvPts.isVisible = true
                        ivStar.isVisible = true
                        tvPts.text = getString(R.string.pts_placeholder, it.points.toInt().toString())
                    } else {
                        etMobileNumber.setText("")
                        tilMobileNumber.hint = getString(R.string.mobile_number)

                        tvPts.isVisible = false
                        ivStar.isVisible = false
                    }
                }

                appliedFilters.observe(viewLifecycleOwner) {
                    if (it.isEmpty()) {
                        tvFilterBadge.visibility = View.GONE
                        tvFilterBadge.text = "0"
                    } else {
                        tvFilterBadge.visibility = View.VISIBLE
                        tvFilterBadge.text = "${it.size}"
                    }
                }
            }
        }
    }

    private fun chooseNumber() {
        findNavController().safeNavigate(
            AllRewardsInnerFragmentDirections.actionAllRewardsInnerFragmentToSelectEnrolledAccountFragment(
                EntryPoint.REWARDS
            )
        )
    }

    override val logTag: String = "AllRewardsInnerFragment"

    override val analyticsScreenName = "rewards.all"
}

const val SELECTED_ENROLLED_ACCOUNT_KEY = "selected_enrolled_account"

const val TAB_KEY = "tab"
