/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.globeonesuperapp.account_activities.payments

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.errors.payment.GetPaymentsError
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.account_activities.bill_statements.AccountBillStatementsViewModel
import ph.com.globe.globeonesuperapp.utils.BaseViewModel
import ph.com.globe.globeonesuperapp.utils.date.DateFilter
import ph.com.globe.globeonesuperapp.utils.ui.overlays_and_dialogs.GeneralEventsHandlerProvider
import ph.com.globe.model.payment.GetPaymentParams
import ph.com.globe.model.payment.Payment
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.util.GlobeDateFormat
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import ph.com.globe.util.toFormattedStringOrEmpty
import javax.inject.Inject

@HiltViewModel
class BillPaymentsViewModel @Inject constructor(
    private val paymentDomainManager: PaymentDomainManager,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val handler = GeneralEventsHandlerProvider.generalEventsHandler

    private val _enrolledAccount =
        savedStateHandle.getLiveData<EnrolledAccount>(AccountBillStatementsViewModel.ENROLLED_ACCOUNT_KEY)
    val enrolledAccount = _enrolledAccount as LiveData<EnrolledAccount>

    private val _dateFilter =
        savedStateHandle.getLiveData<DateFilter>(
            AccountBillStatementsViewModel.DATE_FILTER_KEY,
            DateFilter.Last3Months
        )
    val dateFilter = _dateFilter as LiveData<DateFilter>

    private lateinit var _filter: DateFilter

    private var currentJob: Job? = null

    private val _data = MutableLiveData<List<AccountActivityPagingState>>()
    val data = _data as LiveData<List<AccountActivityPagingState>>

    var token = ""

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

    fun load() {
        _data.value = listOf(AccountActivityPagingState.SkeletonLoading)

        viewModelScope.launch {
            if (currentJob?.isActive == true) currentJob?.cancelAndJoin()

            currentJob = viewModelScope.launch { fetch() }
        }
    }

    fun setDateFilter(dateFilter: DateFilter) {
        _dateFilter.value = dateFilter
    }

    private suspend fun fetch() {
        paymentDomainManager.getPayments(
            GetPaymentParams(
                _enrolledAccount.value?.primaryMsisdn ?: "",
                _filter.startDate(),
                System.currentTimeMillis().toFormattedStringOrEmpty(GlobeDateFormat.ISO8601)
            )
        ).onSuccess {
            val transactionsState = when {
                it.payments.isEmpty() -> setEmptyState()
                else -> {
                    getCurrentData().addNewTransactions(it.payments).addReachEnd()
                }
            }

            _data.value = transactionsState
            token = it.token
        }.onFailure {
            _data.value = getCurrentData().addErrorState()

            if (it is GetPaymentsError.General)
                handler.handleGeneralError(it.error)
        }
    }

    private fun getCurrentData() = _data.value ?: emptyList()

    private fun List<AccountActivityPagingState>.addErrorState() =
        clearLoadingStates(this) + AccountActivityPagingState.Error

    private fun setEmptyState() = listOf(AccountActivityPagingState.Empty)

    private fun List<AccountActivityPagingState>.addReachEnd() =
        clearLoadingStates(this) + AccountActivityPagingState.ReachEnd

    private fun List<AccountActivityPagingState>.addNewTransactions(
        newTransactions: List<Payment>
    ) = clearLoadingStates(this) + newTransactions.map {
        AccountActivityPagingState.BillPayment(it)
    }

    private fun clearLoadingStates(list: List<AccountActivityPagingState>?) =
        list?.mapNotNull {
            if (it is AccountActivityPagingState.BillPayment) it else null
        } ?: emptyList()

    override val logTag = "BillPaymentsViewModel"
}
