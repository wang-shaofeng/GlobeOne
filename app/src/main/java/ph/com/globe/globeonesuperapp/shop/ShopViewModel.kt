/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.globe.inappupdate.remote_config.RemoteConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.globeonesuperapp.shop.ShopPagerAdapter.Companion.LOAD_ID
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.raffle.SET_AB
import ph.com.globe.model.shop.CatalogStatus
import ph.com.globe.util.fold
import ph.com.globe.util.toDateOrNull
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val authDomainManager: AuthDomainManager,
    private val shopDomainManager: ShopDomainManager,
    private val remoteConfigManager: RemoteConfigManager
) : BaseViewModel() {

    private val _loggedIn = MutableLiveData<Boolean>()
    val loggedIn: LiveData<Boolean> = _loggedIn

    private val _selectedLoadFragment = MutableLiveData<Long>()
    val selectedLoadFragment: LiveData<Long> = _selectedLoadFragment

    var isRaffleSetABInProgress = false
    private val _raffleSetABInProgress = MutableLiveData<Boolean>()
    val raffleSetABInProgress: LiveData<Boolean> = _raffleSetABInProgress

    private val _catalogStatus: MutableLiveData<CatalogStatus> = MutableLiveData()
    val catalogStatus: LiveData<CatalogStatus> = _catalogStatus

    private val _selectedTabId: MutableLiveData<Int> = MutableLiveData()
    val selectedTabId: LiveData<Int> = _selectedTabId

    private val _refreshEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val refreshEnabled: LiveData<Boolean> = _refreshEnabled

    private val _isRefreshingData: MutableLiveData<Boolean> = MutableLiveData()
    val isRefreshingData: LiveData<Boolean> = _isRefreshingData

    init {
        loggedIn()

        viewModelScope.launch {
            fetchOffers(forceRefresh = false)

            val currentTime = System.currentTimeMillis()
            val currentRaffleCampaigns = remoteConfigManager.getRafflesConfig()?.filter {
                it.startDate.toDateOrNull()?.time?.let { it < currentTime } == true &&
                        it.endDate.toDateOrNull()?.time?.let { currentTime < it } == true &&
                        it.name == SET_AB
            }

            isRaffleSetABInProgress = currentRaffleCampaigns?.isNotEmpty() ?: false
            _raffleSetABInProgress.value = isRaffleSetABInProgress
        }
    }

    fun fetchOffers(forceRefresh: Boolean) {
        // Set loading state
        _catalogStatus.value = CatalogStatus.Loading

        viewModelScope.launch {
            shopDomainManager.fetchOffers(forceRefresh).fold({
                _catalogStatus.value = CatalogStatus.Success
            }, {
                _catalogStatus.value = CatalogStatus.Error
            })

            // Disable refreshing state
            _isRefreshingData.value = false
        }
    }

    fun isLoggedIn(): Boolean = authDomainManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN

    fun loggedIn() {
        _loggedIn.value = isLoggedIn()
    }

    fun selectLoadFragment(index: Long) {
        if (index == ShopPagerAdapter.LOAD_INITIAL_ID || index == ShopPagerAdapter.LOAD_FULL_ID)
            _selectedLoadFragment.value = index
    }

    fun onPageSelected(position: Int) {
        if (position == LOAD_ID) {
            setRefreshEnabled(false)
        } else {
            _selectedTabId.value = position
        }
    }

    fun setRefreshEnabled(isEnabled: Boolean) {
        _refreshEnabled.value = isEnabled
    }

    override val logTag = "ShopViewModel"
}
