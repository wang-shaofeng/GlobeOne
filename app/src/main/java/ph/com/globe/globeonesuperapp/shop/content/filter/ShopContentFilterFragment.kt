/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.content.filter

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.FilterItemLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.ShopContentFilterFragmentBinding
import ph.com.globe.globeonesuperapp.shop.promo.filter.FilterType
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopItemFilter
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class ShopContentFilterFragment :
    NoBottomNavViewBindingFragment<ShopContentFilterFragmentBinding>(bindViewBy = {
        ShopContentFilterFragmentBinding.inflate(it)
    }), AnalyticsScreen {

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    private val viewModel: ShopContentFilterViewModel by navGraphViewModels(R.id.shop_subgraph) { defaultViewModelProviderFactory }

    private lateinit var budgetList: List<ShopItemFilter>
    private lateinit var validityList: List<ShopItemFilter>
    private lateinit var typeList: List<ShopItemFilter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:filters screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setDarkStatusBar()

        with(viewBinding) {

            budgetList = viewModel.getFiltersList(FilterType.BUDGET_FILTER)
            setupFilterGroup(budgetList, cgBudgetFilter)

            validityList = viewModel.getFiltersList(FilterType.VALIDITY_FILTER)
            setupFilterGroup(validityList, cgValidityFilter)

            typeList = viewModel.getFiltersList(FilterType.TYPE_FILTER)
            setupFilterGroup(typeList, cgTypeFilter)

            viewModel.initSelectedFilters()

            ivClose.setOnClickListener {
                findNavController().navigateUp()
            }

            btnApplyFilter.setOnClickListener {
                viewModel.applyFilters()
                findNavController().navigateUp()
            }

            btnClearFilters.setOnClickListener {
                clearAllFilters()
            }
        }
    }

    private fun setupFilterGroup(
        filterList: List<ShopItemFilter>,
        chipGroup: ChipGroup
    ) {
        for (filter in filterList) {
            var position = filterList.indexOf(filter)
            var selected = viewModel.checkFilterAppliedState(filterList[position])
            val chip = FilterItemLayoutBinding.inflate(layoutInflater)

            val filterName = filter.title

            chip.tvFilter.text = filterName
            selectFilterItem(chip, selected)
            chip.clFilterLayout.setOnClickListener {
                position = filterList.indexOf(filter)
                selected = viewModel.checkFilterSelectedState(filterList[position])
                selectFilterItem(chip, !selected)
                viewModel.updateFilter(filter.toFilterType(), position, !selected)
            }
            chipGroup.addView(chip.root)
        }
    }

    private fun selectFilterItem(chip: FilterItemLayoutBinding, selected: Boolean) {
        if (selected) chip.ivSelector.setImageResource(R.drawable.ic_blue_selected)
        else chip.ivSelector.setImageResource(R.drawable.ic_gray_broken_circle)
        chip.clFilterLayout.isSelected = selected
    }

    private fun clearAllFilters() =
        with(viewBinding) {
            for (filter in budgetList) {
                val position = budgetList.indexOf(filter)
                val chip = FilterItemLayoutBinding.bind(cgBudgetFilter.getChildAt(position))
                selectFilterItem(chip, false)
            }

            for (filter in validityList) {
                val position = validityList.indexOf(filter)
                val chip = FilterItemLayoutBinding.bind(cgValidityFilter.getChildAt(position))
                selectFilterItem(chip, false)
            }

            for (filter in typeList) {
                val position = typeList.indexOf(filter)
                val chip = FilterItemLayoutBinding.bind(cgTypeFilter.getChildAt(position))
                selectFilterItem(chip, false)
            }

            viewModel.clearFilters()
        }

    override val logTag = "ShopContentFilterFragment"
    override val analyticsScreenName: String="shop.content_filter"
}
