/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.shop.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.auth.AuthDomainManager
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.domain.shop.ShopDomainManager
import ph.com.globe.errors.account.GetAccountBrandError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.reemitValue
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.auth.LoginStatus
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.profile.domain_models.isPostpaid
import ph.com.globe.model.profile.domain_models.isPrepaid
import ph.com.globe.model.shop.ContactData
import ph.com.globe.model.shop.phoneNumberStringValid
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.model.util.brand.AccountBrandType
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager,
    private val profileDomainManager: ProfileDomainManager,
    private val shopDomainManager: ShopDomainManager,
    private val authDomainManager: AuthDomainManager,
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val accounts = mutableListOf<EnrolledAccount>()

    private val _balanceUpdated: MutableLiveData<Pair<String, Float?>> = MutableLiveData()
    val balanceUpdated: LiveData<Pair<String, Float?>> = _balanceUpdated

    private val _enrolledAccounts = MutableLiveData<List<EnrolledAccount>>()
    val enrolledAccounts: LiveData<List<EnrolledAccount>> = _enrolledAccounts

    var numberSelectedFromContacts = ContactData()

    private val _selectedNumber = MutableLiveData<String>()
    val selectedNumber: LiveData<String> = _selectedNumber

    private val _numberSelectedOneTimeEvent = MutableLiveData<OneTimeEvent<Unit>>()
    val numberSelectedOneTimeEvent: LiveData<OneTimeEvent<Unit>> = _numberSelectedOneTimeEvent

    private val _isRetailer = MutableLiveData<Boolean>()
    val isRetailer: LiveData<Boolean> = _isRetailer

    private val _lastCheckedNumberValidation =
        MutableLiveData(NumberValidation("", false, null, null, null))
    val lastCheckedNumberValidation: LiveData<NumberValidation> = _lastCheckedNumberValidation

    var lastValidatedBrandType: AccountBrand? = null

    init {
        if (isLoggedIn())
            viewModelScope.launch {
                profileDomainManager.getEnrolledAccounts()
                    .collect {
                        it.fold({ enrolledAccountsList ->
                            dLog("Fetched enrolled accounts.")
                            accounts.clear()
                            accounts.addAll(enrolledAccountsList)
                            _enrolledAccounts.value = accounts
                        }, {
                            dLog("Failed to fetch enrolled accounts $it")
                            // TODO error handling
                            _enrolledAccounts.value = emptyList()
                        })
                    }
            }
    }

    private fun isLoggedIn(): Boolean =
        authDomainManager.getLoginStatus() != LoginStatus.NOT_LOGGED_IN

    private fun getNumberBrandAndValidity(number: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            accountDomainManager.getAccountBrand(GetAccountBrandParams(number)).fold({
                dLog("Fetched phone number brand.")
                lastValidatedBrandType = it.result.brand
                _lastCheckedNumberValidation.value =
                    NumberValidation(
                        number,
                        true,
                        it.result.brand,
                        it.result.brand.brandType,
                        null
                    )
            }, {
                dLog("Failed to fetch phone number brand.")
                when (it) {
                    is GetAccountBrandError.General -> handler.handleGeneralError(it.error)
                    else -> _lastCheckedNumberValidation.value =
                        NumberValidation(number, false, null, null, it)
                }
            })
        }
    }

    private fun validateNumber(number: String) {
        lastCheckedNumberValidation.value?.let { lastValidation ->
            if (lastValidation.number == number) {
                if (!lastValidation.isValid)
                    _lastCheckedNumberValidation.reemitValue()
            } else if (number.phoneNumberStringValid()) {
                getNumberBrandAndValidity(number)
            } else _lastCheckedNumberValidation.value =
                NumberValidation(number, false, null, null, GetAccountBrandError.InvalidParameter)
        }
    }

    private fun validateRetailer(serviceNumber: String) {
        viewModelScope.launch {
            shopDomainManager.validateRetailer(serviceNumber).fold({
                _isRetailer.value = it
                dLog("Validate retailer network call success")
            }, {
                _isRetailer.value = false
                dLog("Validate retailer network call failure")
            })
        }
    }

    fun selectAndValidateNumber(inputtedNumber: String?) {
        inputtedNumber?.let { number ->
            _selectedNumber.value = number
            _numberSelectedOneTimeEvent.value = OneTimeEvent(Unit)
            validateNumber(number)
            validateRetailer(number)
        }
    }

    fun getNumberOwnerOrPlaceholder(number: String?, placeholder: String): String {
        val matchingAccount =
            enrolledAccounts.value?.filter { !it.isPostpaid() }?.find { it.primaryMsisdn == number }
        return matchingAccount?.accountAlias
            ?: if (numberSelectedFromContacts.phoneNumber == number)
                numberSelectedFromContacts.contactName
            else
                placeholder
    }

    fun isEnrolledAccountNumber(number: String): Boolean {
        return _enrolledAccounts.value?.let { accounts ->
            accounts.filter { !it.isPostpaid() }.find { it.primaryMsisdn == number } != null
        } ?: false
    }

    fun inquireBalance() {
        viewModelScope.launch {
            for (i in 0 until accounts.size) {
                if (accounts[i].isPrepaid()) {
                    accountDomainManager.inquirePrepaidBalance(accounts[i].primaryMsisdn).fold({
                        _balanceUpdated.postValue(Pair(accounts[i].primaryMsisdn, it.balance))
                    }, {

                    })
                }
            }
        }
    }

    override val logTag = "ContactsViewModel"
}

data class NumberValidation(
    var number: String,
    var isValid: Boolean,
    var brand: AccountBrand?,
    var brandType: AccountBrandType?,
    var error: GetAccountBrandError?
)
