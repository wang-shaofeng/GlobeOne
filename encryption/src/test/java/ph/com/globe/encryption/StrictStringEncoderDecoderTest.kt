/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.encryption

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import ph.com.globe.encryption.RandomUtil.generateFixedRandomDefaultCharsetStrings
import ph.com.globe.encryption.RandomUtil.randomBytes
import ph.com.globe.encryption.api.StrictStringEncoderDecoder
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset

class StrictStringEncoderDecoderTest {
    private var strictStringEncoderDecoder: StrictStringEncoderDecoder? = null

    @Before
    fun setUp() {
        strictStringEncoderDecoder = StrictStringEncoderDecoder()
    }

    @Test
    fun testConvertFromMalformedByteArray() {
        try {
            val notValidUTF8Bytes = byteArrayOf(-128, -98, 62, -128, -25)
            strictStringEncoderDecoder!!.toOriginal(notValidUTF8Bytes)
        } catch (e: Exception) {
            Assertions.assertThat(e is CharacterCodingException).isEqualTo(true)
        }
    }

    @Test
    fun testConvertFromStringToBytesAndBack() {
        for (i in 0 until ITERATIONS) {
            val original = generateFixedRandomDefaultCharsetStrings()
            val converted = strictStringEncoderDecoder!!.toCharsetBytes(original)
            val backData = strictStringEncoderDecoder!!.toOriginal(converted)
            Assertions.assertThat(converted).isNotEqualTo(original)
            Assertions.assertThat(backData).isEqualTo(original)
        }
    }

    @Test
    fun testConvertFromBytesToStringAndBack() {
        for (i in 0 until ITERATIONS) {
            val org = String(randomBytes(), Charset.defaultCharset())
            val original = org.toByteArray(Charset.defaultCharset())
            val converted = strictStringEncoderDecoder!!.toOriginal(original)
            val backData = strictStringEncoderDecoder!!.toCharsetBytes(converted)
            Assertions.assertThat(converted).isNotEqualTo(original)
            Assertions.assertThat(backData).isEqualTo(original)
        }
    }

    companion object {
        private const val ITERATIONS = 25
    }
}
