package ph.com.globe.domain.billings

import ph.com.globe.errors.billings.GetBillingsDetailsError
import ph.com.globe.errors.billings.GetBillingsStatementsError
import ph.com.globe.errors.billings.GetBillingsStatementsPdfError
import ph.com.globe.model.billings.network_models.*
import ph.com.globe.util.LfResult

interface BillingsDataManager {

    suspend fun getBillingsDetails(params: GetBillingsDetailsParams): LfResult<GetBillingsDetailsResponse, GetBillingsDetailsError>

    suspend fun getBillingsStatements(params: GetBillingsStatementsParams): LfResult<GetBillingsStatementsResponse, GetBillingsStatementsError>

    suspend fun getBillingsStatementsPdf(params: GetBillingsStatementsPdfParams): LfResult<GetBillingsStatementsPdfResponse, GetBillingsStatementsPdfError>
}
