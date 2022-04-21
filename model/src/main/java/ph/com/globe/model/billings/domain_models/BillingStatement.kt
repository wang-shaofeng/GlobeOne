package ph.com.globe.model.billings.domain_models

import java.io.Serializable

data class BillingStatement(
    val id: String?,
    val type: String?,
    val status: String?,
    val billingDate: String?,
    val billStartDate: String?,
    val billEndDate: String?,
    val baNo: String?,
    val dueDate: String?,
    val totalAmount: Double?,
    val invoiceAmount: Double?,
    val invoiceNetAmount: Double?,
    val verificationToken: String?
): Serializable
