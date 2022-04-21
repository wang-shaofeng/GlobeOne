package ph.com.globe.data.shared_preferences.token.utils

import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT
import ph.com.globe.data.shared_preferences.token.TokenRepository

// Extension function to get the UUID from user token
// Returns null if the user is not logged in
fun TokenRepository.getUUID(): String? =
    try {
        getUserToken().successOrNull()?.let {
            JWT(it).claims[UUID_KEY]?.asString()
        }
    } catch (e: DecodeException) {
        null
    }

// User JWT token contains UUID value under this key
private const val UUID_KEY = "uuid"
