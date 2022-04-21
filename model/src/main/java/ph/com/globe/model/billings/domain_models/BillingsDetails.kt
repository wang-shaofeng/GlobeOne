package ph.com.globe.model.billings.domain_models

import ph.com.globe.model.account.PostpaidPaymentStatus

data class BillingsDetails(
    val billStartDate: String?,
    val billEndDate: String?,
    val dueDate: String,
    val cutOffDate: String?,
    val billingArrangementId: String?,
    val billingAddress: BillingAddress,
    val billAmount: String?,
    val outstandingBalance: String,
    val balanceStatus: String?,
    val lastPaymentDate: String,
    val lastPaymentAmount: String,
    val totalExcessCharges: String?,
    val excessCharges: ExcessCharges?
)

data class BillingAddress(
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

data class ExcessCharges(
    val data: String,
    val call: String,
    val vas: String,
    val text: String,
    val others: String,
)

fun BillingsDetails.getPostpaidPaymentStatus(): PostpaidPaymentStatus =
    when (balanceStatus) {
        ALL_SET -> PostpaidPaymentStatus.AllSet
        DUE_SOON -> PostpaidPaymentStatus.BillDueSoon
        OVERDUE -> PostpaidPaymentStatus.BillOverdue
        // Empty or null balance status should be handled as 'Due Soon'
        else -> PostpaidPaymentStatus.BillDueSoon
    }

private const val ALL_SET = "All Set"
private const val DUE_SOON = "Due Soon"
private const val OVERDUE = "Overdue"

const val BILLING_DETAILS_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ"
