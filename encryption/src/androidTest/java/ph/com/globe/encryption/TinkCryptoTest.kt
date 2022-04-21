/*
 * Copyright (C) 2021 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */

package ph.com.globe.encryption

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.crypto.tink.Aead
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import ph.com.globe.encryption.api.AeadManager
import ph.com.globe.encryption.api.TinkCrypto

class TinkCryptoTest {

    private lateinit var aeadManager: AeadManager
    private lateinit var crypto: TinkCrypto

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        aeadManager = spy(AeadManager(context))
        crypto = TinkCrypto(aeadManager)
    }

    @Test
    fun decryptBeforeEncrypt_shouldThrowCryptoOperationFailed_clearAeadAndSetFlagNotToUseMasterKey() {

        assertThatThrownBy { crypto.decrypt(RandomUtil.randomBytes()) }
            .isInstanceOf(CryptoOperationFailed::class.java)

        verify(aeadManager).getOrGenerateAead()
        verify(aeadManager).clearAeadAndStopTryingToUseMasterKey()
        verifyNoMoreInteractions(aeadManager)
    }

    @Test
    fun encryptionFail_clearAeadAndSetFlagNotToUseMasterKey() {
        val expectedCause = IllegalStateException("Something went wrong")
        val mockedCrypto = mock(Aead::class.java)
        `when`(aeadManager.getOrGenerateAead()).thenReturn(mockedCrypto)
        `when`(mockedCrypto.encrypt(ArgumentMatchers.any(ByteArray::class.java), eq(null)))
            .thenThrow(expectedCause)

        assertThatThrownBy { crypto.encrypt(RandomUtil.randomBytes()) }
            .isInstanceOf(CryptoOperationFailed::class.java)
            .hasCause(expectedCause)

        verify(aeadManager).getOrGenerateAead()
        verify(aeadManager).clearAeadAndStopTryingToUseMasterKey()
        verify(mockedCrypto).encrypt(ArgumentMatchers.any(ByteArray::class.java), eq(null))

        verifyNoMoreInteractions(aeadManager, mockedCrypto)
    }

    @Test
    fun encryptionSuccessButDecryptionFail_clearAeadAndSetFlagNotToUseMasterKey() {
        val expectedCause = IllegalStateException("Something went wrong")
        val mockedCrypto = mock(Aead::class.java)
        `when`(aeadManager.getOrGenerateAead())
            .thenCallRealMethod()
            .thenReturn(mockedCrypto)
        `when`(mockedCrypto.decrypt(ArgumentMatchers.any(ByteArray::class.java), eq(null)))
            .thenThrow(expectedCause)

        val original = RandomUtil.randomBytes()
        val encryptedData = crypto.encrypt(original)
        assertThat(original).isNotEqualTo(encryptedData)

        assertThatThrownBy { crypto.decrypt(encryptedData) }
            .isInstanceOf(CryptoOperationFailed::class.java)
            .hasCause(expectedCause)

        verify(aeadManager, times(2)).getOrGenerateAead()
        verify(aeadManager).clearAeadAndStopTryingToUseMasterKey()
        verify(mockedCrypto).decrypt(eq(encryptedData), eq(null))
        verifyNoMoreInteractions(aeadManager, mockedCrypto)
    }

    @Test
    fun testEncryptDecryptSpecialCase() {
        crypto.encryptDecryptTestHelper(ByteArray(0))
        crypto.encryptDecryptTestHelper(ByteArray(1))

        verify(aeadManager, times(4)).getOrGenerateAead()
        verifyNoMoreInteractions(aeadManager)
    }

    @Test
    fun testEncryptDecryptEmptyData() {
        repeat(ITERATIONS) {
            crypto.encryptDecryptTestHelper(RandomUtil.randomEmptyBytes())
        }
        verify(aeadManager, times(2 * ITERATIONS)).getOrGenerateAead()
        verifyNoMoreInteractions(aeadManager)
    }

    @Test
    fun testEncryptDecryptRandomData() {
        repeat(ITERATIONS) {
            crypto.encryptDecryptTestHelper(RandomUtil.randomBytes())
        }
        verify(aeadManager, times(2 * ITERATIONS)).getOrGenerateAead()
        verifyNoMoreInteractions(aeadManager)
    }

    private companion object {
        const val ITERATIONS = 25
    }
}
