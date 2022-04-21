/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.errors.payment.GetPaymentsError
import ph.com.globe.model.payment.GetPaymentParams
import ph.com.globe.model.payment.GetPaymentsResult
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetPaymentsUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager
) : HasLogTag {

    suspend fun execute(params: GetPaymentParams): LfResult<GetPaymentsResult, GetPaymentsError> =
        paymentManager.getPayments(params)

    override val logTag = "GetPaymentsUseCase"
}
