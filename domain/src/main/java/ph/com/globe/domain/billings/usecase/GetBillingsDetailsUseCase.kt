package ph.com.globe.domain.billings.usecase

import ph.com.globe.domain.billings.BillingsDataManager
import ph.com.globe.errors.billings.GetBillingsDetailsError
import ph.com.globe.model.billings.domain_models.BillingsDetails
import ph.com.globe.model.billings.network_models.GetBillingsDetailsParams
import ph.com.globe.model.billings.network_models.toDomain
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetBillingsDetailsUseCase @Inject constructor(private val billingsDataManager: BillingsDataManager) {

    suspend fun execute(params: GetBillingsDetailsParams): LfResult<BillingsDetails, GetBillingsDetailsError> =
        billingsDataManager.getBillingsDetails(params).fold(
            {
                LfResult.success(it.toDomain())
            }, {
                LfResult.failure(it)
            }
        )
}
