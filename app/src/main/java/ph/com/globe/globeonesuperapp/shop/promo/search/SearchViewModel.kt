/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.promo.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ph.com.globe.domain.rewards.RewardsDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.globeonesuperapp.rewards.odettePrioritySort
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.SearchItem
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val shopDomainManager: ShopDomainManager,
    private val rewardsDomainManager: RewardsDomainManager
) : BaseViewModel() {

    private val _items = MutableStateFlow<List<SearchItem>>(emptyList())

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: LiveData<List<String>> = _searchHistory.asLiveData(Dispatchers.Default)

    private val _searchedOffers = _items.combine(_searchHistory) { items, history ->
        if (history.isEmpty()) arrayListOf()
        else items.filter { it.name.contains(history.last(), true) }
            // TODO temporary Odette priority sort
            .odettePrioritySort()
    }
    val searchedOffers: LiveData<List<SearchItem>> = _searchedOffers.asLiveData(Dispatchers.Default)

    private val _recommendString = MutableStateFlow("")

    private val _searchRecommendation = _items.combine(_recommendString) { items, string ->
        if (string.length > 2)
            items.filter { it.name.contains(string, true) }.map { it.name }
        else emptyList()
    }
    val searchRecommendation: LiveData<List<String>> =
        _searchRecommendation.asLiveData(Dispatchers.Default)

    private val _unavailableOffersList = MutableLiveData<List<SearchItem>>()
    val unavailableOffersList: LiveData<List<SearchItem>> = _unavailableOffersList

    private var searchHistoryCleared = false

    fun searchByName(search: String) {
        viewModelScope.launch(Dispatchers.Default) {
            if (searchHistoryCleared) {
                searchHistoryCleared = false
                _searchHistory.emit(mutableListOf(search))
            } else {
                var searchList = _searchHistory.firstOrNull()?.toMutableList()
                if (searchList == null) {
                    searchList = mutableListOf(search)
                } else {
                    if (searchList.contains(search)) searchList.remove(search)
                    else if (searchList.size == 5) searchList.removeFirst()

                    searchList.add(search)
                }
                _searchHistory.emit(searchList)
            }
        }
    }

    fun clearSearchHistory() {
        searchHistoryCleared = true
    }

    fun recommendSearch(search: String) {
        viewModelScope.launch(Dispatchers.Default) {
            _recommendString.emit(search)
        }
    }

    fun initForRewards() {
        viewModelScope.launch(Dispatchers.Default) {
            rewardsDomainManager.getAllRewards().collect { _items.emit(it) }
        }
    }

    fun initForLoanable(loanable: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            (if (loanable) shopDomainManager.getLoanable() else shopDomainManager.getPromos()).collect {
                _items.emit(it)
            }
        }
    }

    override val logTag = "SearchViewModel"
}
