/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.content.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.globeonesuperapp.shop.promo.filter.*
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopItemFilter.*
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopItemFilter.BudgetFilter.Companion.UNLIMITED
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.shop.domain_models.isBrandCorrect
import javax.inject.Inject

@HiltViewModel
class ShopContentFilterViewModel @Inject constructor(
    shopDomainManager: ShopDomainManager
) : BaseViewModel() {

    private val budgetFilters = listOf(
        BudgetFilter(10, 50, "P10-P50"),
        BudgetFilter(51, 100, "P51-P100"),
        BudgetFilter(101, 250, "P101-P250"),
        BudgetFilter(251, 500, "P251-P500"),
        BudgetFilter(501, 750, "P501-P750"),
        BudgetFilter(751, UNLIMITED, "P751 and above")
    )

    private val validityFilters = listOf(
        ValidityFilter("1", "1 day"),
        ValidityFilter("2", "2 days"),
        ValidityFilter("3", "3 days"),
        ValidityFilter("5", "5 days"),
        ValidityFilter("7", "7 days"),
        ValidityFilter("15", "15 days"),
        ValidityFilter("30", "30 days")
    )

    private val typeFilters = listOf(
        TypeFilter("new", "New"),
        TypeFilter("limited", "Limited"),
        TypeFilter("discounted", "Discounted")
    )

    private val appliedFilters = mutableListOf<ShopItemFilter>()

    private val selectedFilters = mutableListOf<ShopItemFilter>()

    private val _numberOfFilters = MutableLiveData(appliedFilters.size)
    val numberOfFilters: LiveData<Int> = _numberOfFilters

    private val _selectedFiltersFlow = MutableSharedFlow<List<ShopItemFilter>>(1)
        .also { it.tryEmit(appliedFilters) }

    private val _contentPromoSortType = MutableStateFlow(SortType.NONE)
    private val _selectedNumberBrand:MutableStateFlow<AccountBrand?> = MutableStateFlow(null)

    private val _contentPromoOffersList =
        shopDomainManager.getContentPromos().sort(_contentPromoSortType)

    val filteredContentPromoOffersList =
        _contentPromoOffersList
            .combine(_selectedNumberBrand) { list, brand ->
                list.filter {
                    it.isBrandCorrect(brand) || brand == null
                }
            }
            .applyFilters(_selectedFiltersFlow)
            .asLiveData(Dispatchers.Default)

    fun sortContentPromos(selectedSort: SortType) {
        viewModelScope.launch(Dispatchers.Default) { _contentPromoSortType.emit(selectedSort) }
    }

    fun setNumberBrand(brand: AccountBrand?) {
        viewModelScope.launch { _selectedNumberBrand.emit(brand) }
    }

    fun getFiltersList(section: FilterType) = section.getFiltersFromFilterType()

    fun initSelectedFilters() {
        selectedFilters.clear()
        selectedFilters.addAll(appliedFilters)
    }

    fun checkFilterAppliedState(filter: ShopItemFilter): Boolean = appliedFilters.contains(filter)

    fun checkFilterSelectedState(filter: ShopItemFilter): Boolean = selectedFilters.contains(filter)

    fun updateFilter(section: FilterType, position: Int, selected: Boolean) {
        section.getFiltersFromFilterType()[position].let { filterItem ->
            if (selected) {
                selectedFilters.add(filterItem)
            } else {
                selectedFilters.remove(filterItem)
            }
        }
    }

    fun applyFilters() {
        appliedFilters.clear()
        appliedFilters.addAll(selectedFilters)

        for (filter in budgetFilters) filter.selected = appliedFilters.contains(filter)
        for (filter in validityFilters) filter.selected = appliedFilters.contains(filter)
        for (filter in typeFilters) filter.selected = appliedFilters.contains(filter)

        viewModelScope.launch { _selectedFiltersFlow.emit(appliedFilters.toList()) }
        _numberOfFilters.value = appliedFilters.size
    }

    fun clearFilters() {
        selectedFilters.clear()
        applyFilters()
    }

    private fun FilterType.getFiltersFromFilterType() =
        when (this) {
            FilterType.BUDGET_FILTER -> budgetFilters
            FilterType.VALIDITY_FILTER -> validityFilters
            FilterType.TYPE_FILTER -> typeFilters
            else -> emptyList()
        }

    override val logTag = "ShopContentFilterViewModel"
}
