/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account

import ph.com.globe.model.group.domain_models.UsageItem
import ph.com.globe.model.util.brand.AccountBrand
import java.util.*

sealed class BrandStatus {
    data class Success(
        val uiAccountBrand: UIAccountBrand
    ) : BrandStatus()

    object Empty : BrandStatus()
    object Loading : BrandStatus()
}

sealed class UIAccountBrand(val brand: AccountBrand) {
    class RegularBrand(brand: AccountBrand): UIAccountBrand(brand)
    class PlatinumBrand(brand: AccountBrand): UIAccountBrand(brand)
}

fun BrandStatus?.getBrandSafely(): AccountBrand? =
    if (this is BrandStatus.Success) uiAccountBrand.brand else null

sealed class BalanceStatus {
    data class Success(
        val balance: Float,
        val expiryDate: String = "",
        val expiringAmount: String = ""
    ) : BalanceStatus()

    data class Empty(
        val linkGCash: Boolean = false
    ) : BalanceStatus()

    object Loading : BalanceStatus()
}

sealed class BillStatus {
    data class Success(
        val paymentStatus: PostpaidPaymentStatus?,
        val disconnected: Boolean,
        val dueDate: Date?,
        val billAmount: String?
    ) : BillStatus()

    object Error : BillStatus()

    object Loading : BillStatus()
}

sealed class DataUsageStatus {
    data class Success(
        val usageItems: List<UsageItem>
    ) : DataUsageStatus()

    object Empty : DataUsageStatus()
    object Loading : DataUsageStatus()
}
