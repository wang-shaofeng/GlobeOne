/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.voucher.usecases

import ph.com.globe.domain.voucher.VoucherDataManager
import ph.com.globe.errors.voucher.GetPromoVouchersError
import ph.com.globe.model.voucher.GetPromoVouchersParams
import ph.com.globe.model.voucher.PromoVouchersResult
import ph.com.globe.util.LfResult
import ph.com.globe.util.fold
import javax.inject.Inject

class GetPromoVouchersUserCase @Inject constructor(
    private val voucherManager: VoucherDataManager
) {
    suspend fun execute(params: GetPromoVouchersParams): LfResult<PromoVouchersResult, GetPromoVouchersError> =
        voucherManager.getPromoVouchers(params).fold(
            { response ->
                LfResult.success(response.result)
            }, {
                LfResult.failure(it)
            }
        )
}
