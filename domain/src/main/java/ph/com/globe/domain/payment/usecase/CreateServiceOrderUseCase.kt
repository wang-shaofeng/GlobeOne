/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.usecase

import ph.com.globe.analytics.logger.HasLogTag
import ph.com.globe.analytics.logger.dLog
import ph.com.globe.domain.balance.BalanceDataManager
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.errors.balance.CheckBalanceSufficiencyError
import ph.com.globe.errors.payment.CreateServiceOrderError
import ph.com.globe.model.balance.CheckBalanceSufficiencyParams
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import java.lang.IllegalStateException
import javax.inject.Inject

class CreateServiceOrderUseCase @Inject constructor(
    private val paymentManager: PaymentDataManager,
    private val balanceDataManager: BalanceDataManager
) :
    HasLogTag {
    suspend fun execute(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError> {

        balanceDataManager.checkBalanceSufficiency(
            CheckBalanceSufficiencyParams(
                params.sourceNumber,
                (params.purchaseType.amount.toDouble() + params.purchaseType.getShareFee()).toString()
            )
        ).fold({ isSufficient ->
            return if (isSufficient) {
                when (params.purchaseType) {
                    is PurchaseType.BuyPromo -> {
                        // we are in the share-a-promo flow hence createServiceOrderPromo call
                        paymentManager.createServiceOrderPromo(params)
                    }
                    is PurchaseType.BuyLoad -> {
                        // we are in the share-a-load flow hence createServiceOrderLoad call
                        paymentManager.createServiceOrderLoad(params)
                    }
                    else -> throw IllegalStateException("Incorrect purchase type for $logTag")
                }
            } else {
                dLog("create service order failed.")
                LfResult.failure(CreateServiceOrderError.InsufficientFunds)
            }
        }, {
            return LfResult.failure(
                CreateServiceOrderError.General((it as CheckBalanceSufficiencyError.General).error)
            )
        })
    }

    override val logTag = "CreateServiceOrderUseCase"
}
