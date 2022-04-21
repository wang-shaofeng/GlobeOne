package ph.com.globe.model.account

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetPostpaidActivePromoSubscriptionRequest(
    val serviceNumber: String,
    val forceRefresh: Boolean? = true,
    val primaryResourceType: String = "C"
)

@JsonClass(generateAdapter = true)
data class GetPostpaidActivePromoSubscriptionResponse(
    val activePromoSubscriptions: PostpaidActivePromoSubscriptionsJson
)

@JsonClass(generateAdapter = true)
data class PostpaidActivePromoSubscriptionsJson(
    val callAndText: List<PostpaidSubscriptionJson>?,
    val lifeStyle: List<PostpaidSubscriptionJson>?,
    val addOn: List<PromoActiveSubscriptionJson>?
)

@JsonClass(generateAdapter = true)
data class PostpaidSubscriptionJson(
    val offerName: String?,
    val offerDescription: String?,
    val callsRemaining: String?,
    val callsTotal: String?,
    val smsRemaining: String?,
    val smsTotal: String?,
    val expiryDate: String?,
)
