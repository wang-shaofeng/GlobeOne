/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.voucher

import ph.com.globe.errors.voucher.GetLoyaltySubscribersCouponDetailsError
import ph.com.globe.errors.voucher.GetPromoVouchersError
import ph.com.globe.errors.voucher.MarkVouchersAsUsedError
import ph.com.globe.errors.voucher.RetrieveUsedVouchersError
import ph.com.globe.model.voucher.*
import ph.com.globe.util.LfResult

interface VoucherDataManager {

    suspend fun markVoucherAsUsed(params: MarkVouchersAsUsedParams): LfResult<Unit, MarkVouchersAsUsedError>

    suspend fun getLoyaltySubscribersCouponDetails(params: GetLoyaltySubscribersCouponDetailsParams): LfResult<LoyaltySubscriberCouponDetailsResponse, GetLoyaltySubscribersCouponDetailsError>

    suspend fun retrieveUsedVouchers(params: RetrieveUsedVouchersParams): LfResult<RetrieveUsedVouchersResponse, RetrieveUsedVouchersError>

    suspend fun getPromoVouchers(params: GetPromoVouchersParams): LfResult<PromoVouchersResponse, GetPromoVouchersError>
}
