/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.filter

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.globeonesuperapp.shop.promo.filter.FilterType.*
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopItemFilter.*
import ph.com.globe.globeonesuperapp.shop.promo.filter.ShopItemFilter.BudgetFilter.Companion.UNLIMITED
import ph.com.globe.globeonesuperapp.shop.promo.filter.SortType.NONE
import ph.com.globe.globeonesuperapp.utils.AppConstants.ALL_PROMOS_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.GLOBE_PREPAID_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.HOME_WIFI_PREPAID_TAB
import ph.com.globe.globeonesuperapp.utils.AppConstants.TM_TAB
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.shop.GetPromoSubscriptionHistoryParams
import ph.com.globe.model.shop.PromoSubscriptionHistoryItem
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.Validity
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.shop.domain_models.isBrandCorrect
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class ShopOffersSortFilterViewModel @Inject constructor(
    private val shopDomainManager: ShopDomainManager,
    private val authDomainManager: AuthDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

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

    private val functionFilters = listOf(
        FunctionFilter("mobile_data_allocation", "Mobile Data"),
        FunctionFilter("home_data_allocation", "Home Data"),
        FunctionFilter("app_data_allocation", "App Data"),
        FunctionFilter("voice_allocation", "Calls"),
        FunctionFilter("sms_allocation", "Text"),
        FunctionFilter("booster_allocation", "Booster")
    )

    private val promoTypeFilters = listOf(
        PromoTypeFilter("Go", "Go", listOf(GLOBE_PREPAID_TAB)),
        PromoTypeFilter("GoSURF", "GoSURF", listOf(GLOBE_PREPAID_TAB, HOME_WIFI_PREPAID_TAB)),
        PromoTypeFilter("GoSAKTO", "GoSAKTO", listOf(GLOBE_PREPAID_TAB)),
        PromoTypeFilter("GoUNLI", "GoUNLI", listOf(GLOBE_PREPAID_TAB)),
        PromoTypeFilter("SURF4ALL", "SURF4ALL"),
        PromoTypeFilter("GoBooster", "GoBooster", listOf(GLOBE_PREPAID_TAB)),
        PromoTypeFilter("EasySURF", "EasySURF", listOf(TM_TAB)),
        PromoTypeFilter("FunSagad", "FunSagad", listOf(TM_TAB)),
        PromoTypeFilter("All-In", "All-In", listOf(TM_TAB)),
        PromoTypeFilter("Big-A-TEN", "Big-A-TEN", listOf(TM_TAB)),
        PromoTypeFilter("EasyPLAN", "EasyPLAN", listOf(TM_TAB)),
        PromoTypeFilter("Combo", "Combo", listOf(TM_TAB)),
        PromoTypeFilter("Best Deals", "Best Deals", listOf(TM_TAB)),
        PromoTypeFilter("Surf", "Surf", listOf(TM_TAB)),
        PromoTypeFilter("HomeSURF", "HomeSURF", listOf(HOME_WIFI_PREPAID_TAB)),
        PromoTypeFilter("HomeWATCH", "HomeWATCH", listOf(HOME_WIFI_PREPAID_TAB)),
        PromoTypeFilter("MyBizSurf", "MyBizSurf", listOf(HOME_WIFI_PREPAID_TAB)),
        PromoTypeFilter("MySchoolSurf", "MySchoolSurf", listOf(HOME_WIFI_PREPAID_TAB))
    )

    private val _promosCurrentTab: MutableStateFlow<Int> = MutableStateFlow(-1)
    private val _borrowCurrentTab: MutableStateFlow<Int> = MutableStateFlow(-1)

    private val _selectedNumberBrand: MutableStateFlow<AccountBrand?> = MutableStateFlow(null)
    private val _isNotAllTab: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var numberOfFiltersApplied = 0
    private val selectedFilters = mutableListOf<ShopItemFilter>()

    private val _applyFilters = MutableLiveData<OneTimeEvent<Unit>>()
    val applyFilters: LiveData<OneTimeEvent<Unit>> = _applyFilters

    private val _numberOfFilters = MutableLiveData(numberOfFiltersApplied)
    val numberOfFilters: LiveData<Int> = _numberOfFilters

    private val _selectedFiltersFlow = MutableSharedFlow<List<ShopItemFilter>>(1)
        .also { it.tryEmit(selectedFilters) }
    private val _promoSortType = MutableStateFlow(NONE)

    private val _promoAllOffersList = shopDomainManager.getPromos().sort(_promoSortType)

    val promosOffersLists = _promoAllOffersList
        .combine(_selectedNumberBrand) { list, brand ->
            list.filter { it.isBrandCorrect(brand) || brand == null }
        }.map {
            it.splitOnSectionsWithMax8Items()
        }.asLiveData(Dispatchers.Default)

    val promosFilteredOffersList =
        _promoAllOffersList
            .combine(_promosCurrentTab) { list, tab ->
                list.filter {
                    when (tab) {
                        GLOBE_PREPAID_TAB -> it.isGlobePrepaid || it.isPostpaid
                        TM_TAB -> it.isTM || it.isTMMyFi
                        HOME_WIFI_PREPAID_TAB -> it.isHomePrepaidWifi
                        ALL_PROMOS_TAB -> true
                        else -> false
                    }
                }
            }
            .combine(_selectedNumberBrand) { list, brand ->
                list.filter {
                    it.isBrandCorrect(brand) || brand == null
                }
            }
            .combine(_selectedFiltersFlow) { list, filters ->
                list to filters.isNotEmpty()
            }
            .flatMapLatest {
                if (boosterFilterApplied) flow { ShopUiModel(null, null) }
                else if (it.second) {
                    flow { emit(it.first) }
                        .applyFilters(_selectedFiltersFlow)
                        .map {
                            ShopUiModel(null, it)
                        }
                } else {
                    flow { emit(it.first.splitOnSectionsWithMax8Items()) }
                        .map {
                            ShopUiModel(it, null)
                        }
                }
            }.asLiveData(Dispatchers.IO)

    private val _loanableSortType = MutableStateFlow(NONE)
    private val _loanableAllOffersList =
        shopDomainManager.getLoanable().sort(_loanableSortType)

    val loanableAllOffersList: LiveData<List<ShopItem>> =
        _loanableAllOffersList.combine(_borrowCurrentTab) { list, tab ->
            list.filter {
                when (tab) {
                    ALL_PROMOS_TAB -> true
                    else -> false
                }
            }
        }.asLiveData(Dispatchers.Default)

    val loanableOffersLists = _loanableAllOffersList
        .combine(_selectedNumberBrand) { list, brand ->
            list.filter { it.isBrandCorrect(brand) || brand == null }
        }.map {
            it.splitOnSectionsWithMax8Items()
        }.asLiveData(Dispatchers.Default)

    val loanableFilteredOffersLists = _loanableAllOffersList
        .combine(_borrowCurrentTab) { list, tab ->
            list.filter {
                when (tab) {
                    GLOBE_PREPAID_TAB -> it.isGlobePrepaid || it.isPostpaid
                    TM_TAB -> it.isTM || it.isTMMyFi
                    else -> false
                }
            }
        }
        .combine(_selectedNumberBrand) { list, brand ->
            list.filter {
                it.isGlobePrepaid && brand == AccountBrand.GhpPrepaid || it.isPostpaid && brand == AccountBrand.GhpPostpaid
                        || (it.isTM || it.isTMMyFi) && brand == AccountBrand.Tm || brand == null
            }
        }
        .map {
            it.splitOnSectionsWithMax8Items()
        }.asLiveData(Dispatchers.Default)

    val unavailablePromosList: LiveData<List<ShopItem>> =
        _promoAllOffersList.combine(_promosCurrentTab) { list, tab ->
            list.filter {
                when (tab) {
                    GLOBE_PREPAID_TAB -> it.isGlobePrepaid || it.isPostpaid
                    TM_TAB -> it.isTM || it.isTMMyFi
                    HOME_WIFI_PREPAID_TAB -> it.isHomePrepaidWifi
                    ALL_PROMOS_TAB -> true
                    else -> false
                }
            }.take(1)
        }.combine(_isNotAllTab) { list, isNotAllTab ->
            list.filter { isNotAllTab }
        }.combine(_selectedNumberBrand) { list, brand ->
            list.filter { it.isBrandCorrect(brand) && brand != null }
        }.asLiveData(Dispatchers.Default)

    val unavailableLoansList: LiveData<List<ShopItem>> =
        _loanableAllOffersList.combine(_borrowCurrentTab) { list, tab ->
            list.filter {
                when (tab) {
                    GLOBE_PREPAID_TAB -> it.isGlobePrepaid || it.isPostpaid
                    TM_TAB -> it.isTM || it.isTMMyFi
                    else -> false
                }
            }.take(1)
        }.combine(_isNotAllTab) { list, isNotAllTab ->
            list.filter { isNotAllTab }
        }.combine(_selectedNumberBrand) { list, brand ->
            list.filter {
                (it.isGlobePrepaid && brand != AccountBrand.GhpPrepaid || it.isPostpaid && brand == AccountBrand.GhpPostpaid
                        || (it.isTM || it.isTMMyFi) && brand != AccountBrand.Tm) && brand != null
            }
        }.asLiveData(Dispatchers.Default)

    val filteredPromosAllOffersList: LiveData<List<ShopItem>> =
        _promoAllOffersList
            .applyFilters(_selectedFiltersFlow)
            .asLiveData(Dispatchers.Default)

    private val _businessFilterAdded = MutableLiveData<OneTimeEvent<Unit>>()
    val businessFilterAdded: LiveData<OneTimeEvent<Unit>> = _businessFilterAdded

    val promoActivePage = combine(_selectedFiltersFlow, _promosCurrentTab) { filters, tab ->
        ShopTabUiModel(filters.isNotEmpty(), tab)
    }.asLiveData(Dispatchers.Default)

    val borrowActivePage = _borrowCurrentTab.asLiveData(Dispatchers.Default)

    private val _showAllBoosters: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _subscriptionHistory = MutableStateFlow(emptyList<PromoSubscriptionHistoryItem>())
    val boostersForUser = _promoAllOffersList
        .combine(_promosCurrentTab) { list, tab ->
            list.filter {
                when (tab) {
                    GLOBE_PREPAID_TAB -> it.isGlobePrepaid
                    TM_TAB -> it.isTM
                    HOME_WIFI_PREPAID_TAB -> it.isHomePrepaidWifi
                    else -> false
                }
            }
        }.combine(_subscriptionHistory) { allOffers, subscriptionHistory ->
            val boosters = mutableListOf<BoosterDetailsItem>()
            var boosterIds = mutableListOf<String>()
            subscriptionHistory.filter { it.status == "Active" }.forEach { item ->
                allOffers.find { it.chargePromoId == item.serviceId }?.let {
                    it.boosters?.let {
                        boosterIds.addAll(it)
                        boosterIds = boosterIds.distinct().toMutableList()
                    }
                }
            }

            boosterIds.forEach { id ->
                boosters.addAll(allOffers.filter {
                    it.sections.getOrNull(0)?.id == id
                            || it.sections.getOrNull(1)?.id == id
                            || it.sections.getOrNull(2)?.id == id
                }.map { item ->
                    val sectionName =
                        if (allOffers.any { offer -> offer.sections.any { section -> item.sections.any { section.booster ?: "" == it.id } } })
                            allOffers.first { offer -> offer.sections.any { section -> item.sections.any { section.booster ?: "" == it.id } } }.sections.first { section -> item.sections.any { section.booster ?: "" == it.id } }.name
                        else ""

                    BoosterDetailsItem(
                        item.chargePromoId,
                        item.nonChargePromoId,
                        item.chargeServiceParam,
                        item.nonChargeServiceParam,
                        item.name,
                        item.price,
                        item.discount,
                        item.validity,
                        item.displayColor,
                        item.boosterAllocation,
                        item.description,
                        sectionName,
                        item.shareable,
                        item.shareKeyword,
                        item.shareFee,
                        item.skelligWallet,
                        item.skelligCategory,
                        item.apiSubscribe,
                        Applications(
                            item.applicationService?.description,
                            item.applicationService?.apps?.map {
                                AppDetailsItem(
                                    it.appIcon,
                                    it.appName
                                )
                            })
                    )
                })
            }

            boosters.toList()
        }.combine(_showAllBoosters) { boosters, show ->
            if (show) boosters else boosters.take(3)
        }.asLiveData(Dispatchers.Default)

    var boosterFilterApplied = false
    private val _hidePromos = MutableLiveData<OneTimeEvent<Boolean>>()
    val hidePromos: LiveData<OneTimeEvent<Boolean>> = _hidePromos

    private fun isLoggedIn(): Boolean =
        authDomainManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN

    fun getPromoSubscriptionHistory(mobileNumber: String) {
        if (isLoggedIn())
            viewModelScope.launchWithLoadingOverlay(handler) {
                shopDomainManager.getPromoSubscriptionHistory(
                    GetPromoSubscriptionHistoryParams(mobileNumber)
                ).fold(
                    { response ->
                        _subscriptionHistory.emit(response.result.subscriptions)
                    },
                    {
                        _subscriptionHistory.emit(emptyList())
                    }
                )
            }
        else
            viewModelScope.launch { _subscriptionHistory.emit(emptyList()) }
    }

    fun setBrand(brand: AccountBrand?) {
        viewModelScope.launch { _selectedNumberBrand.emit(brand) }
    }

    fun getFiltersList(section: FilterType, selectedTab: Int) =
        section.getFiltersFromFilterType().filter { it.tabs.contains(selectedTab) }

    fun updateFilter(filterItem: ShopItemFilter, selected: Boolean, addBooster: Boolean = false) {
        boosterFilterApplied = addBooster
        filterItem.selected = selected
        if (selected) {
            selectedFilters.add(filterItem)
            numberOfFiltersApplied++
        } else {
            selectedFilters.remove(filterItem)
            numberOfFiltersApplied--
        }
    }

    fun addBoosterFilter() {
        updateFilter(FUNCTION_FILTER.getFiltersFromFilterType()[BOOSTER_FILTER_INDEX], true, addBooster = true)
        boosterFilterApplied = true
        viewModelScope.launch {
            _selectedFiltersFlow.emit(selectedFilters.toList())
            _hidePromos.value = OneTimeEvent(boosterFilterApplied)
            _showAllBoosters.emit(boosterFilterApplied)
        }
        _numberOfFilters.value = numberOfFiltersApplied
    }

    fun clearFilters() {
        for (filter in budgetFilters) filter.selected = false
        for (filter in validityFilters) filter.selected = false
        for (filter in typeFilters) filter.selected = false
        for (filter in functionFilters) filter.selected = false
        for (filter in promoTypeFilters) filter.selected = false

        numberOfFiltersApplied = 0
        _numberOfFilters.value = numberOfFiltersApplied

        selectedFilters.clear()
        boosterFilterApplied = false
        viewModelScope.launch {
            _selectedFiltersFlow.emit(selectedFilters.toList())
            _hidePromos.value = OneTimeEvent(boosterFilterApplied)
            _showAllBoosters.emit(boosterFilterApplied)
        }
    }

    fun sortLoanable(selectedSort: SortType) {
        viewModelScope.launch(Dispatchers.Default) { _loanableSortType.emit(selectedSort) }
    }

    fun sortPromos(selectedSort: SortType) {
        viewModelScope.launch(Dispatchers.Default) { _promoSortType.emit(selectedSort) }
    }

    fun applyFilters() {
        viewModelScope.launch {
            _selectedFiltersFlow.emit(selectedFilters.toList())
            _hidePromos.value = OneTimeEvent(boosterFilterApplied)
            _showAllBoosters.emit(boosterFilterApplied)
        }
        _applyFilters.value = OneTimeEvent(Unit)
        _numberOfFilters.value = numberOfFiltersApplied
    }

    fun setPromosCurrentTab(currentTab: Int) {
        viewModelScope.launch {
            _promosCurrentTab.emit(currentTab)
            _isNotAllTab.emit(currentTab != 0)
        }
    }

    fun setBorrowCurrentTab(currentTab: Int) {
        viewModelScope.launch {
            _borrowCurrentTab.emit(currentTab)
            _isNotAllTab.emit(currentTab != 0)
        }
    }

    private fun FilterType.getFiltersFromFilterType() =
        when (this) {
            BUDGET_FILTER -> budgetFilters
            VALIDITY_FILTER -> validityFilters
            TYPE_FILTER -> typeFilters
            FUNCTION_FILTER -> functionFilters
            PROMOTYPE_FILTER -> promoTypeFilters
            else -> emptyList()
        }

    override val logTag = "ShopPromoSortFilterViewModel"
}

private const val BOOSTER_FILTER_INDEX = 5

data class ShopUiModel(
    val sections: List<Section>?,
    val filteredList: List<ShopItem>?
)

data class ShopTabUiModel(
    val hasFilters: Boolean,
    val currentTab: Int
)

@Parcelize
data class BoosterDetailsItem(
    val chargePromoId: String,
    val nonChargePromoId: String,
    val chargeServiceParam: String,
    val nonChargeServiceParam: String,
    val name: String,
    val price: String,
    val discount: String?,
    val validity: Validity?,
    val displayColor: String,
    val data: List<Long>?,
    val dataDescription: String,
    val forPromo: String,
    val shareable: Boolean,
    val shareKeyword: String?,
    val shareFee: String?,
    val skelligWallet: String?,
    val skelligCategory: String?,
    val apiSubscribe: String,
    val applicationService: Applications?,
) : Parcelable

@Parcelize
data class Applications(
    val description: String?,
    val apps: List<AppDetailsItem>?
) : Parcelable

@Parcelize
data class AppDetailsItem(
    val appIcon: String,
    val appName: String
) : Parcelable
