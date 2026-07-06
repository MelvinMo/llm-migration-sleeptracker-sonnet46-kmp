package com.sleeptracker.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toCValues
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import platform.CommonCrypto.CCCrypt
import platform.CommonCrypto.kCCAlgorithmAES
import platform.CommonCrypto.kCCEncrypt
import platform.CommonCrypto.kCCDecrypt
import platform.CommonCrypto.kCCOptionPKCS7Padding
import platform.CommonCrypto.kCCKeySizeAES256
import platform.CommonCrypto.kCCBlockSizeAES128

// MIGRATION: TypeScript `CryptoJS.AES.encrypt` → iOS `CommonCrypto.CCCrypt` with AES-256-CBC.
// CommonCrypto is iOS's native crypto library, part of the Security framework.
// This preserves the same AES/CBC/PKCS7 + IV:ciphertext format as the Android implementation,
// ensuring cross-platform data compatibility.
//
// MIGRATION: Key stored in iOS Keychain (IosSecureStorage) under the same key name "myAppEncryptionKey".
// The Base64-encoded 256-bit key format is identical to Android for interoperability.

@OptIn(ExperimentalForeignApi::class)
actual class EncryptionService {
    private var encryptionKey: ByteArray? = null

    actual suspend fun initialize() {
        val stored = IosSecureStorage().getItem(ENCRYPTION_KEY_NAME)
        encryptionKey = if (stored != null) {
            NSData.create(base64EncodedString = stored, options = 0u)?.toByteArray()
        } else {
            // Generate new 256-bit random key
            val newKey = ByteArray(32)
            newKey.usePinned { pinned ->
                memScoped {
                    SecRandomCopyBytes(kSecRandomDefault, newKey.size.toULong(), pinned.addressOf(0))
                }
            }
            val base64Key = NSData.dataWithBytes(newKey.toCValues().ptr, newKey.size.toULong())
                .base64EncodedStringWithOptions(0u)
            IosSecureStorage().setItem(ENCRYPTION_KEY_NAME, base64Key)
            newKey
        }
    }

    actual suspend fun encrypt(data: String): String {
        val key = encryptionKey ?: throw IllegalStateException("EncryptionService not initialized")
        val dataBytes = data.encodeToByteArray()

        // Generate 16-byte IV using pinned buffer (correct write-back pattern)
        val iv = ByteArray(16)
        iv.usePinned { pinned ->
            memScoped {
                SecRandomCopyBytes(kSecRandomDefault, iv.size.toULong(), pinned.addressOf(0))
            }
        }

        val outputSize = dataBytes.size + kCCBlockSizeAES128.toInt()
        val output = ByteArray(outputSize)
        var numBytesEncrypted = 0uL

        // MIGRATION_FIX: Use usePinned + alloc<ULongVar> so CCCrypt can write back to
        // the actual ByteArray and the actual byte-count variable, not temporary CValues copies.
        output.usePinned { pinnedOutput ->
            memScoped {
                val outMoved = alloc<ULongVar>()
                CCCrypt(
                    op             = kCCEncrypt,
                    alg            = kCCAlgorithmAES,
                    options        = kCCOptionPKCS7Padding,
                    key            = key.toCValues().ptr,
                    keyLength      = kCCKeySizeAES256.toULong(),
                    iv             = iv.toCValues().ptr,
                    dataIn         = dataBytes.toCValues().ptr,
                    dataInLength   = dataBytes.size.toULong(),
                    dataOut        = pinnedOutput.addressOf(0),   // write directly into output ByteArray
                    dataOutAvailable = outputSize.toULong(),
                    dataOutMoved   = outMoved.ptr                 // write-back pointer for byte count
                )
                numBytesEncrypted = outMoved.value
            }
        }

        val ivBase64 = NSData.dataWithBytes(iv.toCValues().ptr, iv.size.toULong())
            .base64EncodedStringWithOptions(0u)
        val ciphertextBase64 = NSData.dataWithBytes(
            output.toCValues().ptr, numBytesEncrypted
        ).base64EncodedStringWithOptions(0u)

        // MIGRATION: Same IV:ciphertext Base64 colon-separated format as Android implementation
        return "$ivBase64:$ciphertextBase64"
    }

    actual suspend fun decrypt(encryptedBase64: String): String {
        val key = encryptionKey ?: throw IllegalStateException("EncryptionService not initialized")
        val parts = encryptedBase64.split(":")
        if (parts.size != 2) throw IllegalArgumentException("Invalid encrypted data format.")

        // MIGRATION_FIX: Use corrected toByteArray() extension (see below)
        val iv = NSData.create(base64EncodedString = parts[0], options = 0u)?.toByteArray()
            ?: throw IllegalArgumentException("Invalid IV")
        val ciphertext = NSData.create(base64EncodedString = parts[1], options = 0u)?.toByteArray()
            ?: throw IllegalArgumentException("Invalid ciphertext")

        val output = ByteArray(ciphertext.size)
        var numBytesDecrypted = 0uL

        output.usePinned { pinnedOutput ->
            memScoped {
                val outMoved = alloc<ULongVar>()
                CCCrypt(
                    op             = kCCDecrypt,
                    alg            = kCCAlgorithmAES,
                    options        = kCCOptionPKCS7Padding,
                    key            = key.toCValues().ptr,
                    keyLength      = kCCKeySizeAES256.toULong(),
                    iv             = iv.toCValues().ptr,
                    dataIn         = ciphertext.toCValues().ptr,
                    dataInLength   = ciphertext.size.toULong(),
                    dataOut        = pinnedOutput.addressOf(0),
                    dataOutAvailable = output.size.toULong(),
                    dataOutMoved   = outMoved.ptr
                )
                numBytesDecrypted = outMoved.value
            }
        }

        return output.take(numBytesDecrypted.toInt()).toByteArray().decodeToString()
    }

    // MIGRATION_FIX: Original `readBytes()` pointer arithmetic was incorrect.
    //   BUG: `(readBytes()!! + i).toByte()` performed pointer arithmetic and cast the
    //        resulting CPointer address value to Byte — not the byte AT that address.
    //   FIX: Use `usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }`.
    //        `NSData.bytes` returns a `COpaquePointer?` pointing to the raw data.
    //        `usePinned + addressOf(0)` pins the Kotlin ByteArray so GC won't move it
    //        while `platform.posix.memcpy` copies the raw bytes into it.
    private fun NSData.toByteArray(): ByteArray {
        val length = this.length.toInt()
        if (length == 0) return ByteArray(0)
        val result = ByteArray(length)
        result.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), bytes, length.toULong())
        }
        return result
    }
}

private const val ENCRYPTION_KEY_NAME = "myAppEncryptionKey"
