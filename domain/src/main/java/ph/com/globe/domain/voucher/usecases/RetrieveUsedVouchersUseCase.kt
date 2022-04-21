/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.domain.voucher.usecases

import ph.com.globe.domain.voucher.VoucherDataManager
import ph.com.globe.errors.voucher.RetrieveUsedVouchersError
import ph.com.globe.model.voucher.RetrieveUsedVouchersParams
import ph.com.globe.model.voucher.RetrieveUsedVouchersResponse
import ph.com.globe.util.LfResult
import javax.inject.Inject

class RetrieveUsedVouchersUseCase @Inject constructor(
    private val voucherManager: VoucherDataManager
) {

    suspend fun execute(params: RetrieveUsedVouchersParams): LfResult<RetrieveUsedVouchersResponse, RetrieveUsedVouchersError> =
        voucherManager.retrieveUsedVouchers(params)
}
