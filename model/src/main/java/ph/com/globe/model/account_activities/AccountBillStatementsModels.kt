/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.model.account_activities

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AccountBillStatementsHistoryResponseModel(
    val result: AccountBillStatementsHistoryResultModel
)

@JsonClass(generateAdapter = true)
data class AccountBillStatementsHistoryResultModel(
    val billingStatements: List<AccountBillStatementHistoryModel>?
)

@JsonClass(generateAdapter = true)
data class AccountBillStatementHistoryModel(
    val id: String?,
    val billStartDate: String?,
    val billEndDate: String?,
    val totalAmount: Double?
)

data class AccountBillStatementHistory(
    val id: String,
    val billStartDate: String,
    val billEndDate: String,
    val totalAmount: Double
) : Serializable
