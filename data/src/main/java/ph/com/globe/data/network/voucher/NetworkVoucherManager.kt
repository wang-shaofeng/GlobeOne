/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.network.voucher

import ph.com.globe.domain.voucher.VoucherDataManager
import ph.com.globe.errors.voucher.GetLoyaltySubscribersCouponDetailsError
import ph.com.globe.errors.voucher.GetPromoVouchersError
import ph.com.globe.errors.voucher.MarkVouchersAsUsedError
import ph.com.globe.errors.voucher.RetrieveUsedVouchersError
import ph.com.globe.model.voucher.*
import ph.com.globe.util.LfResult
import javax.inject.Inject

class NetworkVoucherManager @Inject constructor(
    factory: VoucherComponent.Factory
) : VoucherDataManager {

    private val voucherComponent = factory.create()

    override suspend fun markVoucherAsUsed(params: MarkVouchersAsUsedParams): LfResult<Unit, MarkVouchersAsUsedError> =
        voucherComponent.provideMarkVoucherAsUsedNetworkCall().execute(params)

    override suspend fun getLoyaltySubscribersCouponDetails(params: GetLoyaltySubscribersCouponDetailsParams): LfResult<LoyaltySubscriberCouponDetailsResponse, GetLoyaltySubscribersCouponDetailsError> =
        voucherComponent.provideGetLoyaltySubscribersCouponDetailsNetworkCall().execute(params)

    override suspend fun retrieveUsedVouchers(params: RetrieveUsedVouchersParams): LfResult<RetrieveUsedVouchersResponse, RetrieveUsedVouchersError> =
        voucherComponent.provideRetrieveUsedVouchersNetworkCall().execute(params)

    override suspend fun getPromoVouchers(params: GetPromoVouchersParams): LfResult<PromoVouchersResponse, GetPromoVouchersError> =
        voucherComponent.provideGetPromoVouchersNetworkCall().execute(params)
}
