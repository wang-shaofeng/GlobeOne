/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.dashboard.banners

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.domain.banners.BannersDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.banners.BannerModel
import javax.inject.Inject

@HiltViewModel
class BannersViewModel @Inject constructor(
    private val bannersDomainManager: BannersDomainManager
) : BaseViewModel() {

    val banners: LiveData<List<BannerModel>> =
        bannersDomainManager.getDashboardBanners().asLiveData()

    init {
        fetchBanners()
    }

    fun fetchBanners() {
        viewModelScope.launch {
            bannersDomainManager.fetchBanners()
        }
    }

    override val logTag = "BannersViewModel"
}
