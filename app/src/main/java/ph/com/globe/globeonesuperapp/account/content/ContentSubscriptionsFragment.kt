/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.content

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.*
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsFragmentDirections
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.account.TAB_POSITION_CONTENT
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsContentFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment
import ph.com.globe.model.profile.domain_models.isPostpaidMobile
import ph.com.globe.model.shop.ContentSubscriptionUIModel
import javax.inject.Inject

@AndroidEntryPoint
class ContentSubscriptionsFragment :
    NestedViewBindingFragment<AccountDetailsContentFragmentBinding>(bindViewBy = {
        AccountDetailsContentFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }
    private val subscriptionsViewModel: ContentSubscriptionsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    // Different data source for postpaid mobile and prepaid account
    private val contentSubscriptions: LiveData<List<ContentSubscriptionUIModel>> by lazy {
        with(accountDetailsViewModel) {
            if (selectedEnrolledAccount.isPostpaidMobile()) {
                postpaidContentItems
            } else {
                subscriptionsViewModel.contentSubscriptions
            }
        }
    }

    private val subscriptionsAdapter by lazy {
        ContentSubscriptionsAdapter(accountDetailsViewModel.selectedEnrolledAccount) { subscription ->
            logCustomEvent(
                analyticsEventsProvider.provideEvent(
                    EventCategory.Engagement,
                    PRODUCTS_SCREEN, CLICKABLE_BANNER, subscription.promoName
                )
            )
            if (subscription.isActivated) {
                findNavController().safeNavigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToContentUnsubscribeFragment(
                        accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn,
                        subscription
                    )
                )
            } else {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(subscription.redirectionLink))
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:subscriptions screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {
            with(accountDetailsViewModel) {

                rvContentSubscriptions.adapter = subscriptionsAdapter
                clSubscriptionsInfo.isVisible = !selectedEnrolledAccount.isPostpaidMobile()
                lavLoading.isVisible = selectedEnrolledAccount.isPostpaidMobile()

                contentSubscriptions.observe(viewLifecycleOwner) {
                    subscriptionsAdapter.submitList(it)

                    if (selectedEnrolledAccount.isPostpaidMobile()) {
                        lavLoading.visibility = View.GONE
                        incEmptyState.root.isVisible = it.isEmpty()
                    }

                    // Send event to dynamically recalculate ViewPager height
                    onSubscriptionsDataLoaded(TAB_POSITION_CONTENT)
                }
            }
        }
    }

    override val logTag = "AccountDetailsContentFragment"

    override val analyticsScreenName = "account.subscriptions"
}
