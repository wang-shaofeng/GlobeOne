package ph.com.globe.data.network.auth.calls

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.data.network.auth.AuthRetrofit
import ph.com.globe.data.network.util.logFailedNetworkCall
import ph.com.globe.data.network.util.logSuccessfulNetworkCall
import ph.com.globe.data.network.util.toLFSdkResult
import ph.com.globe.data.network.util.toLfSdkResult
import ph.com.globe.errors.GeneralError
import ph.com.globe.errors.NetworkError
import ph.com.globe.errors.auth.ValidateSimSerialPairingError
import ph.com.globe.globeonesuperapp.data.BuildConfig
import ph.com.globe.model.auth.ValidateSimSerialParams
import ph.com.globe.model.auth.ValidateSimSerialRequest
import ph.com.globe.model.auth.ValidateSimSerialResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import retrofit2.Response
import javax.inject.Inject

class ValidateSimSerialNetworkCall@Inject constructor(
    private val authRetrofit: AuthRetrofit
) : HasLogTag {

    suspend fun execute(params: ValidateSimSerialParams) : LfResult<ValidateSimSerialResponse, ValidateSimSerialPairingError> {
        val response = kotlin.runCatching {
            authRetrofit.validateSimSerial(
                ValidateSimSerialRequest(
                    categoryIdentifier = arrayListOf("EnrollAccount"),
                    mobileNumber = params.mobileNumber,
                    simSerial = params.simSerial.toAcceptedSimSerialFormat(),
                    mode = "1",
                    channel = "edo_campaign"
                )
            )
        }.fold(Response<ValidateSimSerialResponse>::toLfSdkResult, Throwable::toLFSdkResult)

        return response.fold(
            {
                logSuccessfulNetworkCall()
                LfResult.success(it)
            },
            {
                logFailedNetworkCall(it)
                LfResult.failure(it.toSpecific())
            }
        )
    }

    override val logTag = "ValidateSimSerialNetworkCall"
}

private fun String.toAcceptedSimSerialFormat() : String{
    return BuildConfig.HPW_SIMSERIAL_PREFIX.plus(this)
}

private fun NetworkError.toSpecific(): ValidateSimSerialPairingError {
    when (this) {
        is NetworkError.Http -> {
            if (this.errorResponse?.error?.code == "50202" && this.errorResponse?.error?.details == "MSISDN and sim serial do not match.")
                return ValidateSimSerialPairingError.NotValidBroadbandAccount
        }
        else -> Unit
    }
    return ValidateSimSerialPairingError.General(GeneralError.Other(this))
}
