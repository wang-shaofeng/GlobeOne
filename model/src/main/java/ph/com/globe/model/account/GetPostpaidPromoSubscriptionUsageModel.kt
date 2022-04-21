package ph.com.globe.model.account

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.network_models.PromoSubscriptionUsageJson

@JsonClass(generateAdapter = true)
data class GetPostpaidPromoSubscriptionUsageRequest(
    val serviceNumber: String,
    val forceRefresh: Boolean? = true
)

@JsonClass(generateAdapter = true)
data class GetPostpaidPromoSubscriptionUsageResponse(
    val promoSubscriptionUsage: PromoSubscriptionUsageJson
)
