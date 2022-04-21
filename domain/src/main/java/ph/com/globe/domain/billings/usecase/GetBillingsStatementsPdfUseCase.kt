package ph.com.globe.domain.billings.usecase

import ph.com.globe.domain.billings.BillingsDataManager
import ph.com.globe.errors.billings.GetBillingsStatementsPdfError
import ph.com.globe.model.billings.network_models.GetBillingsStatementsPdfParams
import ph.com.globe.model.billings.network_models.GetBillingsStatementsPdfResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetBillingsStatementsPdfUseCase @Inject constructor(private val billingsDataManager: BillingsDataManager) {

    suspend fun execute(params: GetBillingsStatementsPdfParams): LfResult<GetBillingsStatementsPdfResponse, GetBillingsStatementsPdfError> =
        billingsDataManager.getBillingsStatementsPdf(params).fold(
            {
                LfResult.success(it)
            }, {
                LfResult.failure(it)
            }
        )
}
