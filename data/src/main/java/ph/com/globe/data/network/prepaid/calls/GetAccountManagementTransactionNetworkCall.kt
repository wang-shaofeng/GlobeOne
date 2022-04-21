package ph.com.globe.data.network.prepaid.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.prepaid.PrepaidRetrofit
import ph.com.globe.data.network.util.*
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.data.shared_preferences.token.TokenRepository
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.prepaid.GetAccountManagementTransactionError
import ph.com.globe.model.prepaid.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import ph.com.globe.util.successOrErrorAction
import retrofit2.Response
import javax.inject.Inject

class GetAccountManagementTransactionNetworkCall @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val prepaidRetrofit: PrepaidRetrofit
): HasLogTag {

    suspend fun execute(
        transactionType: String,
        brand: String,
        dateFrom: String,
        dateTo: String,
        lastTransactionKey: String?,
        mobileNumber: String
    ): LfResult<PrepaidTransactions, GetAccountManagementTransactionError>{
        val headers = tokenRepository.createAuthenticatedHeader().successOrErrorAction {
            logFailedToCreateAuthHeader()
            return LfResult.failure(
                GetAccountManagementTransactionError.General(GeneralError.NotLoggedIn)
            )
        }

        val response = kotlin.runCatching {
            prepaidRetrofit.getLoyaltySubscribersTransactionHistory(
                headers = headers,
                transactionType = transactionType,
                brand = brand,
                startDate = dateFrom,
                endDate = dateTo,
                lastTransactionKey = lastTransactionKey,
                mobileNumber = mobileNumber
            )
        }.fold(Response<PrepaidLedgerResponseModel>::toLfSdkResult, Throwable::toLFSdkResult)

        response.fold({
            logSuccessfulNetworkCall()
            val transactions = it.result.transactions.map { model ->
                model.toPrepaidLedgerTransactionItem(
                    getPrepaidTransactionType(mobileNumber, model),
                    getTransactionTypeMapping(model)
                )
            }
            return LfResult.success(PrepaidTransactions(transactions, it.result.lastTransactionKey, it.result.morePage.hasMorePage()))
        }, {
            logFailedNetworkCall(it)
            return it.checkIfEmptyTransaction(mobileNumber)
        })
    }

    override val logTag = "GetAccountManagementTransactionNetworkCall"
}

private fun String.hasMorePage(): Boolean = this == "true"

// remove 0 / 63 to compare in msisdn
private fun String.convertToMSISDNFormat() = when {
    this.startsWith("0") -> this.removePrefix("0")
    this.startsWith("63") -> this.removePrefix("63")
    else -> this
}

private fun NetworkError.checkIfEmptyTransaction(mobileNumber: String): LfResult<PrepaidTransactions, GetAccountManagementTransactionError> {
    when (this) {
        is NetworkError.Http -> {
            if (this.errorResponse?.error?.code == "50202" && this.errorResponse?.error?.details.contentEquals("500 - msisdn: ${mobileNumber.convertToMSISDNFormat()} not found"))
                return LfResult.failure(GetAccountManagementTransactionError.EmptyTransaction)
        }
        else -> Unit
    }
    return LfResult.failure(GetAccountManagementTransactionError.General(GeneralError.Other(this)))
}
