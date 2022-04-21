package ph.com.globe.model.prepaid

import ph.com.globe.model.prepaid.PrepaidLedgerTransactionItem.PrepaidType.*
import java.io.Serializable
import kotlin.math.roundToInt

data class PrepaidTransactions(
    val list: List<PrepaidLedgerTransactionItem>,
    val lastTransactionKey: String? = null,
    val morePage: Boolean = false
)

data class PrepaidLedgerTransactionItem(
    val id: Int,
    val type: PrepaidType,
    val sourceMobileNumber: String,
    val targetMobileNumber: String?,
    val transactionCount: Int? = 0,
    val chargeAmount: Double = 0.0,
    val transactionTypeMapping: String?,
    val eventStartDate: String,
    val serviceCharge: Double = 0.0,
    val channel: String?,
    val serviceName: String?,
    val durationCount: String?,
    val dataVolumeCount: String?,
    val unitOfMeasurementCode: String?
) : Serializable {

    sealed class PrepaidType : Serializable {

        data class Load(val type: LoadType) : PrepaidType() {
            enum class LoadType {
                LOAD_BOUGHT, LOAD_RECEIVED, LOAD_SHARED, LOAD_LOANED, LOAN_PAID, LOAD_EXPIRED
            }
        }

        object Data : PrepaidType()

        data class Promo(val type: PromoType) : PrepaidType() {
            enum class PromoType {
                PROMO_BOUGHT, PROMO_LOANED, PROMO_RECEIVED, PROMO_SHARED, PROMO_RECEIVED_OTHERS
            }
        }

        data class Call(val type: CallType) : PrepaidType() {
            enum class CallType {
                CALL_MADE, CALL_RECEIVED
            }
        }

        data class Text(val type: TextType) : PrepaidType() {
            enum class TextType {
                TEXT_RECEIVED, TEXT_REFUNDED, TEXT_SENT
            }
        }

        object None : PrepaidType()
    }
}

private const val MOBILE_NUMBER_PREFIX = "0"
private const val PH_NUMBER_PREFIX = "9"

fun String.convertToPrefixNumberFormat(): String {
    return when {
        this.startsWith(PH_NUMBER_PREFIX) -> {
            MOBILE_NUMBER_PREFIX + this
        }
        else -> {
            this
        }
    }
}

fun String.addPrefixViaTransactionMap(transactionTypeMapping: String): String {
    return if (transactionTypeMapping == "Local number to Local number") {
        MOBILE_NUMBER_PREFIX + this
    } else {
        this
    }
}

fun getPrepaidTransactionType(
    msisdn: String?,
    model: PrepaidLedgerModel
): PrepaidLedgerTransactionItem.PrepaidType {
    with(model) {
        return when {
            topupCut == "load bought" -> {
                Load(Load.LoadType.LOAD_BOUGHT)
            }
            topupCut == "load received" -> {
                Load(Load.LoadType.LOAD_RECEIVED)
            }
            topupCut == "load shared" -> {
                Load(Load.LoadType.LOAD_SHARED)
            }
            topupCut == "load loaned" -> {
                Load(Load.LoadType.LOAD_LOANED)
            }
            topupCut == "load expired" -> {
                Load(Load.LoadType.LOAD_EXPIRED)
            }
            topupCut == "loan paid" -> {
                Load(Load.LoadType.LOAN_PAID)
            }
            promoCut == "promo received" -> {
                Promo(Promo.PromoType.PROMO_RECEIVED)
            }
            promoCut == "promo bought" -> {
                Promo(Promo.PromoType.PROMO_BOUGHT)
            }
            promoCut == "promo shared" -> {
                Promo(Promo.PromoType.PROMO_SHARED)
            }
            promoCut == "promo loaned" -> {
                Promo(Promo.PromoType.PROMO_LOANED)
            }
            promoCut == "promo received (others)" -> {
                Promo(Promo.PromoType.PROMO_RECEIVED_OTHERS)
            }
            unitOfMeasurementCode == TIME_BASED || unitOfMeasurementCode == VOLUME_BASED -> {
                Data
            }
            msisdn == sourceMobileNumber?.convertToPrefixNumberFormat() && refundIndicator == null -> {
                Call(Call.CallType.CALL_MADE)
            }
            msisdn == targetMobileNumber?.convertToPrefixNumberFormat() && refundIndicator == null -> {
                Call(Call.CallType.CALL_RECEIVED)
            }
            msisdn == sourceMobileNumber?.convertToPrefixNumberFormat() && refundIndicator == REFUND_INDICATOR_N -> {
                Text(Text.TextType.TEXT_SENT)
            }
            msisdn == targetMobileNumber?.convertToPrefixNumberFormat() && refundIndicator == REFUND_INDICATOR_N -> {
                Text(Text.TextType.TEXT_RECEIVED)
            }
            refundIndicator == REFUND_INDICATOR_Y -> {
                Text(Text.TextType.TEXT_REFUNDED)
            }
            else -> {
                None
            }
        }
    }
}

fun getTransactionTypeMapping(model: PrepaidLedgerModel): String {
    with(model) {
        return when (usage?.networkRoamingTypeCode + usage?.destinationTypeCode + usage?.directionCode) {
            "RIMO" -> "Roaming number to International number"
            "RNMO" -> "Roaming number to Local number"
            "RNMT" -> "Roaming number to Local number"
            "RIMT" -> "Roaming number to International number"
            "HNMO" -> "Local number to Local number"
            "HIMO" -> "Local number to International number"
            else -> "Others"
        }
    }
}

fun PrepaidLedgerModel.toPrepaidLedgerTransactionItem(
    type: PrepaidLedgerTransactionItem.PrepaidType,
    transactionTypeMapping: String
) = PrepaidLedgerTransactionItem(
    System.currentTimeMillis().toInt() * 123,
    type,
    sourceMobileNumber!!.addPrefixViaTransactionMap(transactionTypeMapping),
    targetMobileNumber?.addPrefixViaTransactionMap(transactionTypeMapping),
    transactionCount?.toDouble()?.roundToInt(),
    chargeAmount?.toDoubleOrNull() ?: 0.0,
    transactionTypeMapping,
    eventStartDate,
    serviceCharge?.toDoubleOrNull() ?: 0.0,
    channel,
    serviceName,
    durationCount,
    dataVolumeCount,
    unitOfMeasurementCode
)

//refund indicator
const val REFUND_INDICATOR_Y = "Y"
const val REFUND_INDICATOR_N = "N"

//data type
const val TIME_BASED = "S"
const val VOLUME_BASED = "K"
