package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetUsageConsumptionReportsError
import ph.com.globe.model.account.GetUsageConsumptionReportsParams
import ph.com.globe.model.account.UsageConsumptionResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetUsageConsumptionReportsUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(params: GetUsageConsumptionReportsParams): LfResult<UsageConsumptionResult, GetUsageConsumptionReportsError> =
        accountManager.getUsageConsumptionReports(params)
}
