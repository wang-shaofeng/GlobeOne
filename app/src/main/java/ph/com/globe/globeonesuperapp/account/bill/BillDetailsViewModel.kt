package ph.com.globe.globeonesuperapp.account.bill

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.com.globe.domain.billings.BillingsDomainManager
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.shared_preferences.BILL_BUBBLE_SHOWN_KEY
import ph.com.globe.model.billings.domain_models.BillingStatement
import ph.com.globe.model.billings.network_models.GetBillingsStatementsParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.fold
import javax.inject.Inject

@HiltViewModel
class BillDetailsViewModel @Inject constructor(
    private val billingsDomainManager: BillingsDomainManager,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel() {

    private val billBubbleShown: Boolean
        get() = sharedPreferences.getBoolean(BILL_BUBBLE_SHOWN_KEY, false)

    private val _bubbleVisibilityState = MutableLiveData(false)
    val bubbleVisibilityState: LiveData<Boolean> = _bubbleVisibilityState

    private val _billStatementsStatus = MutableLiveData<BillStatementsStatus>()
    val billStatementsStatus: LiveData<BillStatementsStatus> = _billStatementsStatus

    fun fetchBillStatements(enrolledAccount: EnrolledAccount) {
        _billStatementsStatus.value = BillStatementsStatus.Loading
        viewModelScope.launch {
            billingsDomainManager.getBillingsStatements(
                GetBillingsStatementsParams(
                    enrolledAccount.mobileNumber ?: "",
                    enrolledAccount.segment,
                    NUMBER_OF_FETCHED_BILLS
                )
            ).fold({
                _billStatementsStatus.value = BillStatementsStatus.Success(it)
            }, {
                _billStatementsStatus.value = BillStatementsStatus.Error
            })
        }
    }

    fun showBubbleIfFirstEntrance() {
        if (!billBubbleShown) {
            viewModelScope.launch {
                delay(BUBBLE_INITIAL_DELAY)
                _bubbleVisibilityState.value = true
                delay(BUBBLE_VISIBILITY_DURATION)
                _bubbleVisibilityState.value = false

                // Update bubble state in preferences
                sharedPreferences.edit().putBoolean(BILL_BUBBLE_SHOWN_KEY, true).apply()
            }
        }
    }

    sealed class BillStatementsStatus {
        data class Success(
            val billStatements: List<BillingStatement>
        ) : BillStatementsStatus()

        object Loading : BillStatementsStatus()

        object Error : BillStatementsStatus()
    }

    override val logTag: String = "BillDetailsViewModel"
}

private const val BUBBLE_INITIAL_DELAY = 500L
private const val BUBBLE_VISIBILITY_DURATION = 5000L

// We need last two bills
private const val NUMBER_OF_FETCHED_BILLS = 2
