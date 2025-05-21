package com.sumi.pockon.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoManager {

    private const val IV_SIZE = 12
    private const val TAG_LENGTH = 128 // bits

    fun generateKeyFromUID(uid: String): SecretKey {
        val keyBytes = MessageDigest.getInstance("SHA-256").digest(uid.toByteArray())
        return SecretKeySpec(keyBytes.copyOf(16), "AES") // 128-bit key
    }

    fun encrypt(data: ByteArray, key: SecretKey): ByteArray {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val cipherText = cipher.doFinal(data)

        return iv + cipherText
    }

    fun decrypt(data: ByteArray, key: SecretKey): ByteArray {
        val iv = data.copyOfRange(0, IV_SIZE)
        val cipherText = data.copyOfRange(IV_SIZE, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        return cipher.doFinal(cipherText)
    }
}