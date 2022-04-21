/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.AccountEnrollmentRaffleViewModel
import ph.com.globe.globeonesuperapp.addaccount.AccountEnrollmentRaffleViewModel.RaffleResult.RaffleSuccess
import ph.com.globe.globeonesuperapp.dashboard.CREDITED_TO_KEY
import ph.com.globe.globeonesuperapp.dashboard.TICKETS_EARNED_KEY
import ph.com.globe.globeonesuperapp.dashboard.TITLE_KEY
import ph.com.globe.globeonesuperapp.databinding.AddAccountMoreAccountsFragmentBinding
import ph.com.globe.globeonesuperapp.rewards.data_as_currency.ACCOUNTS_COUNT_UPDATE_REQUIRED
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import ph.com.globe.model.util.brand.AccountSegment
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountMoreAccountsFragment :
    NoBottomNavViewBindingFragment<AddAccountMoreAccountsFragmentBinding>(
        bindViewBy = {
            AddAccountMoreAccountsFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private lateinit var addAccountMoreAccountsRecyclerViewAdapter: AddAccountMoreAccountsRecyclerViewAdapter

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by hiltNavGraphViewModels(
        R.id.navigation_add_account
    )

    private val accountEnrollmentRaffleViewModel: AccountEnrollmentRaffleViewModel by hiltNavGraphViewModels(
        R.id.navigation_add_account
    )

    private var currentTab = 0
    private val emptyViewMobileDescription by lazy { getString(R.string.no_accounts_description_mobile) }
    private val emptyViewBroadbandDescription by lazy { getString(R.string.no_accounts_description_broadband) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:add account screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        addAccountMoreAccountsRecyclerViewAdapter =
            AddAccountMoreAccountsRecyclerViewAdapter({ account ->
                addAccountMoreAccountsViewModel.createDeleteAccountDialog(
                    {
                        addAccountMoreAccountsViewModel.deleteAccount(account, currentTab)
                    },
                    {}
                )
            }, {
                findNavController().safeNavigate(
                    AddAccountMoreAccountsFragmentDirections.actionAddAccountMoreAccountsFragmentToAddAccountDetailsFragment(
                        it,
                        currentTab
                    )
                )
            })

        with(viewBinding) {
            rvAccounts.layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun supportsPredictiveItemAnimations() = false
            }
            rvAccounts.adapter = addAccountMoreAccountsRecyclerViewAdapter

            with(addAccountMoreAccountsViewModel) {
                filterAccounts()

                filteredAccounts.observe(viewLifecycleOwner, { list ->
                    addAccountMoreAccountsRecyclerViewAdapter.submitList(list)
                    addAccountMoreAccountsRecyclerViewAdapter.notifyDataSetChanged()
                    rvAccounts.smoothScrollToPosition(0)
                })

                tlAccounts.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        currentTab = tab!!.position
                        filterAccounts(tab.position)
                        addAccountMoreAccountsRecyclerViewAdapter.updateEmptyViewDescriptionText(
                            when (tab.position) {
                                1 -> emptyViewMobileDescription
                                2 -> emptyViewBroadbandDescription
                                else -> ""
                            }
                        )
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

                    override fun onTabReselected(tab: TabLayout.Tab?) = Unit

                })

                btnProceed.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Conversion,
                            ADD_ACCOUNT_SCREEN, BUTTON, PROCEED
                        )
                    )
                    logUiActionEvent(
                        "Proceed",
                        additionalParams = mapOf(
                            AccountSegment.Mobile.toString() to numberOfMobileAccountsAdded()
                                .toString(),
                            AccountSegment.Broadband.toString() to numberOfBroadbandAccountsAdded()
                                .toString()
                        )
                    )
                    // TODO remove migration flow migrateOldAccounts()
                    decideNextActionAfterAccountEnrollment()
                }

                btnAddAccount.setOnClickListener {
                    logCustomEvent(
                        analyticsEventsProvider.provideEvent(
                            EventCategory.Conversion,
                            ADD_ACCOUNT_SCREEN, BUTTON, ADD_ACCOUNT
                        )
                    )
                    logCustomEvent(
                        analyticsEventsProvider.provideCustomGAEvent(
                            GAEventCategory.Registration,
                            ENROLL_ACCOUNT,
                            encryptedUserEmail
                        )
                    )
                    navigateToAccountEnrollment()
                }

                tvLogout.setOnClickListener {
                    addAccountMoreAccountsViewModel.logout()
                }

                enableProceedButton.observe(viewLifecycleOwner, {
                    it.handleEvent {
                        btnProceed.isEnabled = it
                    }
                })

                // TODO remove the migration flow from the app
                /*enrollMigratedAccountsResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->
                        when (result) {
                            is EnrollMigratedAccountsSuccess -> {
                                findNavController().safeNavigate(
                                    AddAccountMoreAccountsFragmentDirections.actionAddAccountMoreAccountsFragmentToAddAccountFinishFragment()
                                )
                            }

                            is AddAccountSuccess -> {
                                decideNextActionAfterAccountEnrollment()
                            }
                        }
                    }
                })*/

                accountEnrollmentRaffleViewModel.raffleResult.observe(viewLifecycleOwner, {
                    it.handleEvent { result ->
                        when {
                            dacFlow -> {
                                crossBackstackNavigator.crossNavigate(
                                    BaseActivity.REWARDS_KEY,
                                    R.id.dataAsCurrencyFragment,
                                    bundleOf(ACCOUNTS_COUNT_UPDATE_REQUIRED to true)
                                )
                            }
                            hasPromo -> {
                                findNavController().safeNavigate(R.id.action_addAccountMoreAccountsFragment_to_surpriseIsComingFragment)
                            }
                            result is RaffleSuccess -> {
                                crossBackstackNavigator.crossNavigateWithoutHistory(
                                    BaseActivity.RAFFLE_KEY,
                                    R.id.earnedRaffleTicketsFragment,
                                    bundleOf(
                                        TITLE_KEY to getString(R.string.earned_tickets),
                                        CREDITED_TO_KEY to result.profileName,
                                        TICKETS_EARNED_KEY to result.numOfTickets
                                    )
                                )
                            }
                            else -> {
                                crossBackstackNavigator.crossNavigateWithoutHistory(
                                    BaseActivity.DASHBOARD_KEY,
                                    R.id.dashboardFragment
                                )
                            }
                        }
                    }
                })
            }
        }
    }

    private fun decideNextActionAfterAccountEnrollment() {
        with(addAccountMoreAccountsViewModel) {
            if (newUser)
                findNavController().safeNavigate(
                    AddAccountMoreAccountsFragmentDirections.actionAddAccountMoreAccountsFragmentToAddAccountFinishFragment()
                )
            else accountEnrollmentRaffleViewModel.getInfo()
        }
    }

    private fun navigateToAccountEnrollment() {
        logUiActionEvent("Add account")
        findNavController().safeNavigate(AddAccountMoreAccountsFragmentDirections.actionAddAccountMoreAccountsFragmentToAddAccountNumberFragment())
    }

    override val logTag = "AddAccountMoreAccountsFragment"

    override val analyticsScreenName: String = "enrollment.enroll_globe_accounts"
}
