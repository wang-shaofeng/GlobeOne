/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.payment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.payment.PaymentDataManager
import ph.com.globe.errors.payment.*
import ph.com.globe.model.payment.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkPaymentManager @Inject constructor(
    factory: PaymentComponent.Factory
) : PaymentDataManager {

    private val paymentComponent: PaymentComponent = factory.create()

    override suspend fun createAdyenPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCreateAdyenPaymentSessionNetworkCall().execute(params)
        }

    override suspend fun createGCashPaymentSession(params: CreatePaymentSessionParams): LfResult<CreatePaymentSessionResult, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCreateGCashPaymentSessionNetworkCall().execute(params)
        }

    override suspend fun getGCashBalance(mobileNumber: String): LfResult<GetGCashBalanceResult, GetGcashBalanceError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetGCashBalanceNetworkCall().execute(mobileNumber)
        }

    override suspend fun getPaymentSession(params: GetPaymentSessionParams): LfResult<GetPaymentSessionResult, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetPaymentSessionNetworkCall().execute(params)
        }

    override suspend fun paymentService(params: PaymentServiceParams): LfResult<PaymentServiceResult, PaymentServiceError> =
        withContext(Dispatchers.IO) {
            paymentComponent.providePaymentServiceNetworkCall().execute(params)
        }

    override suspend fun getPaymentMethodNetworkCall(): LfResult<List<CreditCardModel>, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetPaymentMethodNetworkCall().execute()
        }

    override suspend fun deletePaymentMethodNetworkCall(params: DeletePaymentMethodParams): LfResult<Unit, PaymentError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideDeletePaymentMethodNetworkCall().execute(params)
        }

    override suspend fun linkGCashAccountNetworkCall(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideLinkGCashAccountNetworkCall().execute(params)
        }

    override suspend fun unlinkGCashAccountNetworkCall(params: LinkingGCashAccountParams): LfResult<Unit, LinkingGCashError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideUnlinkGCashAccountNetworkCall().execute(params)
        }

    override suspend fun purchasePromo(params: PurchaseParams): LfResult<Unit, PurchaseError> =
        withContext(Dispatchers.IO) {
            paymentComponent.providePurchasePromoNetworkCall().execute(params)
        }

    override suspend fun multiplePurchasePromo(params: PurchaseParams): LfResult<MultiplePurchasePromoResult, PurchaseError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideMultiplePurchasePromoNetworkCall().execute(params)
        }

    override suspend fun purchaseLoad(params: PurchaseParams): LfResult<PurchaseLoadResponse, PurchaseError> =
        withContext(Dispatchers.IO) {
            paymentComponent.providePurchaseLoadNetworkCall().execute(params)
        }

    override suspend fun createServiceOrderLoad(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCreateServiceOrderLoadNetworkCall().execute(params)
        }

    override suspend fun createServiceOrderPromo(params: CreateServiceOrderParameters): LfResult<CreateServiceIdResult, CreateServiceOrderError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideCreateServiceOrderPromoNetworkCall().execute(params)
        }

    override suspend fun getPayments(params: GetPaymentParams): LfResult<GetPaymentsResult, GetPaymentsError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetPaymentsNetworkCall().execute(params)
        }

    override suspend fun getPaymentReceipt(params: GetPaymentReceiptParams): LfResult<String, GetPaymentReceiptError> =
        withContext(Dispatchers.IO) {
            paymentComponent.provideGetPaymentReceiptNetworkCall().execute(params)
        }

}
