/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.voucher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ph.com.globe.domain.voucher.di.VoucherComponent
import ph.com.globe.errors.voucher.GetLoyaltySubscribersCouponDetailsError
import ph.com.globe.errors.voucher.GetPromoVouchersError
import ph.com.globe.errors.voucher.MarkVouchersAsUsedError
import ph.com.globe.errors.voucher.RetrieveUsedVouchersError
import ph.com.globe.model.voucher.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class VoucherUseCaseManager @Inject constructor(
    factory: VoucherComponent.Factory
) : VoucherDomainManager {

    private val voucherComponent = factory.create()

    override suspend fun getLoyaltySubscribersCouponDetails(params: GetLoyaltySubscribersCouponDetailsParams): LfResult<LoyaltySubscriberCouponDetailsResult, GetLoyaltySubscribersCouponDetailsError> =
        withContext(Dispatchers.IO) {
            voucherComponent.provideGetLoyaltySubscribersCouponDetailsUseCase().execute(params)
        }

    override suspend fun markVoucherAsUsed(params: MarkVouchersAsUsedParams): LfResult<Unit, MarkVouchersAsUsedError> =
        withContext(Dispatchers.IO) {
            voucherComponent.provideMarkVoucherAsUsedUseCase().execute(params)
        }

    override suspend fun retrieveUsedVouchers(params: RetrieveUsedVouchersParams): LfResult<RetrieveUsedVouchersResponse, RetrieveUsedVouchersError> =
        withContext(Dispatchers.IO) {
            voucherComponent.provideRetrieveUsedVouchersUseCase().execute(params)
        }

    override suspend fun getPromoVouchers(params: GetPromoVouchersParams): LfResult<PromoVouchersResult, GetPromoVouchersError> =
        withContext(Dispatchers.IO) {
            voucherComponent.provideGetPromoVouchersUserCase().execute(params)
        }
}
