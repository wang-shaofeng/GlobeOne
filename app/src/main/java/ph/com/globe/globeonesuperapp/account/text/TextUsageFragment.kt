/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account.text

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.account.AccountDetailsViewModel
import ph.com.globe.globeonesuperapp.account.TAB_POSITION_TEXT
import ph.com.globe.globeonesuperapp.databinding.AccountDetailsConsumptionFragmentBinding
import ph.com.globe.globeonesuperapp.utils.view_binding.NestedViewBindingFragment

@AndroidEntryPoint
class TextUsageFragment :
    NestedViewBindingFragment<AccountDetailsConsumptionFragmentBinding>(bindViewBy = {
        AccountDetailsConsumptionFragmentBinding.inflate(it)
    }) {

    private val accountDetailsViewModel: AccountDetailsViewModel by navGraphViewModels(R.id.account_subgraph) { defaultViewModelProviderFactory }

    private val textUsageAdapter = TextUsageAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewBinding.rvConsumptionItems.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = textUsageAdapter
        }

        accountDetailsViewModel.textUsages.observe(viewLifecycleOwner) {
            textUsageAdapter.submitList(it)
            viewBinding.lavLoading.visibility = View.GONE
            viewBinding.incEmptyState.root.isVisible = it.isEmpty()

            // Send event to dynamically recalculate ViewPager height
            accountDetailsViewModel.onSubscriptionsDataLoaded(TAB_POSITION_TEXT)
        }
    }

    override val logTag = "AccountDetailsTextFragment"
}
