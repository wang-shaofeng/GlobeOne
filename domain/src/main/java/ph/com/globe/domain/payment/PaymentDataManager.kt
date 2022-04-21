/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment

import ph.com.globe.errors.payment.*
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult

interface PaymentDataManager {

    suspend fun createAdyenPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError>

    suspend fun createGCashPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError>

    suspend fun getGCashBalance(mobileNumber: String): LfResult<GetGCashBalanceResult, GetGcashBalanceError>

    suspend fun getPaymentSession(params: GetPaymentSessionParams): LfResult<GetPaymentSessionResult, PaymentError>

    suspend fun paymentService(params: PaymentServiceParams): LfResult<PaymentServiceResult, PaymentServiceError>

    suspend fun getPaymentMethodNetworkCall(): LfResult<List<CreditCardModel>, PaymentError>

    suspend fun deletePaymentMethodNetworkCall(params: DeletePaymentMethodParams): LfResult<Unit, PaymentError>

    suspend fun linkGCashAccountNetworkCall(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError>

    suspend fun unlinkGCashAccountNetworkCall(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError>

    suspend fun purchasePromo(params: PurchaseParams): LfResult<Unit, PurchaseError>

    suspend fun multiplePurchasePromo(params: PurchaseParams): LfResult<MultiplePurchasePromoResult, PurchaseError>

    suspend fun purchaseLoad(params: PurchaseParams): LfResult<PurchaseLoadResponse, PurchaseError>

    suspend fun createServiceOrderLoad(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError>

    suspend fun createServiceOrderPromo(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError>

    suspend fun getPayments(params: GetPaymentParams): LfResult<GetPaymentsResult, GetPaymentsError>

    suspend fun getPaymentReceipt(params: GetPaymentReceiptParams): LfResult<String, GetPaymentReceiptError>

}
