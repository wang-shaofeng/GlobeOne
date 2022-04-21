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
import ph.com.globe.errors.account.GetAccountAccessTypeError
import ph.com.globe.model.account.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetAccountAccessTypeNetworkCall @Inject constructor(
    private val accountRetrofit: AccountRetrofit,
    private val tokenRepository: TokenRepository,
) : HasLogTag {

    suspend fun execute(params: GetAccountAccessTypeParams): LfResult<GetAccountAccessTypeResult, GetAccountAccessTypeError> {

        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(GetAccountAccessTypeError.General(GeneralError.NotLoggedIn))
        }

        val response = kotlin.runCatching {
            accountRetrofit.getAccountAccessType(headers, params.toQueryMap())
        }.fold(
            Response<GetAccountAccessTypeResponse>::toLfSdkResult,
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

    override val logTag = "GetAccountAccessTypeNetworkCall"
}

private fun NetworkError.toSpecific() =
    GetAccountAccessTypeError.General(GeneralError.Other(this))
