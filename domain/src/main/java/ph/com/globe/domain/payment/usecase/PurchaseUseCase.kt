/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.domain.payment.PaymentDomainManager
import ph.com.globe.errors.payment.CreateServiceOrderError
import ph.com.globe.errors.payment.PurchaseError
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

/**
 * This use case class is instantiated and used ONLY within the Charge To Load flow.
 */
class PurchaseUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager,
    private val paymentDomainManager: PaymentDomainManager
) :
    HasLogTag {

    suspend fun execute(params: PurchaseParams): LfResult<PurchaseResult, PurchaseError> {
        if (params.sourceNumber != params.targetNumber) {
            // if the sourceNumber and the targetNumber are different than we enter the share-a-load share-a-promo flow
            paymentDomainManager.createServiceOrderUseCase(
                CreateServiceOrderParameters(
                    params.sourceNumber,
                    params.targetNumber,
                    params.purchaseType
                )
            )
        } else {
            // we are in buy promo flow if the source and the target numbers are the same
            paymentManager.multiplePurchasePromo(params)
        }.fold({
            return LfResult.success(
                if (it is CreateServiceIdResult) PurchaseResult.ShareALoadPromoResult(it)
                else PurchaseResult.GeneralResult(it as MultiplePurchasePromoResult)
            )
        }, {
            return LfResult.failure(
                if (it is CreateServiceOrderError) PurchaseError.ShareLoadPromoOtpSendingError(it) else it as PurchaseError
            )
        })
    }

    override val logTag = "PurchaseUseCase"
}
