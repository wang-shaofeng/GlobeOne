package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction

import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.globeonesuperapp.account_activities.AccountActivitiesViewModel
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.date.MINUS_TWO_DAY
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandler
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.model.account.GetAccountBrandParams
import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.fold

abstract class PrepaidBasedViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountDomainManager: AccountDomainManager
) : BaseViewModel() {

    private var currentJob: Job? = null

    protected val handler: GeneralEventsHandler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _enrolledAccount =
        savedStateHandle.getLiveData<EnrolledAccount>(AccountActivitiesViewModel.ENROLLED_ACCOUNT_KEY)
    val enrolledAccount = _enrolledAccount as LiveData<EnrolledAccount>

    private val _dateFilter =
        savedStateHandle.getLiveData<DateFilter>(
            PrepaidLedgerViewModel.DATE_FILTER_KEY,
            DateFilter.Last2Days(buffer = MINUS_TWO_DAY)
        )

    protected var partialDates: MutableList<DateFilter.DateRange> = mutableListOf()

    private val _transactions: MutableLiveData<List<AccountActivityPagingState>> = MutableLiveData(
        listOf()
    )
    val transactions = _transactions as LiveData<List<AccountActivityPagingState>>

    protected var lastTransactionKey: String? = null

    protected var currentDataNum = 0

    protected var accountBrand: AccountBrand? = null

    abstract suspend fun nextFetch()

    init {
        viewModelScope.launch {
            combine(
                _enrolledAccount.asFlow().distinctUntilChanged(),
                _dateFilter.asFlow().distinctUntilChanged()
            ) { _, date ->
                partialDates = date.calculateDateRange().toMutableList()
            }.collect {
                clearCacheData()
                reload()
            }
        }
    }

    fun setTransactions(transactions: List<AccountActivityPagingState>) {
        _transactions.value = transactions
    }

    fun setFilterDate(date: DateFilter) {
        _dateFilter.value = date
    }

    fun setEnrolledAccount(enrolledAccount: EnrolledAccount) {
        _enrolledAccount.value = enrolledAccount
    }

    fun reload() {
        _transactions.value = listOf(AccountActivityPagingState.SkeletonLoading)
        viewModelScope.launch {
            if (currentJob?.isActive == true) currentJob?.cancelAndJoin()
            loadMore()
        }
    }

    fun loadMore() {
        currentJob = viewModelScope.launch {
            nextFetch()
        }
    }

    protected suspend fun getBrandFromGetAccountBrand(msisdn: String): AccountBrand? {
        accountDomainManager.getAccountBrand(GetAccountBrandParams(msisdn))
            .fold({ response ->
                return response.result.brand
            }, {
                return null
            })
    }

    private fun clearCacheData() {
        _transactions.value = emptyList()
        lastTransactionKey = null
        currentDataNum = 0
    }

    protected fun getCurrentTransactions() = _transactions.value ?: emptyList()

    protected fun List<AccountActivityPagingState>.addErrorState() =
        clearLoadingStates(this) + AccountActivityPagingState.Error

    protected fun setEmptyState() = listOf(AccountActivityPagingState.Empty)

    protected fun List<AccountActivityPagingState>.addReachEnd(): List<AccountActivityPagingState> {
        return if (this.size < 5) this
        else clearLoadingStates(this) + AccountActivityPagingState.ReachEnd
    }

    protected fun List<AccountActivityPagingState>.addNewTransactions(
        newTransactions: List<PrepaidLedgerTransactionItem>
    ) = clearLoadingStates(this) + newTransactions.map {
        AccountActivityPagingState.PrepaidLedger(it)
    }

    protected fun List<AccountActivityPagingState>.addLoading(currentOffset: Int) =
        clearLoadingStates(this) + AccountActivityPagingState.Loading(true, currentOffset)

    protected fun clearLoadingStates(list: List<AccountActivityPagingState>?) =
        list?.mapNotNull {
            if (it is AccountActivityPagingState.PrepaidLedger) it else null
        } ?: emptyList()
}
