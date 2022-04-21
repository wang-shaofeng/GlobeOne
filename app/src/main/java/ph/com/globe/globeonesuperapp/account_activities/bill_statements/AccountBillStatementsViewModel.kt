/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.bill_statements

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ph.com.globe.domain.billings.BillingsDomainManager
import ph.com.globe.errors.billings.GetBillingsStatementsError
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.model.billings.domain_models.BillingStatement
import ph.com.globe.model.billings.network_models.GetBillingsStatementsParams
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.AccountSegment
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@HiltViewModel
class AccountBillStatementsViewModel @Inject constructor(
    private val BillingsDomainManager: BillingsDomainManager,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {
    private val handler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _enrolledAccount =
        savedStateHandle.getLiveData<EnrolledAccount>(ENROLLED_ACCOUNT_KEY)
    val enrolledAccount = _enrolledAccount as LiveData<EnrolledAccount>

    private val _dateFilter =
        savedStateHandle.getLiveData<DateFilter>(DATE_FILTER_KEY, DateFilter.Last3Months)
    val dateFilter = _dateFilter as LiveData<DateFilter>

    private val _data = MutableLiveData<List<AccountActivityPagingState>>()
    val data = _data as LiveData<List<AccountActivityPagingState>>

    private var currentJob: Job? = null

    private lateinit var _filter: DateFilter

    init {
        viewModelScope.launch {
            combine(
                _enrolledAccount.asFlow().distinctUntilChanged(),
                _dateFilter.asFlow().distinctUntilChanged()
            ) { _, filter ->
                _filter = filter
            }.collect { load() }
        }
    }

    fun setEnrolledAccount(enrolledAccount: EnrolledAccount) {
        _enrolledAccount.postValue(enrolledAccount)
    }

    fun setDateFilter(dateFilter: DateFilter) {
        _dateFilter.value = dateFilter
    }

    fun load() {
        _data.value = listOf(AccountActivityPagingState.SkeletonLoading)

        viewModelScope.launch {
            if (currentJob?.isActive == true) currentJob?.cancelAndJoin()

            currentJob = viewModelScope.launch { fetch() }
        }
    }

    private suspend fun fetch() {
        BillingsDomainManager.getBillingsStatements(
            GetBillingsStatementsParams(
                mobileNumber = _enrolledAccount.value?.mobileNumber ?: "",
                segment = AccountSegment.Mobile,
                pageSize = _filter.getMonths()
            )
        ).onSuccess {
            val transactionsState = when {
                it.isEmpty() -> setEmptyState()
                else -> getCurrentData().addNewTransactions(it).addReachEnd()
            }

            _data.value = transactionsState
        }.onFailure {
            when (it) {
                is GetBillingsStatementsError.NoBillingStatementFound -> {
                    _data.value = setEmptyState()
                }

                is GetBillingsStatementsError.General -> {
                    _data.value = getCurrentData().addErrorState()
                    handler.handleGeneralError(it.error)
                }
            }
        }
    }

    private fun getCurrentData() = _data.value ?: emptyList()

    private fun List<AccountActivityPagingState>.addErrorState() =
        clearLoadingStates(this) + AccountActivityPagingState.Error

    private fun setEmptyState() = listOf(AccountActivityPagingState.Empty)

    private fun List<AccountActivityPagingState>.addReachEnd() =
        clearLoadingStates(this) + AccountActivityPagingState.ReachEnd

    private fun List<AccountActivityPagingState>.addNewTransactions(
        newTransactions: List<BillingStatement>
    ) = clearLoadingStates(this) + newTransactions.map {
        AccountActivityPagingState.BillStatement(it)
    }

    private fun clearLoadingStates(list: List<AccountActivityPagingState>?) =
        list?.mapNotNull {
            if (it is AccountActivityPagingState.BillStatement) it else null
        } ?: emptyList()

    companion object {
        const val ENROLLED_ACCOUNT_KEY = "enrolledAccount"
        const val DATE_FILTER_KEY = "dateFilter"
    }

    override val logTag = "AccountBillStatementsViewModel"
}
