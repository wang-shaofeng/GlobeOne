/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.filter

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import ph.com.globe.analytics.AnalyticsScreen
import ph.com.globe.analytics.logger.GlobeAnalyticsLogger
import ph.com.globe.globeonesuperapp.R
import ph.com.globe.globeonesuperapp.databinding.FilterItemLayoutBinding
import ph.com.globe.globeonesuperapp.databinding.ShopPromoFilterFragmentBinding
import ph.com.globe.globeonesuperapp.utils.AppConstants.ALL_PROMOS_TAB
import ph.com.globe.globeonesuperapp.utils.analytics.AnalyticsEventsProvider
import ph.com.globe.globeonesuperapp.utils.setDarkStatusBar
import ph.com.globe.globeonesuperapp.utils.view_binding.NoBottomNavViewBindingFragment
import javax.inject.Inject

@AndroidEntryPoint
class ShopPromoFilterFragment : NoBottomNavViewBindingFragment<ShopPromoFilterFragmentBinding>(
    bindViewBy = {
        ShopPromoFilterFragmentBinding.inflate(it)
    }
), AnalyticsScreen {

    private val shopOffersSortFilterViewModel: ShopOffersSortFilterViewModel by navGraphViewModels(
        R.id.shop_subgraph
    ) { defaultViewModelProviderFactory }

    private val shopPromoFilterFragmentArgs by navArgs<ShopPromoFilterFragmentArgs>()

    private lateinit var budgetList: List<ShopItemFilter>
    private lateinit var validityList: List<ShopItemFilter>
    private lateinit var typeList: List<ShopItemFilter>
    private lateinit var functionList: List<ShopItemFilter>
    private lateinit var promotypeList: List<ShopItemFilter>

    @Inject
    override lateinit var analyticsLogger: GlobeAnalyticsLogger

    @Inject
    lateinit var analyticsEventsProvider: AnalyticsEventsProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analyticsLogger.logAnalyticsEvent(analyticsEventsProvider.provideScreenViewEvent("globe:app:filters screen"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabSelected = shopPromoFilterFragmentArgs.tabSelected

        setDarkStatusBar()

        with(viewBinding) {
            with(shopOffersSortFilterViewModel) {
                ivClose.setOnClickListener {
                    clearAllFilters(tabSelected)
                    findNavController().navigateUp()
                }

                budgetList = getFiltersList(FilterType.BUDGET_FILTER, tabSelected)
                setupFilterGroup(budgetList, cgBudgetFilter)

                validityList = getFiltersList(FilterType.VALIDITY_FILTER, tabSelected)
                setupFilterGroup(validityList, cgValidityFilter)

                typeList = getFiltersList(FilterType.TYPE_FILTER, tabSelected)
                setupFilterGroup(typeList, cgTypeFilter)

                functionList = getFiltersList(FilterType.FUNCTION_FILTER, tabSelected)
                setupFilterGroup(functionList, cgFunctionFilter)

                if (tabSelected == ALL_PROMOS_TAB) {
                    tvPromoTypeFilter.visibility = View.GONE
                    cgPromoTypeFilter.visibility = View.GONE
                } else {
                    promotypeList = getFiltersList(FilterType.PROMOTYPE_FILTER, tabSelected)
                    setupFilterGroup(promotypeList, cgPromoTypeFilter)
                }

                btnClearFilters.setOnClickListener {
                    clearAllFilters(tabSelected)
                }

                btnApplyFilter.setOnClickListener {
                    shopOffersSortFilterViewModel.applyFilters()
                }

                shopOffersSortFilterViewModel.applyFilters.observe(viewLifecycleOwner, {
                    it.handleEvent {
                        findNavController().navigateUp()
                    }
                })
            }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        findNavController().navigateUp()
                    }
                })
        }
    }

    private fun setupFilterGroup(
        filterList: List<ShopItemFilter>,
        chipGroup: ChipGroup
    ) {
        for (filter in filterList) {
            var position = filterList.indexOf(filter)
            var selected = filterList[position].selected
            val chip = FilterItemLayoutBinding.inflate(layoutInflater)

            val filterName = filter.title

            chip.tvFilter.text = filterName
            selectFilterItem(chip, selected)
            chip.clFilterLayout.setOnClickListener {
                position = filterList.indexOf(filter)
                selected = filterList[position].selected
                selectFilterItem(chip, !selected)
                shopOffersSortFilterViewModel.updateFilter(
                    filter,
                    !selected
                )
            }
            chipGroup.addView(chip.root)
        }
    }

    private fun selectFilterItem(chip: FilterItemLayoutBinding, selected: Boolean) {
        if (selected) chip.ivSelector.setImageResource(R.drawable.ic_blue_selected)
        else chip.ivSelector.setImageResource(R.drawable.ic_gray_broken_circle)
        chip.clFilterLayout.isSelected = selected
    }

    private fun clearAllFilters(tabSelected: Int) =
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

            for (filter in functionList) {
                val position = functionList.indexOf(filter)
                val chip = FilterItemLayoutBinding.bind(cgFunctionFilter.getChildAt(position))
                selectFilterItem(chip, false)
            }

            if (tabSelected != ALL_PROMOS_TAB) {
                for (filter in promotypeList) {
                    val position = promotypeList.indexOf(filter)
                    val chip = FilterItemLayoutBinding.bind(cgPromoTypeFilter.getChildAt(position))
                    selectFilterItem(chip, false)
                }
            }

            shopOffersSortFilterViewModel.clearFilters()
        }

    override val logTag = "ShopPromoFilterFragment"

    override val analyticsScreenName: String = "shop.promos_filter"

}
