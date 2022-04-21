/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.voucher.di

import dagger.Module
import dagger.Subcomponent
import ph.com.globe.domain.ManagerScope
import ph.com.globe.domain.voucher.usecases.GetLoyaltySubscribersCouponDetailsUseCase
import ph.com.globe.domain.voucher.usecases.GetPromoVouchersUserCase
import ph.com.globe.domain.voucher.usecases.MarkVoucherAsUsedUseCase
import ph.com.globe.domain.voucher.usecases.RetrieveUsedVouchersUseCase

@Module(subcomponents = [VoucherComponent::class])
internal interface VoucherModule

@ManagerScope
@Subcomponent
interface VoucherComponent {

    fun provideGetLoyaltySubscribersCouponDetailsUseCase(): GetLoyaltySubscribersCouponDetailsUseCase

    fun provideMarkVoucherAsUsedUseCase(): MarkVoucherAsUsedUseCase

    fun provideRetrieveUsedVouchersUseCase(): RetrieveUsedVouchersUseCase

    fun provideGetPromoVouchersUserCase(): GetPromoVouchersUserCase

    @Subcomponent.Factory
    interface Factory {
        fun create(): VoucherComponent
    }
}
