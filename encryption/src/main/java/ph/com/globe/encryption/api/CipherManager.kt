/*
 * Copyright (C) 2022 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption.api

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject


internal open class CipherManager @Inject constructor() {

    @Throws(InterruptedException::class)
    open fun getOrGenerateCipher(secret: String): Cipher {
        val cipher = Cipher.getInstance(ECB_MODE)
        cipher?.init(Cipher.ENCRYPT_MODE, getSecretKey(secret.toByteArray(), secret.length))
        return cipher
    }

    private fun getSecretKey(key: ByteArray, keyLength: Int) = SecretKeySpec(key, 0, keyLength, "AES")

    private companion object {
        const val ECB_MODE = "AES/ECB/PKCS5Padding"
    }
}
