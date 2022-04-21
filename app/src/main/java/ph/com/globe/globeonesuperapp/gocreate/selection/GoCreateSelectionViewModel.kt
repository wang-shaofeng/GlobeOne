/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.shop.domain_models.ShopItem
import ph.com.globe.model.shop.domain_models.UNLIMITED_CALLS_DESCRIPTION
import ph.com.globe.model.util.GB
import javax.inject.Inject

@HiltViewModel
class GoCreateSelectionViewModel @Inject constructor() : BaseViewModel() {

    private val _selectedConfiguration = MutableStateFlow(GoCreateOfferConfiguration())

    private val _goCreateOffers = MutableStateFlow<List<ShopItem>>(emptyList())

    val matchedOffer: LiveData<ShopItem?> = _goCreateOffers
        .combine(_selectedConfiguration) { offers, configuration ->
            if (configuration.validity == 0 || configuration.accessData == 0)
                return@combine null

            offers.filter { it.matchGoCreateConfiguration(configuration) }
                .takeIf { it.size == 1 }?.first()
        }.asLiveData(Dispatchers.Default)

    private val goWatchIcons: List<String> = listOf(
        "https://new.globe.com.ph/assets/files/media/YouTube.png",
        "https://new.globe.com.ph/assets/files/media/Netflix.png",
        "https://new.globe.com.ph/assets/files/media/VIU.png",
        "https://new.globe.com.ph/assets/files/media/iWantTFC.png",
        "https://new.globe.com.ph/assets/files/media/HBOGO.png",
        "https://new.globe.com.ph/assets/files/media/NBA.png",
        "https://new.globe.com.ph/assets/files/media/WeTV.png",
        "https://new.globe.com.ph/assets/files/media/Kumu.png"
    )
    private val goPlayIcons: List<String> = listOf(
        "https://new.globe.com.ph/assets/files/media/MobileLegends.png",
        "https://new.globe.com.ph/assets/files/media/ClashOfClans.png",
        "https://new.globe.com.ph/assets/files/media/PUBG.png",
        "https://new.globe.com.ph/assets/files/media/CallOfDutyMobile.png",
        "https://new.globe.com.ph/assets/files/media/MUOrigin2.png",
        "https://new.globe.com.ph/assets/files/media/WildRift.png",
        "https://new.globe.com.ph/assets/files/media/LegendsOfRuneterra.png",
        "https://new.globe.com.ph/assets/files/media/Twitch.png"
    )
    private val goShareIcons: List<String> = listOf(
        "https://new.globe.com.ph/assets/files/media/FacebookSquare.png",
        "https://new.globe.com.ph/assets/files/media/InstagramSquare.png",
        "https://new.globe.com.ph/assets/files/media/TiktokSquare.png",
        "https://new.globe.com.ph/assets/files/media/TwitterSquare.png",
        "https://new.globe.com.ph/assets/files/media/Kumu.png",
        "https://new.globe.com.ph/assets/files/media/Snapchat.png",
        "https://new.globe.com.ph/assets/files/media/Houseparty.png"
    )

    private val boosterGoCreateItems: List<GoCreateBoosterItem> = listOf(
        GoCreateBoosterItem("GoWATCH", goWatchIcons, true),
        GoCreateBoosterItem("GoPLAY", goPlayIcons),
        GoCreateBoosterItem("GoSHARE", goShareIcons),
    )

    private val _goCreateBoosters = MutableLiveData(boosterGoCreateItems)
    val goCreateBoosters: LiveData<List<GoCreateBoosterItem>> = _goCreateBoosters

    fun setGoCreateOffers(offers: List<ShopItem>) {
        viewModelScope.launch {
            _goCreateOffers.emit(offers)
        }
    }

    fun selectValidity(value: Int) {
        updateSelectedConfiguration(_selectedConfiguration.value.copy(validity = value))
    }

    fun selectAllAccessData(value: Int) {
        updateSelectedConfiguration(_selectedConfiguration.value.copy(accessData = value))
    }

    fun selectAppData(value: Int) {
        updateSelectedConfiguration(_selectedConfiguration.value.copy(appBoosterData = value))
    }

    fun selectGoCreateBooster(item: GoCreateBoosterItem?) {
        updateSelectedConfiguration(_selectedConfiguration.value.copy(appBooster = item))
        item?.let {
            _goCreateBoosters.value = boosterGoCreateItems.map {
                it.copy(
                    selected = it.title == item.title
                )
            }
        }
    }

    fun changeUnlimitedCallsState(): Boolean {
        val unlimitedCalls = _selectedConfiguration.value.unlimitedCalls
        val newState = !unlimitedCalls
        updateSelectedConfiguration(_selectedConfiguration.value.copy(unlimitedCalls = newState))
        return newState
    }

    fun getSelectedConfiguration() = _selectedConfiguration.value

    private fun updateSelectedConfiguration(configuration: GoCreateOfferConfiguration) {
        viewModelScope.launch {
            _selectedConfiguration.emit(configuration)
        }
    }

    private fun ShopItem.matchGoCreateConfiguration(configuration: GoCreateOfferConfiguration): Boolean {
        val matchValidity = validity?.days == configuration.validity
        val matchAccessData = mobileDataSize.firstOrNull()?.let { dataAmount ->
            (dataAmount / GB).toInt() == configuration.accessData
        } ?: false

        val matchAppBooster = configuration.appBooster?.let { booster ->
            val matchType =
                appDataDescription.firstOrNull()?.lowercase()?.contains(booster.title.lowercase())
                    ?: false
            val matchAppData =
                appDataSize.firstOrNull()?.let { dataAmount ->
                    (dataAmount / GB).toInt() == configuration.appBoosterData
                } ?: false

            return@let matchType && matchAppData
        } ?: !isAnyAppService

        val unlimitedCalls = callDescription.firstOrNull() == UNLIMITED_CALLS_DESCRIPTION
        val matchUnlimitedCalls = unlimitedCalls == configuration.unlimitedCalls

        return matchValidity
                && matchAccessData
                && matchAppBooster
                && matchUnlimitedCalls
    }

    override val logTag = "GoCreateSelectionViewModel"
}

data class GoCreateOfferConfiguration(
    val validity: Int = 0,
    val accessData: Int = 0,
    val appBooster: GoCreateBoosterItem? = null,
    val appBoosterData: Int = 0,
    val unlimitedCalls: Boolean = false
)

data class GoCreateBoosterItem(
    val title: String,
    val icons: List<String>,
    var selected: Boolean = false,
)
