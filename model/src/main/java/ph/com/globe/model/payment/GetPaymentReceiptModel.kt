/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.payment

data class GetPaymentReceiptParams(
    val receiptId: String,
    val token: String
)
