/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.credit.usecase

import ph.com.globe.domain.credit.CreditDataManager
import ph.com.globe.errors.credit.GetCreditInfoError
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.credit.GetCreditInfoResponse
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetCreditInfoUseCase @Inject constructor(
    private val creditManager: CreditDataManager
) {

    suspend fun execute(params: GetCreditInfoParams): LfResult<GetCreditInfoResponse, GetCreditInfoError> {
        return creditManager.getCreditInfo(params)
    }
}
