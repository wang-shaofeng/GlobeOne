/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.data

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.events.CLICKABLE_BANNER
import ph.com.globe.analytics.events.EventCategory
import ph.com.globe.analytics.events.PRODUCTS_SCREEN
import ph.com.globe.analytics.logCustomEvent
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsFragmentDirections
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.account.TAB_POSITION_DATA
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsDataConsumptionFragmentBinding
import ph.com.globe.globeonesuperapp.gocreate.select_account.GO_CREATE_ELIGIBLE_BRAND
import ph.com.globe.globeonesuperapp.group.*
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter
import ph.com.globe.globeonesuperapp.shop.promo.ShopOfferRecyclerViewAdapter
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.getRefreshDate
import ph.com.globe.globeonesuperapp.utils.navigation.safeNavigate
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsViewModel
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment
import ph.com.globe.model.account.DataUsageStatus.*
import ph.com.globe.model.account.getBrandSafely
import javax.inject.Inject

@AndroidEntryPoint
class DataUsageFragment :
    NestedViewBindingFragment<AccountDetailsDataConsumptionFragmentBinding>(bindViewBy = {
        AccountDetailsDataConsumptionFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val generalEventsViewModel: GeneralEventsViewModel by activityViewModels()

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }
    private val dataUsageViewModel: DataUsageViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val dataUsageAdapter by lazy {
        DataUsageAdapter(accountDetailsViewModel.selectedEnrolledAccount,
            itemCallback = { item, usageInfo ->
                logCustomEvent(
                    analyticsEventsProvider.provideEvent(
                        EventCategory.Engagement,
                        PRODUCTS_SCREEN, CLICKABLE_BANNER, usageInfo
                    )
                )
                when {
                    item.accountRole == GROUP_ROLE_OWNER -> {
                        findNavController().safeNavigate(
                            R.id.action_accountDetailsFragment_to_group_subgraph,
                            bundleOf(
                                OWNER_NUMBER to item.accountNumber,
                                OWNER_ALIAS to item.accountName,
                                SKELLING_WALLET to item.skelligWallet,
                                SKELLING_CATEGORY to item.skelligCategory
                            )
                        )
                    }
                    item.accountRole == GROUP_ROLE_MEMBER -> {
                        findNavController().safeNavigate(
                            AccountDetailsFragmentDirections.actionAccountDetailsFragmentToGroupMemberViewFragment(
                                item.groupOwnerMobileNumber,
                                item.skelligWallet,
                                item.skelligCategory,
                                item.accountNumber,
                                item.accountName
                            )
                        )
                    }
                    item.addOnData -> {
                        findNavController().safeNavigate(
                            AccountDetailsFragmentDirections.actionAccountDetailsFragmentToDataUsageInfoFragment(
                                item
                            )
                        )
                    }
                }
            },
            learnMoreCallback = {
                generalEventsViewModel.leaveGlobeOneAppNonZeroRated {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ACCESS_DATA_INFO_URL))
                    startActivity(intent)
                }
            })
    }

    private val historyAdapter = ShopOfferRecyclerViewAdapter {
        findNavController().safeNavigate(
            AccountDetailsFragmentDirections.actionAccountDetailsFragmentToShopSubgraph(
                ShopPagerAdapter.PROMO_ID,
                isToShopItemDetails = true,
                shopItem = it
            )
        )
    }
    private val subscribeAdapter = ShopOfferRecyclerViewAdapter {
        findNavController().safeNavigate(
            AccountDetailsFragmentDirections.actionAccountDetailsFragmentToShopSubgraph(
                ShopPagerAdapter.PROMO_ID,
                isToShopItemDetails = true,
                shopItem = it
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {

            rvConsumptionItems.adapter = dataUsageAdapter
            rvSuggestions.adapter = historyAdapter
            rvSubscribes.adapter = subscribeAdapter

            tvShowPromos.setOnClickListener {
                findNavController().safeNavigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToShopSubgraph(
                        tabToSelect = ShopPagerAdapter.PROMO_ID,
                        mobileNumber = accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn,
                        toolbarTab = getString(R.string.account_details)
                    )
                )
            }

            incGoCreateBanner.root.setOnClickListener {
                findNavController().navigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToGoCreateSubgraph(
                        entryPoint = getString(R.string.wayfinder_account_details),
                        accountDetailsViewModel.selectedEnrolledAccount.primaryMsisdn
                    )
                )
            }

            btnAllPromos.setOnClickListener {
                findNavController().safeNavigate(
                    AccountDetailsFragmentDirections.actionAccountDetailsFragmentToShopSubgraph(
                        ShopPagerAdapter.PROMO_ID,
                        isToShopPromoInner = true
                    )
                )
            }

            accountDetailsViewModel.brandStatus.observe(viewLifecycleOwner) { status ->
                val brand = status.getBrandSafely()
                gpTryTheseOutArea.isVisible = brand == GO_CREATE_ELIGIBLE_BRAND
                if (tvBottomSectionTitle.isVisible) {
                    dataUsageViewModel.showTryTheseOutItem(brand!!)
                }
            }

            accountDetailsViewModel.billingDetails.observe(viewLifecycleOwner) { billingDetails ->
                dataUsageAdapter.setRefreshDate(
                    billingDetails.getRefreshDate()
                )
            }

            with(dataUsageViewModel) {

                dataUsageStatus.observe(viewLifecycleOwner) { status ->
                    when (status) {
                        is Success -> {
                            dataUsageAdapter.submitList(status.usageItems)
                        }
                        Loading -> {
                            dataUsageAdapter.submitList(emptyList())
                            historyAdapter.submitList(emptyList())
                            subscribeAdapter.submitList(emptyList())
                        }
                    }

                    lavLoading.isVisible = status is Loading
                    incEmptyState.root.isVisible = status is Empty
                    notifyViewPagerHeightChanged()
                }

                tryTheseOut.observe(viewLifecycleOwner, {
                    historyAdapter.submitList(
                        it.take(2)
                    )
                    notifyViewPagerHeightChanged()
                })

                subscriptionsHistory.observe(viewLifecycleOwner, {
                    gpSubscribeAgain.isVisible = it.isNotEmpty()
                    if (it.isNotEmpty()) {
                        if (dataUsageAdapter.itemCount != 0) {
                            subscribeAdapter.submitList(it.take(1))
                        } else {
                            subscribeAdapter.submitList(it.take(2))
                        }
                    }
                })
            }
        }
    }

    private fun notifyViewPagerHeightChanged() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(RECALCULATE_VIEW_PAGER_HEIGHT_DELAY)

            // Send event to dynamically recalculate ViewPager height
            accountDetailsViewModel.onSubscriptionsDataLoaded(TAB_POSITION_DATA)
        }
    }

    override val logTag = "DataUsageFragment"

    override val analyticsScreenName = "account.data_usage"
}

private const val RECALCULATE_VIEW_PAGER_HEIGHT_DELAY = 100L
