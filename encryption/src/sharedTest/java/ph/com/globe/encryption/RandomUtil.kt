/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.encryption

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.*

/**
 * Random utility class to generate random data for tests. It's SEEDED so the tests are reproducible.
 */
object RandomUtil {
    private const val MAX_LENGTH = 512
    private const val SEED = 9742
    private val RANDOM = Random(SEED.toLong())
    @JvmOverloads
    fun randomBytes(maxLength: Int = MAX_LENGTH): ByteArray {
        val rndBytes = ByteArray(maxLength)
        RANDOM.nextBytes(rndBytes)
        return rndBytes
    }

    fun randomEmptyBytes(): ByteArray {
        return ByteArray(randomLength())
    }

    /**
     * Generates [String]s that are random, with replaced characters if some of the random bytes generated
     * can't be represented by [Charset.defaultCharset].
     *
     * @return random String in [Charset.defaultCharset]
     */
    fun generateFixedRandomDefaultCharsetStrings(): String {
        val original = String(randomBytes(), Charset.defaultCharset())
        val encoder = Charset.defaultCharset().newEncoder()
        encoder.onMalformedInput(CodingErrorAction.REPLACE)
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE)
        val encoded: ByteBuffer = try {
            encoder.encode(CharBuffer.wrap(original))
        } catch (e: CharacterCodingException) {
            throw IllegalStateException("Shouldn't happen ", e)
        }
        val byteData = ByteArray(encoded.limit())
        encoded[byteData]
        return String(byteData, Charset.defaultCharset())
    }

    private fun randomLength(): Int {
        return RANDOM.nextInt(MAX_LENGTH)
    }
}
