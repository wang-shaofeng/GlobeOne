/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.encryption.api

import android.util.Base64
import ph.com.globe.encryption.Crypto
import ph.com.globe.encryption.CryptoOperationFailed
import ph.com.globe.encryption.StringCrypto
import java.nio.charset.CharacterCodingException
import javax.inject.Inject

/**
 * Default implementation of [StringCrypto] interface.
 */
internal class DefaultStringCrypto @Inject constructor(
    private val crypto: Crypto,
    private val converter: StrictStringEncoderDecoder
) : StringCrypto {

    @Synchronized
    @Throws(InterruptedException::class)
    override fun encrypt(data: String): String {
        val charsetBytes = toCharsetBytesOrThrow(data)
        val encryptedBytes = crypto.encrypt(charsetBytes)
        return base64StringCatcher { Base64.encodeToString(encryptedBytes, Base64.DEFAULT) }
    }

    private fun toCharsetBytesOrThrow(data: String): ByteArray {
        return try {
            converter.toCharsetBytes(data)
        } catch (e: CharacterCodingException) {
            throw CryptoOperationFailed(e)
        }
    }

    @Synchronized
    @Throws(InterruptedException::class)
    override fun decrypt(encryptedData: String): String {
        val encryptedBytes =
            base64ByteArrayCatcher { Base64.decode(encryptedData, Base64.DEFAULT) }
        val original = crypto.decrypt(encryptedBytes)
        return toOriginalStringOrThrow(original)
    }

    private fun toOriginalStringOrThrow(original: ByteArray): String {
        return try {
            converter.toOriginal(original)
        } catch (e: CharacterCodingException) {
            throw CryptoOperationFailed(e)
        }
    }

    private fun base64ByteArrayCatcher(operation: () -> ByteArray): ByteArray =
        try {
            operation()
        } catch (e: IllegalArgumentException) {
            throw CryptoOperationFailed(e)
        }

    private fun base64StringCatcher(operation: () -> String): String =
        try {
            operation()
        } catch (e: IllegalArgumentException) {
            throw CryptoOperationFailed(e)
        }
}
