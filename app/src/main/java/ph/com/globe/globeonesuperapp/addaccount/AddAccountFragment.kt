/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.addaccount

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logUiActionEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.addmoreaccounts.AddAccountMoreAccountsViewModel
import ph.com.globe.globeonesuperapp.databinding.AddAccountFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class AddAccountFragment : NoBottomNavViewBindingFragment<AddAccountFragmentBinding>(
    bindViewBy = {
        AddAccountFragmentBinding.inflate(it)
    }
), AnalyticsScreen {

    private val addAccountMoreAccountsViewModel: AddAccountMoreAccountsViewModel by navGraphViewModels(
        R.id.navigation_add_account
    ) { defaultViewModelProviderFactory }

    private val addAccountFragmentArgs: AddAccountFragmentArgs by navArgs()

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLightStatusBar()

        with(viewBinding) {

            addAccountMoreAccountsViewModel.apply {
                newUser = addAccountFragmentArgs.newUser
                dacFlow = addAccountFragmentArgs.dacFlow
            }

            tvLogout.setOnClickListener {
                logUiActionEvent("Logout")
                addAccountMoreAccountsViewModel.logout()
            }

            btnAddAccount.setOnClickListener {
                logUiActionEvent("Add account")
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Conversion,
                        ADD_ACCOUNT_SCREEN, BUTTON, ADD_ACCOUNT
                    )
                )
                findNavController().safeNavigate(
                    AddAccountFragmentDirections.actionAddAccountFragmentToAddAccountNumberFragment()
                )
            }

            btnDoItLater.setOnClickListener {
                logUiActionEvent(getString(R.string.do_it_later))
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        ADD_ACCOUNT_SCREEN, BUTTON, I_WILL_DO_IT_LATER
                    )
                )
                findNavController().safeNavigate(
                    AddAccountFragmentDirections.actionAddAccountFragmentToAddAccountFinishFragment()
                )
            }
        }
    }

    override val logTag = "AddAccountFragment"

    override val analyticsScreenName: String = "enrollment.get_started"
}
