package ph.com.globe.globeonesuperapp.account_activities.rewards

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.account_activities.AccountActivitiesDomainManager
import ph.com.globe.errors.account_activities.GetRewardsHistoryError
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.date.MINUS_ONE_DAY
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.model.account.toNumberType
import ph.com.globe.model.account.toSubscribeType
import ph.com.globe.model.account_activities.AccountRewardsTransaction
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@HiltViewModel
class AccountRewardsViewModel @Inject constructor(
    private val accountActivitiesDomainManager: AccountActivitiesDomainManager,
    savedStateHandle: SavedStateHandle
) : ViewModel(), HasLogTag {
    private val handler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _enrolledAccount =
        savedStateHandle.getLiveData<EnrolledAccount>(ENROLLED_ACCOUNT_KEY)
    val enrolledAccount = _enrolledAccount as LiveData<EnrolledAccount>

    private val _dateFilter =
        savedStateHandle.getLiveData<DateFilter>(DATE_FILTER_KEY, DateFilter.Yesterday(buffer = MINUS_ONE_DAY))
    val dateFilter = _dateFilter as LiveData<DateFilter>

    private val _transactions = MutableLiveData<List<AccountActivityPagingState>>()
    val transactions = _transactions as LiveData<List<AccountActivityPagingState>>

    private var currentDataNum = 0

    private var currentJob: Job? = null

    private var partialDates: MutableList<DateFilter.DateRange> = mutableListOf()

    init {
        viewModelScope.launch {
            combine(
                _enrolledAccount.asFlow().distinctUntilChanged(),
                _dateFilter.asFlow().distinctUntilChanged()
            ) { _, filter ->
                // this method is used for triggering reload function
                partialDates = filter.calculateDateRange().toMutableList()
            }.collect { reload() }
        }
    }

    fun setEnrolledAccount(enrolledAccount: EnrolledAccount) {
        _enrolledAccount.postValue(enrolledAccount)
    }

    fun setDateFilter(dateFilter: DateFilter) {
        _dateFilter.value = dateFilter
    }

    fun reload() {
        _transactions.value = listOf(AccountActivityPagingState.SkeletonLoading)
        currentDataNum = 0

        viewModelScope.launch {
            if (currentJob?.isActive == true) currentJob?.cancelAndJoin()

            loadMore()
        }
    }

    fun loadMore() {
        currentJob = viewModelScope.launch { nextFetch() }
    }

    private suspend fun nextFetch() {
        // Reset trigger flag in Loading
        if (_transactions.value?.lastOrNull() is AccountActivityPagingState.Loading) {
            _transactions.value = clearLoadingStates(_transactions.value) +
                    AccountActivityPagingState.Loading(false, currentDataNum)
        }

        accountActivitiesDomainManager.getRewardsHistory(
            _enrolledAccount.value?.primaryMsisdn ?: "",
            partialDates.firstOrNull()?.dateFrom ?: "",
            partialDates.firstOrNull()?.dateTo ?: "",
            (_enrolledAccount.value?.primaryMsisdn ?: "").toNumberType().toSubscribeType()
        ).onSuccess {
            if (partialDates.isNotEmpty()) partialDates.removeFirst()
            currentDataNum += it.size

            val transactionsState = when {
                currentDataNum == 0 && partialDates.isEmpty() -> setEmptyState()
                partialDates.isEmpty() -> getCurrentTransactions()
                    .addNewTransactions(it)
                    .addReachEnd()
                else -> getCurrentTransactions()
                    .addNewTransactions(it)
                    .addLoading(currentDataNum)
            }

            _transactions.value = transactionsState
        }.onFailure {
            _transactions.value = getCurrentTransactions().addErrorState()

            if (it is GetRewardsHistoryError.General)
                handler.handleGeneralError(it.error)
        }
    }

    private fun getCurrentTransactions() = _transactions.value ?: emptyList()

    private fun List<AccountActivityPagingState>.addErrorState() =
        clearLoadingStates(this) + AccountActivityPagingState.Error

    private fun setEmptyState() = listOf(AccountActivityPagingState.Empty)

    private fun List<AccountActivityPagingState>.addReachEnd() =
        clearLoadingStates(this) + AccountActivityPagingState.ReachEnd

    private fun List<AccountActivityPagingState>.addNewTransactions(
        newTransactions: List<AccountRewardsTransaction>
    ) = clearLoadingStates(this) + newTransactions.map {
        AccountActivityPagingState.Rewards(it)
    }

    private fun List<AccountActivityPagingState>.addLoading(currentOffset: Int) =
        clearLoadingStates(this) + AccountActivityPagingState.Loading(true, currentOffset)

    private fun clearLoadingStates(list: List<AccountActivityPagingState>?) =
        list?.mapNotNull {
            if (it is AccountActivityPagingState.Rewards) it else null
        } ?: emptyList()

    companion object {
        const val ENROLLED_ACCOUNT_KEY = "enrolledAccount"
        const val DATE_FILTER_KEY = "dateFilter"
    }

    override val logTag = "AccountRewardsViewModel"
}
