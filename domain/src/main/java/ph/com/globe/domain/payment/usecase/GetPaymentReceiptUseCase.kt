/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.usecase

import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.errors.payment.GetPaymentReceiptError
import ph.com.globe.model.payment.GetPaymentReceiptParams
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetPaymentReceiptUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager
) {
    suspend fun execute(params: GetPaymentReceiptParams): LfResult<String, GetPaymentReceiptError> =
        paymentManager.getPaymentReceipt(params)
}
