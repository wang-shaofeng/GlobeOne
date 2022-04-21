/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.credit.usecase

import ph.com.globe.domain.credit.CreditDataManager
import ph.com.globe.errors.credit.LoanPromoError
import ph.com.globe.model.credit.LoanPromoParams
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class LoanPromoUseCase @Inject constructor(
    private val creditManager: CreditDataManager
) {

    suspend fun execute(params: LoanPromoParams): LfResult<Unit, LoanPromoError> {
        return creditManager.loanPromo(params).fold({
            LfResult.success(it)
        }, {
            LfResult.failure(it)
        })
    }
}
