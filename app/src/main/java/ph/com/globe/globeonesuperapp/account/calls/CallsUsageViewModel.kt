package ph.com.globe.globeonesuperapp.account.calls

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.model.account.GetPlanDetailsParams
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class CallsUsageViewModel @Inject constructor(
    private val accountDomainManager: AccountDomainManager
) : BaseViewModel() {

    private val _planName: MutableLiveData<String> = MutableLiveData()
    val planName: LiveData<String> = _planName

    fun getMobilePlanDetails(msisdn: String) {
        viewModelScope.launch {
            accountDomainManager.getMobilePlanDetails(
                GetPlanDetailsParams(
                    msisdn = msisdn,
                    segment = AccountSegment.Mobile
                )
            ).fold({ planDetailsResult ->
                _planName.value = planDetailsResult.plan.planName
                dLog("Fetching mobile plan details success")
            }, {
                dLog("Fetching mobile plan details failure")
            })
        }
    }

    override val logTag = "CallsUsageViewModel"
}
