/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption.api

import com.google.crypto.tink.Aead
import ph.com.globe.encryption.Crypto
import ph.com.globe.encryption.CryptoOperationFailed
import ph.com.globe.encryption.EncryptionScope
import javax.inject.Inject

@EncryptionScope
internal class TinkCrypto @Inject constructor(
    private val aeadManager: AeadManager
) : Crypto {

    @Synchronized
    @Throws(InterruptedException::class)
    override fun encrypt(data: ByteArray): ByteArray =
        cryptoOperationOrThrowCryptoOperationFailed {
            encrypt(data, null)
        }

    @Synchronized
    @Throws(InterruptedException::class)
    override fun decrypt(encryptedData: ByteArray): ByteArray =
        cryptoOperationOrThrowCryptoOperationFailed {
            decrypt(encryptedData, null)
        }

    private inline fun cryptoOperationOrThrowCryptoOperationFailed(crossinline cryptoOperation: Aead.() -> ByteArray): ByteArray {
        try {
            return aeadManager.getOrGenerateAead().cryptoOperation()
        } catch (interrupt: InterruptedException) {
            throw interrupt
        } catch (cryptoFail: Exception) {
            aeadManager.clearAeadAndStopTryingToUseMasterKey()
            throw CryptoOperationFailed("Crypto operation failed", cryptoFail)
        }
    }
}
