package ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.data

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ph.com.globe.domain.account.AccountDomainManager
import ph.com.globe.domain.prepaid.PrepaidDomainManager
import ph.com.globe.errors.prepaid.GetAccountManagementTransactionError
import ph.com.globe.globeonesuperapp.account_activities.AccountActivityPagingState
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.DATA_TRANSACTION_TYPE
import ph.com.globe.globeonesuperapp.account_activities.prepaid_transaction.PrepaidBasedViewModel
import ph.com.globe.util.onFailure
import ph.com.globe.util.onSuccess
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val prepaidDomainManager: PrepaidDomainManager,
    accountDomainManager: AccountDomainManager,
    savedStateHandle: SavedStateHandle
) : PrepaidBasedViewModel(savedStateHandle, accountDomainManager) {

    override suspend fun nextFetch() {
        val mobileNumber = enrolledAccount.value?.primaryMsisdn ?: ""

        if (transactions.value?.lastOrNull() is AccountActivityPagingState.Loading) {
            setTransactions(
                clearLoadingStates(transactions.value) +
                        AccountActivityPagingState.Loading(false, currentDataNum)
            )
        }

        if (accountBrand == null) {
            accountBrand = getBrandFromGetAccountBrand(msisdn = mobileNumber)
        }

        prepaidDomainManager.getAccountManagementTransaction(
            transactionType = DATA_TRANSACTION_TYPE,
            brand = accountBrand?.name?.uppercase() ?: "",
            dateFrom = partialDates.lastOrNull()?.dateFrom ?: "",
            dateTo = partialDates.firstOrNull()?.dateTo ?: "",
            lastTransactionKey = lastTransactionKey,
            mobileNumber = mobileNumber
        ).onSuccess {
            val hasMorePage = it.morePage
            lastTransactionKey = it.lastTransactionKey
            currentDataNum += it.list.size

            val transactionsState = if (hasMorePage) {
                getCurrentTransactions().addNewTransactions(it.list).addLoading(currentDataNum)
            } else {
                getCurrentTransactions().addNewTransactions(it.list).addReachEnd()
            }
            setTransactions(transactionsState)
        }.onFailure {
            if (it is GetAccountManagementTransactionError.EmptyTransaction) {
                setTransactions(setEmptyState())
            } else {
                setTransactions(getCurrentTransactions().addErrorState())
                if (it is GetAccountManagementTransactionError.General)
                    handler.handleGeneralError(it.error)
            }
        }
    }

    override val logTag = "DataViewModel"
}
