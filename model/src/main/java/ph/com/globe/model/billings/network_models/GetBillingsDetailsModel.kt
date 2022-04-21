package ph.com.globe.model.billings.network_models

import com.squareup.moshi.JsonClass
import ph.com.globe.model.account.UsageUIModel
import ph.com.globe.model.billings.domain_models.BillingAddress
import ph.com.globe.model.billings.domain_models.BillingsDetails
import ph.com.globe.model.billings.domain_models.ExcessCharges
import ph.com.globe.model.billings.domain_models.getPostpaidPaymentStatus
import ph.com.globe.model.profile.domain_models.EnrolledAccount
import ph.com.globe.model.util.brand.*

data class GetBillingsDetailsParams(
    var accountNumber: String? = null,
    var landlineNumber: String? = null,
    var mobileNumber: String? = null,
    val brandType: AccountBrandType,
    val segment: AccountSegment,
    val accountType: String? = null
)

@JsonClass(generateAdapter = true)
data class GetBillingsDetailsResponse(
    val result: GetBillingsDetailsResult
)

@JsonClass(generateAdapter = true)
data class GetBillingsDetailsResult(
    val billStartDate: String?,
    val billEndDate: String?,
    val dueDate: String,
    val cutOffDate: String?,
    val billingArrangementId: String?,
    val billingAddress: BillingAddressResult,
    val billAmount: String?,
    val outstandingBalance: String,
    val balanceStatus: String?,
    val lastPaymentDate: String,
    val lastPaymentAmount: String,
    val totalExcessCharges: String?,
    val excessCharges: ExcessChargesResult?
)

@JsonClass(generateAdapter = true)
data class BillingAddressResult(
    val country: String?,
    val region: String?,
    val province: String,
    val city: String,
    val barangay: String?,
    val street: String,
    val building: String?,
    val houseNumber: String?,
    val postalCode: String?,
    val subdivisionVillage: String?,
    val houseNo: String?,
    val floorNo: String?
)

@JsonClass(generateAdapter = true)
data class ExcessChargesResult(
    val data: String,
    val call: String,
    val vas: String,
    val text: String,
    val others: String,
)

fun GetBillingsDetailsResponse.toDomain() =
    BillingsDetails(
        result.billStartDate,
        result.billEndDate,
        result.dueDate,
        result.cutOffDate,
        result.billingArrangementId,
        result.billingAddress.toDomain(),
        result.billAmount,
        result.outstandingBalance,
        result.balanceStatus,
        result.lastPaymentDate,
        result.lastPaymentAmount,
        result.totalExcessCharges,
        result.excessCharges?.toDomain()
    )

fun BillingAddressResult.toDomain() =
    BillingAddress(
        country,
        region,
        province,
        city,
        barangay,
        street,
        building,
        houseNumber,
        postalCode,
        subdivisionVillage,
        houseNo,
        floorNo
    )

fun ExcessChargesResult.toDomain() =
    ExcessCharges(
        data,
        call,
        vas,
        text,
        others
    )

fun createPostpaidBillingDetailsParams(enrolledAccount: EnrolledAccount) =
    GetBillingsDetailsParams(
        brandType = AccountBrandType.Postpaid,
        segment = enrolledAccount.segment
    ).apply {
        when (enrolledAccount.segment) {
            AccountSegment.Mobile -> {
                mobileNumber = enrolledAccount.mobileNumber
            }
            AccountSegment.Broadband -> {
                enrolledAccount.accountNumber?.let {
                    accountNumber = it
                } ?: enrolledAccount.landlineNumber?.let {
                    landlineNumber = it
                }
            }
        }
    }

fun UsageUIModel.applyBillingDetails(
    billingDetails: BillingsDetails
): UsageUIModel {

    balance = billingDetails.outstandingBalance.toFloatOrNull()
    postpaidPaymentStatus = billingDetails.getPostpaidPaymentStatus()

    return this
}
