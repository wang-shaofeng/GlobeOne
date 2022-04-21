/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.rewards

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.FilterItemLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.RewardsFilterFragmentBinding
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class RewardsFilterFragment :
    NoBottomNavViewBindingFragment<RewardsFilterFragmentBinding>(bindViewBy = {
        RewardsFilterFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    private val rewardsViewModel: RewardsViewModel by navGraphViewModels(R.id.rewards_subgraph) { defaultViewModelProviderFactory }

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:filters screen"))

        rewardsViewModel.removeUnnecessarilyCheckFlag()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setDarkStatusBar()

        with(viewBinding) {
            with(rewardsViewModel) {
                budgetFilters.map { it.toChip() }.forEach { cgBudgetFilter.addView(it) }
                subscriberTypeFilters.map { it.toChip() }
                    .forEach { cgSubscriberTypeFilter.addView(it) }

                btnApplyFilter.setOnClickListener {
                    applySelectedFilters()
                    findNavController().navigateUp()
                }

                btnClearFilters.setOnClickListener {
                    budgetFilters.forEach { it.checked = false }
                    subscriberTypeFilters.forEach { it.checked = false }
                    cgBudgetFilter.removeAllViews()
                    cgSubscriberTypeFilter.removeAllViews()
                    budgetFilters.map { it.toChip() }.forEach { cgBudgetFilter.addView(it) }
                    subscriberTypeFilters.map { it.toChip() }
                        .forEach { cgSubscriberTypeFilter.addView(it) }
                }
            }

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun RewardFilter.toChip(): View {
        val chip = FilterItemLayoutBinding.inflate(layoutInflater)
        chip.tvFilter.text = title
        chip.ivSelector.setImageResource(if (checked) R.drawable.ic_blue_selected else R.drawable.ic_gray_broken_circle)
        chip.clFilterLayout.isSelected = checked
        chip.root.setOnClickListener {
            checked = !checked
            chip.checkFilter(checked)
        }
        return chip.root
    }

    private fun FilterItemLayoutBinding.checkFilter(selected: Boolean) {
        if (selected) ivSelector.setImageResource(R.drawable.ic_blue_selected)
        else ivSelector.setImageResource(R.drawable.ic_gray_broken_circle)
        clFilterLayout.isSelected = selected
    }

    override val logTag = "RewardsFilterFragment"
    override val analyticsScreenName: String = "shop.rewards_filter"
}
