package ph.com.globe.domain.prepaid

import ph.com.globe.errors.prepaid.GetAccountManagementTransactionError
import ph.com.globe.model.prepaid.PrepaidTransactions
import ph.com.globe.util.LfResult

interface PrepaidDataManager {
    suspend fun getAccountManagementTransaction(
        transactionType: String,
        brand: String,
        dateFrom: String,
        dateTo: String,
        lastTransactionKey: String?,
        mobileNumber: String,
    ): LfResult<PrepaidTransactions, GetAccountManagementTransactionError>
}
