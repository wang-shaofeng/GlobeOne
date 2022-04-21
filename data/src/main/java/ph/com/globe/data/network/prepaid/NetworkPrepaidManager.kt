package ph.com.globe.data.network.prepaid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.data.network.prepaid.calls.PrepaidComponent
import ph.com.globe.domain.prepaid.PrepaidDataManager
import ph.com.globe.errors.prepaid.GetAccountManagementTransactionError
import ph.com.globe.model.prepaid.PrepaidTransactions
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkPrepaidManager @Inject constructor(
    factory : PrepaidComponent.Factory
) : PrepaidDataManager {

    val component = factory.create()

    override suspend fun getAccountManagementTransaction(
        transactionType: String,
        brand: String,
        dateFrom: String,
        dateTo: String,
        lastTransactionKey: String?,
        mobileNumber: String
    ): LfResult<PrepaidTransactions, GetAccountManagementTransactionError> =
        withContext(Dispatchers.IO){
            component.provideGetAccountManagementTransactionNetworkCall().execute(
                transactionType, brand, dateFrom, dateTo, lastTransactionKey, mobileNumber
            )
        }
}
