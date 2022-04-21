/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.encryption.api

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import javax.inject.Inject

/**
 * Used by [DefaultStringCrypto] for safe encryption/decryption.
 * Reason why this is needed is because of the way [String] handles calls to [String.getBytes].
 * If you don't provide the right encoding (exact or superset of encoding), [String.getBytes] will silently replace
 * some characters that can't be represented (by encoding supplied). That breaks encryption/decryption cycle.
 * We throw [CharacterCodingException] and fail fast, to prevent the issue.
 */
class StrictStringEncoderDecoder(private val charset: Charset) {

    @Inject
    constructor() : this(Charset.forName("UTF-8"))

    /**
     * Strict encoding of bytes to their byte array representation in [Character] provided.
     *
     * @param original String to encode
     * @return encoded bytes
     * @throws CharacterCodingException if input is malformed or contains unmappable character(s)
     */
    fun toCharsetBytes(original: String): ByteArray {
        val encoder = charset.newEncoder()
        encoder.onMalformedInput(CodingErrorAction.REPORT)
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT)
        val encoded = encoder.encode(CharBuffer.wrap(original))
        val byteData = ByteArray(encoded.limit())
        encoded.get(byteData);
        return byteData
    }

    /**
     * Strict decoding of bytes to their String representation in [Character] provided.
     *
     * @param charsetBytes String to decode
     * @return decoded bytes
     * @throws CharacterCodingException if input is malformed or contains unmappable character(s)
     */
    fun toOriginal(charsetBytes: ByteArray): String {
        val decoder = charset.newDecoder()
        decoder.onMalformedInput(CodingErrorAction.REPORT)
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT)
        val decoded = decoder.decode(ByteBuffer.wrap(charsetBytes))
        val carData = CharArray(decoded.limit())
        decoded.get(carData)
        return String(carData)
    }
}
