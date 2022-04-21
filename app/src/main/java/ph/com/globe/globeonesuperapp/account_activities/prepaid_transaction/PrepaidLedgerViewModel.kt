package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.date.MINUS_TWO_DAY
import javax.inject.Inject

@HiltViewModel
class PrepaidLedgerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val _dateFilter =
        savedStateHandle.getLiveData<DateFilter>(
            DATE_FILTER_KEY,
            DateFilter.Last2Days(buffer = MINUS_TWO_DAY)
        )
    val dateFilter = _dateFilter as LiveData<DateFilter>

    fun setFilterDate(date: DateFilter) {
        _dateFilter.value = date
    }

    companion object {
        const val DATE_FILTER_KEY = "PrepaidLedgerDateFilter"
    }

    override val logTag = "PrepaidLedgerViewModel"
}
