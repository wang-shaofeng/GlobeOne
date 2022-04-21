package ph.com.globe.domain.prepaid

import ph.com.globe.errors.prepaid.GetAccountManagementTransactionError
import ph.com.globe.model.prepaid.PrepaidTransactions
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetAccountManagementTransactionUseCase @Inject constructor(val prepaidDataManager: PrepaidDataManager) {
    suspend fun execute(
        transactionType: String,
        brand: String,
        dateFrom: String,
        dateTo: String,
        lastTransactionKey: String?,
        mobileNumber: String
    ) : LfResult<PrepaidTransactions, GetAccountManagementTransactionError> =
        prepaidDataManager.getAccountManagementTransaction(
            transactionType,
            brand,
            dateFrom,
            dateTo,
            lastTransactionKey,
            mobileNumber
        )
}
