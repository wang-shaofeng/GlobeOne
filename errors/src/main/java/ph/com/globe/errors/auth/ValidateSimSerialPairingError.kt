package ph.com.globe.errors.auth

import ph.com.globe.errors.GeneralError

sealed class ValidateSimSerialPairingError {
    object NotValidBroadbandAccount: ValidateSimSerialPairingError()

    data class General(val error: GeneralError) : ValidateSimSerialPairingError()
}
