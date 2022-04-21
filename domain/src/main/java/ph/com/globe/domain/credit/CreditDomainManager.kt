/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.credit

import ph.com.globe.errors.credit.GetCreditInfoError
import ph.com.globe.errors.credit.LoanPromoError
import ph.com.globe.model.credit.GetCreditInfoParams
import ph.com.globe.model.credit.GetCreditInfoResponse
import ph.com.globe.model.credit.LoanPromoParams
import ph.com.globe.util.LfResult

interface CreditDomainManager {

    suspend fun getCreditInfo(params: GetCreditInfoParams): LfResult<GetCreditInfoResponse, GetCreditInfoError>

    suspend fun loanPromo(params: LoanPromoParams): LfResult<Unit, LoanPromoError>
}
