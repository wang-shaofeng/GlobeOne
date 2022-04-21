package ph.com.globe.data.network.account.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.account.AccountRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.createAuthenticatedHeader
import ph.com.globe.data.network.util.logFailedToCreateAuthHeader
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.account.GetUsageConsumptionReportsError
import ph.com.globe.model.account.GetUsageConsumptionReportsParams
import ph.com.globe.model.account.GetUsageConsumptionReportsResponse
import ph.com.globe.model.account.UsageConsumptionResult
import ph.com.globe.model.account.toQueryMap
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetUsageConsumptionReportsNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository,
) : HasLogTag {

    suspend fun execute(params: GetUsageConsumptionReportsParams): LfResult<UsageConsumptionResult, GetUsageConsumptionReportsError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetUsageConsumptionReportsError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            accountRetrofit.getUsageConsumptionReports(headers, params.toQueryMap())
        }.fold(
            Response<GetUsageConsumptionReportsResponse>::toLfSdkResult,
            Throwable::toLFSdkResult
        )

        return response.fold({
            logSuccessfulNetworkCall()
            LfResult.success(it.result)
        }, {
            logFailedNetworkCall(it)
            LfResult.failure(it.toSpecific())
        })
    }

    override val logTag = "GetUsageConsumptionReportsNetworkCall"
}

private fun NetworkError.toSpecific() =
    GetUsageConsumptionReportsError.General(GeneralError.Other(this))
