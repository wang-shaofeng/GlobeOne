package ph.com.globe.data.network.payment.model

import com.squareup.moshi.JsonClass
import ph.com.globe.model.payment.*
import ph.com.globe.model.util.brand.AccountSegment

@JsonClass(generateAdapter = true)
data class CreateAdyenPaymentSessionRequest(
    val paymentType: String,
    val currency: String = "PHP",
    val countryCode: String = "PH",
    val paymentInformation: AdyenPaymentInformation,
    val settlementInformation: List<SettlementInformation>,
)

@JsonClass(generateAdapter = true)
data class CreateGCashPaymentSessionRequest(
    val paymentType: String,
    val currency: String = "PHP",
    val countryCode: String = "PH",
    val paymentInformation: GCashPaymentInformation,
    val settlementInformation: List<SettlementInformation>,
)

@JsonClass(generateAdapter = true)
data class AdyenPaymentInformation(
    val platform: String = "Android",
    val responseUrl: String,
    val entityType: String?,
    val shopperLocale: String = "en_US",
    val browserInformation: BrowserInformation = BrowserInformation()
)

@JsonClass(generateAdapter = true)
data class GCashPaymentInformation(
    val notificationUrls: List<NotificationUrls> = listOf(
        NotificationUrls("PAY_RETURN", SUCCESS_URL),
        NotificationUrls("CANCEL_RETURN", CANCEL_URL)
    ),
    val signAgreementPay: Boolean = true,
    val environmentInformation: EnvironmentInformation = EnvironmentInformation(),
    val productCode: String = "",
    val order: Order,
    val subMerchantName: String? = null
)

@JsonClass(generateAdapter = true)
data class NotificationUrls(
    val type: String,
    val url: String
)

@JsonClass(generateAdapter = true)
data class EnvironmentInformation(
    val orderTerminalType: String = "APP",
    val terminalType: String = "APP",
    val extendedInfo: String? = null
)

@JsonClass(generateAdapter = true)
data class ExtendedInfo(
    val contentPortalData: ContentPortalData?
)

@JsonClass(generateAdapter = true)
data class ContentPortalData(
    val customerSegment: String,
    val subServiceType: String,
    val customerSubType: String,
    val brand: String,
    val entity: String = "Globe",
    val sku: String,
    val modeOfPayment: String = "GCash",
    val subscriberType: String = AccountSegment.Mobile.toString().uppercase(),
    val contentPartnerShortName: String,
    val srn: String = "N/A",
    val msisdn: String,
    val emailAddress: String?,
    val productDescription: String
)

@JsonClass(generateAdapter = true)
data class Order(
    val orderTitle: String
)

@JsonClass(generateAdapter = true)
data class BrowserInformation(
    val acceptHeader: String = "*/*",
    val userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36"
)

@JsonClass(generateAdapter = true)
data class SettlementInformation(
    val mobileNumber: String? = null,
    val accountNumber: String? = null,
    val emailAddress: String? = null,
    val amount: Double = 1.00,
    val transactionType: String,
    val requestType: String,
    val transactions: List<Transaction>?
)

@JsonClass(generateAdapter = true)
data class CreatePaymentSessionResponse(
    val result: TokenPaymentId
)

@JsonClass(generateAdapter = true)
data class TokenPaymentId(
    val tokenPaymentId: String
)

fun CreatePaymentSessionParams.transformToContentPortalData(): ContentPortalData? =
    (this.purchaseType as? PurchaseType.BuyContentVoucher)?.let { purchaseType ->
        with(purchaseType) {
            ContentPortalData(
                customerSegment = customerDetails?.customerTypeDescription ?: "",
                subServiceType = customerDetails?.customerTypeDescription ?: "",
                customerSubType = customerDetails?.customerSubTypeDescription ?: "",
                brand = if (brand?.isPostpaid() == true) "PT" else brand?.name.toString(),
                sku = purchaseType.denomCategory,
                contentPartnerShortName = purchaseType.partnerName,
                msisdn = mobileNumber,
                emailAddress = emailAddress,
                productDescription = productDescription
            )
        }
    }
