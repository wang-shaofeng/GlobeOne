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
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.addaccount.AccountEnrollmentRaffleViewModel
import ph.com.globe.globeonesuperapp.addaccount.AccountEnrollmentRaffleViewModel.RaffleResult.*
import ph.com.globe.globeonesuperapp.dashboard.CREDITED_TO_KEY
import ph.com.globe.globeonesuperapp.dashboard.TICKETS_EARNED_KEY
import ph.com.globe.globeonesuperapp.dashboard.TITLE_KEY
import ph.com.globe.globeonesuperapp.databinding.SurpriseIsComingFragmentBinding
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class SurpriseIsComingFragment :
    NoBottomNavViewBindingFragment<SurpriseIsComingFragmentBinding>(bindViewBy = {
        SurpriseIsComingFragmentBinding.inflate(it)
    }) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val accountEnrollmentRaffleViewModel: AccountEnrollmentRaffleViewModel by hiltNavGraphViewModels(
        R.id.navigation_add_account
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setLightStatusBar()

        with(viewBinding) {
            accountEnrollmentRaffleViewModel.getInfo()
            accountEnrollmentRaffleViewModel.raffleResult.observe(viewLifecycleOwner, {
                it.handleEvent { result ->
                    if (result is RaffleSuccess) {
                        btnGoToDashboard.text = getString(R.string.next)
                        btnGoToDashboard.setOnClickListener {
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
                    } else {
                        btnGoToDashboard.setOnClickListener {
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

    override val logTag = "SurpriseIsComingFragment"
}
