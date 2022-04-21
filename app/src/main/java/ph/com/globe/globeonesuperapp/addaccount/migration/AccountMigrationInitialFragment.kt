/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount.migration

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel.GetAccountsForMigrationResult.GetAccountsForMigrationSuccess
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel.GetAccountsForMigrationResult.NoAccounts
import ph.com.globe.globeonesuperapp.databinding.AddAccountMigrationInitialFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class AccountMigrationInitialFragment :
    NoBottomNavViewBindingFragment<AddAccountMigrationInitialFragmentBinding>(
        bindViewBy = {
            AddAccountMigrationInitialFragmentBinding.inflate(it)
        }
    ), AnalyticsScreen {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewBinding) {
            btnProceed.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ACCOUNT_MIGRATION_SCREEN, BUTTON, PROCEED
                    )
                )
                addAccountMoreAccountsViewModel.getMigratedAccounts(
                    requireArguments().getString(EMAIL_FOR_MIGRATION) ?: "",
                    requireArguments().getBoolean(IS_SOCIAL_LOGIN)
                )
            }

            btnDoItLater.setOnClickListener {
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ACCOUNT_MIGRATION_SCREEN, BUTTON, I_WILL_DO_IT_LATER
                    )
                )
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            addAccountMoreAccountsViewModel.getMigrationAccountsResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    when (result) {
                        is NoAccounts -> {
                            findNavController().safeNavigate(
                                AccountMigrationInitialFragmentDirections.actionAccountMigrationInitialFragmentToNoAccountsForMigrationFragment()
                            )
                        }

                        is GetAccountsForMigrationSuccess -> {
                            findNavController().safeNavigate(
                                AccountMigrationInitialFragmentDirections.actionAccountMigrationInitialFragmentToAddAccountMoreAccountsFragment()
                            )
                        }
                    }
                }
            })

            // Adding a callback on back pressed to replace the standard up navigation with popBackStack
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        crossBackstackNavigator.crossNavigateWithoutHistory(
                            BaseActivity.DASHBOARD_KEY,
                            R.id.dashboardFragment
                        )
                    }
                }
            )
        }
    }

    override val logTag = "AccountMigrationInitialFragment"

    override val analyticsScreenName = "account.migration_initial"
}

const val EMAIL_FOR_MIGRATION = "EmailForMigration"
const val IS_SOCIAL_LOGIN = "IsSocialLogin"
