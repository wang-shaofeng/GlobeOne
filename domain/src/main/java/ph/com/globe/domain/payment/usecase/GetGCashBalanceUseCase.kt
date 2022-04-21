/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.usecase

import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.errors.payment.GetGcashBalanceError
import ph.com.globe.model.payment.GetGCashBalanceResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetGCashBalanceUseCase @Inject constructor(private val paymentManager: PaymentDataManager) {
    suspend fun execute(mobileNumber: String): LfResult<GetGCashBalanceResult, GetGcashBalanceError> =
        paymentManager.getGCashBalance(mobileNumber)
}
