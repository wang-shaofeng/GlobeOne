/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.calls

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.account.TAB_POSITION_CALLS
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsConsumptionFragmentBinding
import ph.com.globe.globeonesuperapp.utils.getRefreshDate
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment
import ph.com.globe.model.profile.domain_models.isPostpaidMobile

@AndroidEntryPoint
class CallsUsageFragment :
    NestedViewBindingFragment<AccountDetailsConsumptionFragmentBinding>(bindViewBy = {
        AccountDetailsConsumptionFragmentBinding.inflate(it)
    }) {

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }
    private val callsUsageViewModel: CallsUsageViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val callsUsageAdapter by lazy {
        CallsUsageAdapter(accountDetailsViewModel.selectedEnrolledAccount)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(viewBinding) {
            with(accountDetailsViewModel) {

                rvConsumptionItems.apply {
                    layoutManager = GridLayoutManager(requireContext(), 2)
                    adapter = callsUsageAdapter
                }

                callsUsages.observe(viewLifecycleOwner) { usagesList ->
                    callsUsageAdapter.submitList(usagesList)
                    lavLoading.visibility = View.GONE

                    notifyViewPagerHeightChanged()

                    if (selectedEnrolledAccount.isPostpaidMobile()) {
                        if (usagesList == null) {
                            with(callsUsageViewModel) {
                                getMobilePlanDetails(selectedEnrolledAccount.primaryMsisdn)

                                planName.observe(viewLifecycleOwner) { planName ->
                                    showOfferDescriptionCard(planName)
                                }
                            }
                        } else {
                            postpaidOfferDescription.observe(viewLifecycleOwner) { offerDescription ->
                                showOfferDescriptionCard(offerDescription)
                            }
                        }
                    } else {
                        incEmptyState.root.isVisible = usagesList?.isEmpty() ?: false
                    }
                }

                billingDetails.observe(viewLifecycleOwner) { billingDetails ->
                    val refreshDate = billingDetails.getRefreshDate()
                    incCallsAndTextOffer.tvRefreshDate.text =
                        getString(R.string.refreshes_on, refreshDate)
                    callsUsageAdapter.updateItemsRefreshDate(refreshDate)
                }
            }
        }
    }

    private fun notifyViewPagerHeightChanged() {
        // Send event to dynamically recalculate ViewPager height
        accountDetailsViewModel.onSubscriptionsDataLoaded(TAB_POSITION_CALLS)
    }

    private fun showOfferDescriptionCard(offerDescription: String) {
        viewBinding.incCallsAndTextOffer.apply {
            tvOfferDescription.text = offerDescription
            root.visibility = View.VISIBLE
        }
        notifyViewPagerHeightChanged()
    }

    override val logTag = "AccountDetailsCallsFragment"
}
