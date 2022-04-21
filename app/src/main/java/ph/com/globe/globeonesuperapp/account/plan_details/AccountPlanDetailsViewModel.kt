package ph.com.globe.globeonesuperapp.account.plan_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.utils.launchWithLoadingOverlay
import ph.com.globe.model.account.GetBroadbandPlanDetailsResult
import ph.com.globe.model.account.GetMobilePlanDetailsResult
import ph.com.globe.model.account.GetPlanDetailsParams
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class AccountPlanDetailsViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager
) : ViewModel() {

    private val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _broadbandPlanDetails = MutableLiveData<GetBroadbandPlanDetailsResult>()
    val broadbandPlanDetails: LiveData<GetBroadbandPlanDetailsResult> = _broadbandPlanDetails

    private val _mobilePlanDetails = MutableLiveData<GetMobilePlanDetailsResult>()
    val mobilePlanDetails: LiveData<GetMobilePlanDetailsResult> = _mobilePlanDetails

    fun fetchData(msisdn: String, segment: AccountSegment) {
        when (segment) {
            AccountSegment.Mobile -> {
                getMobilePlanDetails(msisdn)
            }
            AccountSegment.Broadband -> {
                getBroadbandPlanDetails(msisdn)
            }
        }
    }

    private fun getMobilePlanDetails(msisdn: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            accountDomainManager.getMobilePlanDetails(
                GetPlanDetailsParams(
                    msisdn = msisdn,
                    segment = AccountSegment.Mobile
                )
            ).fold({
                _mobilePlanDetails.value = it
            }, {
            })
        }
    }

    private fun getBroadbandPlanDetails(msisdn: String) {
        viewModelScope.launchWithLoadingOverlay(handler) {
            accountDomainManager.getBroadbandPlanDetails(
                GetPlanDetailsParams(
                    msisdn = msisdn,
                    segment = AccountSegment.Broadband
                )
            ).fold({
                _broadbandPlanDetails.value = it
            }, {
            })
        }
    }
}
