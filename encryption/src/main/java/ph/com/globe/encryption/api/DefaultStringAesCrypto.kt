/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption.api

import android.util.Base64
import ph.com.globe.encryption.CryptoOperationFailed
import ph.com.globe.encryption.StringAesCrypto
import javax.crypto.Cipher
import javax.inject.Inject

internal class DefaultStringAesCrypto @Inject constructor(
    private val cipherManager: CipherManager
): StringAesCrypto {

    @Synchronized
    @Throws(InterruptedException::class)
    override fun encrypt(data: String, secretKey: String): String  = cryptoOperationOrThrowCryptoOperationFailed ({
        Base64.encodeToString(doFinal(data.toByteArray()), Base64.DEFAULT)
    }, secretKey)

    private inline fun cryptoOperationOrThrowCryptoOperationFailed(crossinline cryptoOperation: Cipher.() -> String, secret: String): String {
        try {
            return cipherManager.getOrGenerateCipher(secret).cryptoOperation()
        } catch (interrupt: InterruptedException) {
            throw interrupt
        } catch (cryptoFail: Exception) {
            throw CryptoOperationFailed("Crypto operation failed", cryptoFail)
        }
    }
}
