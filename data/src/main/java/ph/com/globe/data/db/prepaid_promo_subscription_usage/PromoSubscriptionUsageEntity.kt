/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.data.db.prepaid_promo_subscription_usage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import ph.com.globe.data.db.prepaid_promo_subscription_usage.PromoSubscriptionUsageEntity.Companion.TABLE_NAME
import ph.com.globe.model.account.domain_models.DataItem
import ph.com.globe.model.account.domain_models.PromoSubscriptionUsage
import ph.com.globe.model.account.network_models.GetPrepaidPromoSubscriptionUsageResponse

@Entity(tableName = TABLE_NAME)
@JsonClass(generateAdapter = true)
data class PromoSubscriptionUsageEntity(
    @PrimaryKey(autoGenerate = false)
    val msisdn: String,
    val mainData: List<DataItemEntity>?,
    val appData: List<DataItemEntity>?
) {
    companion object {
        const val TABLE_NAME = "subscription_usages"
    }
}

@JsonClass(generateAdapter = true)
data class DataItemEntity(
    val skelligCategory: String?,
    val skelligWallet: String,
    val dataRemaining: Int?,
    val dataTotal: Int?,
    val endDate: String,
    val type: String?
)

fun GetPrepaidPromoSubscriptionUsageResponse.toEntity(msisdn: String) =
    PromoSubscriptionUsageEntity(
        msisdn,
        promoSubscriptionUsage.mainData?.map {
            DataItemEntity(
                it.skelligCategory,
                it.skelligWallet,
                it.dataRemaining,
                it.dataTotal,
                it.endDate,
                it.type
            )
        },
        promoSubscriptionUsage.appData?.map {
            DataItemEntity(
                it.skelligCategory,
                it.skelligWallet,
                it.dataRemaining,
                it.dataTotal,
                it.endDate,
                it.type
            )
        },
    )

fun PromoSubscriptionUsageEntity.toDomain() =
    PromoSubscriptionUsage(
        mainData?.map {
            DataItem(
                it.skelligCategory,
                it.skelligWallet,
                it.dataRemaining,
                it.dataTotal,
                it.endDate,
                it.type
            )
        },
        appData?.map {
            DataItem(
                it.skelligCategory,
                it.skelligWallet,
                it.dataRemaining,
                it.dataTotal,
                it.endDate,
                it.type
            )
        }
    )
