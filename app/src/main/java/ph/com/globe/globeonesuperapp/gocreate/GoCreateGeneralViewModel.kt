/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.gocreate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.errors.GeneralError
import ph.com.globe.globeonesuperapp.gocreate.select_account.GO_CREATE_ELIGIBLE_BRAND
import ph.com.globe.globeonesuperapp.gocreate.selection.GoCreateOfferConfiguration
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.model.shop.domain_models.ShopItem
import javax.inject.Inject

@HiltViewModel
class GoCreateGeneralViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager,
    private val shopDomainManager: ShopDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _catalogOffers = MutableStateFlow<List<ShopItem>>(emptyList())

    init {
        viewModelScope.launch {
            shopDomainManager.getAllOffers(false).collect { allOffers ->
                updateCatalogOffers(allOffers)
            }
        }
    }

    val isCatalogCached: Boolean get() = _catalogOffers.value.isNotEmpty()
    val goCreateOffers: List<ShopItem> get() = _catalogOffers.value.filter { it.isGoCreate }

    val isMobileNumberSelected: Boolean get() = _mobileNumber.value != null

    private val _entryPointTitle = MutableLiveData<String>()
    val entryPointTitle: LiveData<String> = _entryPointTitle

    private val _mobileNumber = MutableLiveData<String>()
    val mobileNumber: LiveData<String> = _mobileNumber

    private val _brandEligibleStatus = MutableLiveData<Boolean>()
    val brandEligibleStatus: LiveData<Boolean> = _brandEligibleStatus

    private val _accountName = MutableLiveData<String?>()
    val accountName: LiveData<String?> = _accountName

    private val _matchedGoCreateOffer = MutableLiveData<ShopItem>()
    val matchedGoCreateOffer: LiveData<ShopItem> = _matchedGoCreateOffer

    private val _selectedConfiguration = MutableLiveData<GoCreateOfferConfiguration?>()
    val selectedConfiguration: LiveData<GoCreateOfferConfiguration?> = _selectedConfiguration

    fun setEntryPointTitle(title: String) {
        _entryPointTitle.value = title
    }

    fun setMobileNumber(number: String) {
        _mobileNumber.value = number
        validateNumber(number)
    }

    fun setMatchedOfferConfiguration(offer: ShopItem, configuration: GoCreateOfferConfiguration) {
        _matchedGoCreateOffer.value = offer
        _selectedConfiguration.value = configuration
    }

    fun clearSelectedConfiguration() {
        _selectedConfiguration.value = null
    }

    private fun validateNumber(number: String) {
        viewModelScope.launch {
            accountDomainManager.getPersistentBrands().collect { persistentBrands ->

                val brandModel = persistentBrands.find { it.primaryMsisdn == number }
                _brandEligibleStatus.value = brandModel?.brand == GO_CREATE_ELIGIBLE_BRAND
                _accountName.value = brandModel?.accountName
            }
        }
    }

    fun updateCatalogOffers(offers: List<ShopItem>) {
        viewModelScope.launch {
            _catalogOffers.emit(offers)
        }
    }

    fun showGeneralError() {
        handler.handleGeneralError(GeneralError.General)
    }

    override val logTag = "GoCreateGeneralViewModel"
}
