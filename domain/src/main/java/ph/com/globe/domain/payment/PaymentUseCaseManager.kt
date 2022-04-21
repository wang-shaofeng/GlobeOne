/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.payment.di.PaymentComponent
import ph.com.globe.errors.payment.*
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class PaymentUseCaseManager @Inject constructor(
    factory: PaymentComponent.Factory
) : PaymentDomainManager {

    private val paymentComponent: PaymentComponent = factory.create()

    override suspend fun createAdyenPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCreateAdyenPaymentSessionUseCase().execute(params)
        }

    override suspend fun submitAdyenPayment(paymentComponentJson: String): LfResult<SubmitAdyenPaymentResult, SubmitAdyenPaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideSubmitAdyenPaymentUseCase().execute(paymentComponentJson)
        }

    override suspend fun confirmAdyenPayment(actionComponentJson: String): LfResult<ConfirmAdyenPaymentResult, ConfirmAdyenPaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideConfirmAdyenPaymentUseCase().execute(actionComponentJson)
        }

    override suspend fun createGCashPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCreateGCashPaymentSessionUseCase().execute(params)
        }

    override suspend fun getGCashBalance(mobileNumber: String): LfResult<GetGCashBalanceResult, GetGcashBalanceError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetGCashBalanceUseCase().execute(mobileNumber)
        }

    override suspend fun checkPaymentSuccessful(thirdPartyPaymentResult: ThirdPartyPaymentResult): LfResult<GetPaymentSessionResult, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCheckPaymentSuccessfulUseCase().execute(thirdPartyPaymentResult)
        }

    override suspend fun getPaymentMethodUseCase(): LfResult<List<CreditCardModel>, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetPaymentMethodUseCase().execute()
        }

    override suspend fun deletePaymentMethodUseCase(params: DeletePaymentMethodParams): LfResult<Unit, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideDeletePaymentMethodUseCase().execute(params)
        }

    override suspend fun linkGCashAccountUseCase(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideLinkGCashAccountUseCase().execute(params)
        }

    override suspend fun unlinkGCashAccountUseCase(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideUnlinkGCashAccountUseCase().execute(params)
        }

    override suspend fun purchaseUseCase(params: PurchaseParams): LfResult<PurchaseResult, PurchaseError> =
        withContext(Dispatchers.IO) {
            paymentComponent.providePurchaseUseCase().execute(params)
        }

    override suspend fun createServiceOrderUseCase(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCreateServiceOrderUseCase().execute(params)
        }

    override suspend fun getPayments(params: GetPaymentParams): LfResult<GetPaymentsResult, GetPaymentsError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetPaymentsUseCase().execute(params)
        }

    override suspend fun getPaymentReceipt(params: GetPaymentReceiptParams): LfResult<String, GetPaymentReceiptError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetPaymentReceiptUseCase().execute(params)
        }

}
