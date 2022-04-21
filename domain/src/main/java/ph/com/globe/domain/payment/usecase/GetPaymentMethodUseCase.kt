/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.errors.payment.PaymentError
import ph.com.globe.model.payment.CreditCardModel
import ph.com.globe.util.LfResult
import javax.inject.Inject

class GetPaymentMethodUseCase @Inject constructor(private val paymentManager: PaymentDataManager) :
    HasLogTag {

    suspend fun execute(): LfResult<List<CreditCardModel>, PaymentError> {
        return paymentManager.getPaymentMethodNetworkCall()
    }

    override val logTag = "CheckPaymentSuccessfulUseCase"
}
