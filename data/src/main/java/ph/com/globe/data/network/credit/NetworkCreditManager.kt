/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.credit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.credit.CreditDataManager
import ph.com.globe.errors.credit.GetCreditInfoError
import ph.com.globe.errors.credit.LoanPromoError
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.credit.GetCreditInfoResponse
import ph.com.globe.model.credit.LoanPromoParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkCreditManager @Inject constructor(
    factory: CreditComponent.Factory
) : CreditDataManager {

    private val creditComponent: CreditComponent = factory.create()

    override suspend fun getCreditInfo(params: GetCreditInfoParams): LfResult<GetCreditInfoResponse, GetCreditInfoError> =
        withContext(Dispatchers.IO) {
            creditComponent.provideGetCreditInfoNetworkCall().execute(params)
        }

    override suspend fun loanPromo(params: LoanPromoParams): LfResult<Unit, LoanPromoError> =
        withContext(Dispatchers.IO) {
            creditComponent.provideLoanPromoNetworkCall().execute(params)
        }
}
