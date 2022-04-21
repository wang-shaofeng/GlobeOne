/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.loading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.util.fold
import java.io.Serializable
import javax.inject.Inject

@HiltViewModel
class GoCreateLoadingViewModel @Inject constructor(
    private val shopDomainManager: ShopDomainManager
) : BaseViewModel() {

    private val _loadCatalogResult = MutableLiveData<OneTimeEvent<LoadCatalogResult>>()
    val loadCatalogResult: LiveData<OneTimeEvent<LoadCatalogResult>> = _loadCatalogResult

    fun loadCatalog() {
        viewModelScope.launch {
            shopDomainManager.fetchOffers().fold({ allOffers ->
                _loadCatalogResult.value =
                    OneTimeEvent(LoadCatalogResult.LoadSuccessful(allOffers))
                dLog("Fetching shop offers success")

            }, {
                _loadCatalogResult.value = OneTimeEvent(LoadCatalogResult.LoadFailed)
                dLog("Fetching shop offers failure")
            })
        }
    }

    override val logTag = "GoCreateLoadingViewModel"
}

sealed class LoadingEntryPoint : Serializable {
    object LoadCatalog : LoadingEntryPoint()
}

sealed class LoadCatalogResult {
    data class LoadSuccessful(val catalogOffers: List<ShopItem>) : LoadCatalogResult()
    object LoadFailed : LoadCatalogResult()
}
