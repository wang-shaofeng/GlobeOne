package ph.com.globe.domain.auth.usecase

import ph.com.globe.domain.auth.AuthDataManager
import ph.com.globe.errors.auth.ValidateSimSerialPairingError
import ph.com.globe.model.auth.ValidateSimSerialParams
import ph.com.globe.model.auth.ValidateSimSerialResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

class ValidateSimSerialUseCase @Inject constructor(private val authManager: AuthDataManager) {

    suspend fun execute(params: ValidateSimSerialParams): LfResult<ValidateSimSerialResponse, ValidateSimSerialPairingError> {
        return authManager.validateSimSerial(params)
    }
}
