package com.sleeptracker.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.sleeptracker.storage.SecureStorage
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// MIGRATION: TypeScript `CryptoJS.AES.encrypt(data, key, { mode: CBC, padding: PKCS7, iv })`
// → Android `javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")`
// PKCS5Padding is Java's name for PKCS7Padding — they are identical for 128-bit blocks.
//
// KEY COMPATIBILITY:
// The TypeScript source stores a Base64-encoded random 256-bit key in SecureStore.
// Key name: "myAppEncryptionKey"
// We load this same key from Android EncryptedSharedPreferences (AndroidSecureStorage).
// This preserves decryption compatibility with existing user data from the RN app.
//
// CIPHER FORMAT PRESERVED:
// TypeScript: `iv.toString(CryptoJS.enc.Base64) + ':' + encrypted.toString()`
// Kotlin:     "$ivBase64:$ciphertextBase64"  (same colon-separated format)
//
// MIGRATION_FLAG: The spec requires PBKDF2, but adding it would break existing encrypted data.
// We preserve the raw key approach for backwards compatibility.

actual class EncryptionService {
    private var encryptionKey: ByteArray? = null

    // MIGRATION: TypeScript `isInitialized: Promise<void>` → Kotlin `initialize(): Unit` (suspend)
    actual suspend fun initialize() {
        val stored = SecureStorage.INSTANCE?.getItem(ENCRYPTION_KEY_NAME)
        encryptionKey = if (stored != null) {
            Base64.decode(stored, Base64.DEFAULT)
        } else {
            // Generate new 256-bit random key
            val newKey = generateRandom256BitKey()
            val base64Key = Base64.encodeToString(newKey, Base64.NO_WRAP)
            SecureStorage.INSTANCE?.setItem(ENCRYPTION_KEY_NAME, base64Key)
            newKey
        }
    }

    // MIGRATION: TypeScript `CryptoJS.AES.encrypt(data, parsedKey, { iv, mode: CBC, padding: PKCS7 })`
    // Returns "IV_base64:ciphertext_base64" — same format as TypeScript
    actual suspend fun encrypt(data: String): String {
        val key = encryptionKey ?: throw IllegalStateException("EncryptionService not initialized")
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

        // Generate random 128-bit (16-byte) IV — same as TypeScript `WordArray.random(128/8)`
        val iv = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))

        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val ciphertextBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        // MIGRATION: "IV:Ciphertext" format preserved for cross-platform compatibility
        return "$ivBase64:$ciphertextBase64"
    }

    // MIGRATION: TypeScript `CryptoJS.AES.decrypt(ciphertextBase64, parsedKey, { iv, mode: CBC, padding: PKCS7 })`
    actual suspend fun decrypt(encryptedBase64: String): String {
        val key = encryptionKey ?: throw IllegalStateException("EncryptionService not initialized")
        val parts = encryptedBase64.split(":")
        if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted data format. Expected \"IV:Ciphertext\".")

        val iv = Base64.decode(parts[0], Base64.DEFAULT)
        val ciphertext = Base64.decode(parts[1], Base64.DEFAULT)

        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))

        return cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
    }

    private fun generateRandom256BitKey(): ByteArray =
        ByteArray(32).also { java.security.SecureRandom().nextBytes(it) }
}
