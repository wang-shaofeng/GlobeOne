package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetAccountAccessTypeError
import ph.com.globe.model.account.GetAccountAccessTypeParams
import ph.com.globe.model.account.GetAccountAccessTypeResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetAccountAccessTypeUseCase @Inject constructor(private val accountManager: AccountDataManager) {

    suspend fun execute(params: GetAccountAccessTypeParams): LfResult<GetAccountAccessTypeResult, GetAccountAccessTypeError> =
        accountManager.getAccountAccessType(params)
}
