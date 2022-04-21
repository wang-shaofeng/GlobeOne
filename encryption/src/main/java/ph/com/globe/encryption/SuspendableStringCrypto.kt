/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption

/**
 * This interface provides encryption and decryption of String data
 * with automatic key handling.
 *
 *
 * The implementations should probably use [StrictStringEncoderDecoder] to
 * avoid silent problems of encoding/decoding of strings to/from byte arrays.
 */
interface SuspendableStringCrypto {
    /**
     * Encrypts String passed to it.
     * The resulting encrypted String will probably be larger then original, so do not expect equal lengths
     * of input and output.
     *
     *
     * The implementation should probably retry an operation few times since crypto providers can
     * sometimes fail for whatever reason.
     *
     * @param data String to encrypt
     * @return encrypted String
     * @throws CryptoOperationFailed - if operation fails for some reason, after a few retries.
     */
    suspend fun encrypt(data: String): String

    /**
     * Decrypts String passed to it.
     * If you pass the output of [.encrypt] to this method you will get
     * String with same contents as the input to the [.encrypt].
     *
     *
     * The implementation should probably retry an operation few times since crypto providers can
     * sometimes fail for whatever reason.
     *
     * @param encryptedData String to decrypt
     * @return encrypted String
     * @throws CryptoOperationFailed - if operation fails for some reason, after a few retries.
     */
    suspend fun decrypt(encryptedData: String): String
}
