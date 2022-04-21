package ph.com.globe.model.auth

import com.squareup.moshi.JsonClass
import java.io.Serializable

data class ValidateSimSerialParams(
    val mobileNumber: String,
    val simSerial: String
)

@JsonClass(generateAdapter = true)
data class ValidateSimSerialRequest(
    val categoryIdentifier: List<String>,
    val mobileNumber: String,
    val simSerial: String,
    val mode: String,
    val channel: String
) : Serializable

@JsonClass(generateAdapter = true)
data class ValidateSimSerialResponse(
    val result: SimReferenceId
)

@JsonClass(generateAdapter = true)
data class SimReferenceId(
    val simReferenceId: String
)
