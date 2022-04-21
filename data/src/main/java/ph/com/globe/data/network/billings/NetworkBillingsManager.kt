package ph.com.globe.data.network.billings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.billings.BillingsDataManager
import ph.com.globe.errors.billings.GetBillingsDetailsError
import ph.com.globe.errors.billings.GetBillingsStatementsError
import ph.com.globe.errors.billings.GetBillingsStatementsPdfError
import ph.com.globe.model.billings.network_models.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkBillingsManager @Inject constructor(
    factory: BillingsComponent.Factory,
) : BillingsDataManager {

    private val billingsComponent: BillingsComponent = factory.create()

    override suspend fun getBillingsDetails(params: GetBillingsDetailsParams): LfResult<GetBillingsDetailsResponse, GetBillingsDetailsError> =
        withContext(Dispatchers.IO) {
            billingsComponent.provideGetBillingsDetailsNetworkCall().execute(params)
        }

    override suspend fun getBillingsStatements(params: GetBillingsStatementsParams): LfResult<GetBillingsStatementsResponse, GetBillingsStatementsError> =
        withContext(Dispatchers.IO) {
            billingsComponent.provideGetBillingsStatementsNetworkCall().execute(params)
        }

    override suspend fun getBillingsStatementsPdf(params: GetBillingsStatementsPdfParams): LfResult<GetBillingsStatementsPdfResponse, GetBillingsStatementsPdfError> =
        withContext(Dispatchers.IO) {
            billingsComponent.provideGetBillingsStatementsPdfNetworkCall().execute(params)
        }
}
