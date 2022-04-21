package ph.com.globe.domain.billings.usecase

import ph.com.globe.domain.billings.BillingsDataManager
import ph.com.globe.errors.billings.GetBillingsStatementsError
import ph.com.globe.model.billings.domain_models.BillingStatement
import ph.com.globe.model.billings.network_models.GetBillingsStatementsParams
import ph.com.globe.model.billings.network_models.toDomain
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetBillingsStatementsUseCase @Inject constructor(private val billingsDataManager: BillingsDataManager) {

    suspend fun execute(params: GetBillingsStatementsParams): LfResult<List<BillingStatement>, GetBillingsStatementsError> =
        billingsDataManager.getBillingsStatements(params).fold(
            {
                LfResult.success(it.toDomain())
            }, {
                LfResult.failure(it)
            }
        )
}
