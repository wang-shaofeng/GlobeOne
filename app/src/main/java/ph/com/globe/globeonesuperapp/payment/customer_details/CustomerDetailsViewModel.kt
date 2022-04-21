/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.payment.customer_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.profile.ProfileDomainManager
import ph.com.globe.errors.profile.GetCustomerDetailsError
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.OneTimeEvent
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.profile.response_models.CustomerDetails
import ph.com.globe.model.profile.response_models.GetCustomerDetailsParams
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@HiltViewModel
class CustomerDetailsViewModel @Inject constructor(
    private val profileDomainManager: ProfileDomainManager
) : BaseViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _customerDetailsResult = MutableLiveData<OneTimeEvent<CustomerDetails>>()
    val customerDetailsResult: LiveData<OneTimeEvent<CustomerDetails>> = _customerDetailsResult

    fun getCustomerDetails(params: GetCustomerDetailsParams) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            profileDomainManager.getCustomerDetails(params)
                .onSuccess { customerDetails ->
                    _customerDetailsResult.value = OneTimeEvent(customerDetails)
                    dLog("Get customer details success")
                }
                .onFailure { error ->
                    handler.handleGeneralError(
                        (error as GetCustomerDetailsError.General).error
                    )
                    dLog("Get customer details failure")
                }
        }
    }

    override val logTag = "CustomerDetailsViewModel"
}
