/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.voucher.usecases

import ph.com.globe.domain.voucher.VoucherDataManager
import ph.com.globe.errors.voucher.GetLoyaltySubscribersCouponDetailsError
import ph.com.globe.model.voucher.*
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetLoyaltySubscribersCouponDetailsUseCase @Inject constructor(
    private val voucherManager: VoucherDataManager
) {

    suspend fun execute(params: GetLoyaltySubscribersCouponDetailsParams): LfResult<LoyaltySubscriberCouponDetailsResult, GetLoyaltySubscribersCouponDetailsError> =
        voucherManager.getLoyaltySubscribersCouponDetails(params).fold(
            { response ->
                /**
                 * don't early filt applicable data, we used all data size calculate for next request params 'offset'.
                 * we filt data for display, but server dont care the filter rules, they need whole data's 'offset' param.
                 */
                LfResult.success(response.result)
            }, {
                LfResult.failure(it)
            }
        )
}
