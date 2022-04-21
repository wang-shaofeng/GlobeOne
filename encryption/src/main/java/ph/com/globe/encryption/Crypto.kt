/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.encryption

/**
 * This interface provides encryption and decryption of byte data with automatic key handling.
 */
interface Crypto {
    /**
     * Encrypts data passed to it into new byte array.
     * The resulting encrypted byte array will probably be larger then original, so do not expect equal lengths
     * of input and output.
     *
     * @param data byte array to encrypt
     * @return new byte array with encrypted
     * @throws InterruptedException  - if Thread get's interrupted.
     * @throws CryptoOperationFailed - if operation fails for some reason, includes causing Throwable.
     */
    @Throws(InterruptedException::class)
    fun encrypt(data: ByteArray): ByteArray

    /**
     * Decrypts data passed to it into new byte array.
     * If you pass the output of [.encrypt] to this method you will get new byte array with the
     * same contents as the input to the [.encrypt].
     *
     * @param encryptedData byte array to decrypt
     * @return new byte array with decrypted data
     * @throws InterruptedException  - if Thread get's interrupted.
     * @throws CryptoOperationFailed - if operation fails for some reason, includes causing Throwable.
     */
    @Throws(InterruptedException::class)
    fun decrypt(encryptedData: ByteArray): ByteArray
}
