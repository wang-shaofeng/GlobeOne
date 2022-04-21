/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.credit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.credit.di.CreditComponent
import ph.com.globe.errors.credit.GetCreditInfoError
import ph.com.globe.errors.credit.LoanPromoError
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.credit.GetCreditInfoResponse
import ph.com.globe.model.credit.LoanPromoParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class CreditUserCaseManager @Inject constructor(
    factory: CreditComponent.Factory
) : CreditDomainManager {

    private val creditComponent: CreditComponent = factory.create()

    override suspend fun getCreditInfo(params: GetCreditInfoParams): LfResult<GetCreditInfoResponse, GetCreditInfoError> =
        withContext(Dispatchers.IO) {
            creditComponent.provideGetCreditInfoUseCase().execute(params)
        }

    override suspend fun loanPromo(params: LoanPromoParams): LfResult<Unit, LoanPromoError> =
        withContext(Dispatchers.IO) {
            creditComponent.provideLoanPromoUseCase().execute(params)
        }
}
