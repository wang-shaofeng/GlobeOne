/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.personalized_campaigns

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.databinding.PersonalizedCampaignsLoadingFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class PersonalizedCampaignsLoadingFragment :
    NoBottomNavViewBindingFragment<PersonalizedCampaignsLoadingFragmentBinding>({
        PersonalizedCampaignsLoadingFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val campaignPromoArgs by navArgs<PersonalizedCampaignsLoadingFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:personalizedcampaign screen"))

        accountDetailsViewModel.pullFreebie(campaignPromoArgs)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {} // block on back press

        accountDetailsViewModel.pullFreebieSuccessfulStatus.observe(viewLifecycleOwner) {
            it.handleEvent {
                if (it) findNavController().safeNavigate(R.id.pullAFreebieSuccessFragment)
                else findNavController().navigateUp()
            }
        }
    }

    override val logTag: String = "PersonalizedCampaignsLoadingFragment"

    override val analyticsScreenName: String = "personalized_campaign.loading"
}
