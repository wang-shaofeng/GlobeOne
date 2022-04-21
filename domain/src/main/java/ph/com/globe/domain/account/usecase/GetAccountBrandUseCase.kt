/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.account.usecase

import ph.com.globe.domain.account.AccountDataManager
import ph.com.globe.errors.account.GetAccountBrandError
import ph.com.globe.model.account.*
import ph.com.globe.model.util.brand.AccountBrand
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetAccountBrandUseCase @Inject constructor(
    private val accountManager: AccountDataManager
) {

    suspend fun execute(params: GetAccountBrandParams): LfResult<GetAccountBrandResponse, GetAccountBrandError> =
        if (params.msisdn.isAccountNumber() || params.msisdn.isLandlineNumber()) {
            // if the msisdn is account number or landline number we immediately know that the brand is GHP aka Globe Postpaid broadband.
            LfResult.success(GetAccountBrandResponse(GetAccountBrandResult(AccountBrand.GhpPostpaid)))
        } else
        // TODO speed up LfResult.success(GetAccountBrandResponse(GetAccountBrandResult(AccountBrand.GhpPrepaid)))
            accountManager.getAccountBrand(params)
}
