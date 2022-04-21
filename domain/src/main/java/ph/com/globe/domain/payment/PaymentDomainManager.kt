/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment

import ph.com.globe.errors.payment.*
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult

interface PaymentDomainManager {

    suspend fun createAdyenPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError>

    suspend fun submitAdyenPayment(paymentComponentJson: String): LfResult<SubmitAdyenPaymentResult, SubmitAdyenPaymentError>

    suspend fun confirmAdyenPayment(actionComponentJson: String): LfResult<ConfirmAdyenPaymentResult, ConfirmAdyenPaymentError>

    suspend fun createGCashPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError>

    suspend fun getGCashBalance(mobileNumber: String): LfResult<GetGCashBalanceResult, GetGcashBalanceError>

    suspend fun checkPaymentSuccessful(thirdPartyPaymentResult: ThirdPartyPaymentResult): LfResult<GetPaymentSessionResult, PaymentError>

    suspend fun getPaymentMethodUseCase(): LfResult<List<CreditCardModel>, PaymentError>

    suspend fun deletePaymentMethodUseCase(params: DeletePaymentMethodParams): LfResult<Unit, PaymentError>

    suspend fun linkGCashAccountUseCase(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError>

    suspend fun unlinkGCashAccountUseCase(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError>

    suspend fun purchaseUseCase(params: PurchaseParams): LfResult<PurchaseResult, PurchaseError>

    suspend fun createServiceOrderUseCase(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError>

    suspend fun getPayments(params: GetPaymentParams): LfResult<GetPaymentsResult, GetPaymentsError>

    suspend fun getPaymentReceipt(params: GetPaymentReceiptParams): LfResult<String, GetPaymentReceiptError>

}
