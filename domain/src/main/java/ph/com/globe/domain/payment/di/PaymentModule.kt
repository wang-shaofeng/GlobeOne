/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * UnAccountorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.payment.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.payment.usecase.*

@Module(subcomponents = [PaymentComponent::class])
internal interface PaymentModule

@ManagerScope
@Subcomponent
interface PaymentComponent {

    fun provideCreateAdyenPaymentSessionUseCase(): CreateAdyenPaymentSessionUseCase

    fun provideSubmitAdyenPaymentUseCase(): SubmitAdyenPaymentUseCase

    fun provideConfirmAdyenPaymentUseCase(): ConfirmAdyenPaymentUseCase

    fun provideCreateGCashPaymentSessionUseCase(): CreateGCashPaymentSessionUseCase

    fun provideGetGCashBalanceUseCase(): GetGCashBalanceUseCase

    fun provideCheckPaymentSuccessfulUseCase(): CheckPaymentSuccessfulUseCase

    fun provideGetPaymentMethodUseCase(): GetPaymentMethodUseCase

    fun provideDeletePaymentMethodUseCase(): DeletePaymentMethodUseCase

    fun provideLinkGCashAccountUseCase(): LinkGCashAccountUseCase

    fun provideUnlinkGCashAccountUseCase(): UnlinkGCashAccountUseCase

    fun providePurchaseUseCase(): PurchaseUseCase

    fun provideCreateServiceOrderUseCase(): CreateServiceOrderUseCase

    fun provideGetPaymentsUseCase(): GetPaymentsUseCase

    fun provideGetPaymentReceiptUseCase(): GetPaymentReceiptUseCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): PaymentComponent
    }
}
