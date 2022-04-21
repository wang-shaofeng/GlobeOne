/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.BaseActivity
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.EarnedRaffleTicketsFragmentBinding
import ph.com.globe.globeonesuperapp.shop.promo.itemdetails.RAFFLE_DETAILS_URL
import ph.com.globe.globeonesuperapp.utils.navigation.CrossBackstackNavigator
import ph.com.globe.globeonesuperapp.utils.setLightStatusBar
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class EarnedRaffleTicketsFragment :
    NoBottomNavViewBindingFragment<EarnedRaffleTicketsFragmentBinding>(
        bindViewBy = { EarnedRaffleTicketsFragmentBinding.inflate(it) }
    ) {

    @Inject
    lateinit var crossBackstackNavigator: CrossBackstackNavigator

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightStatusBar()

        with(viewBinding) {
            tvEarnedRaffleTicketsTitle.text = requireArguments().getString(TITLE_KEY) ?: ""
            tvCreditedToName.text = requireArguments().getString(CREDITED_TO_KEY) ?: ""
            tvTicketsEarnedNumber.text = requireArguments().getInt(TICKETS_EARNED_KEY).toString()

            tvGoToHome.setOnClickListener {
                crossBackstackNavigator.crossNavigateWithoutHistory(
                    BaseActivity.DASHBOARD_KEY,
                    R.id.dashboardFragment
                )
            }

            tvLearnMore.setOnClickListener {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(RAFFLE_DETAILS_URL))
                    startActivity(intent)
                }
            }
        }
    }

    override val logTag = "EarnedRaffleTicketsFragment"
}

const val TITLE_KEY = "title"
const val CREDITED_TO_KEY = "creditedTo"
const val TICKETS_EARNED_KEY = "ticketsEarned"
