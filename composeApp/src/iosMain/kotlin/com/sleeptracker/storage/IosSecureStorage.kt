package com.sleeptracker.storage

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.Security.errSecSuccess
import platform.Security.errSecItemNotFound
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.darwin.OSStatus

// MIGRATION: TypeScript `expo-secure-store` on iOS → iOS Keychain via Security framework.
// expo-secure-store already used the iOS Keychain under the hood.
// We replicate the same behavior directly with Security framework APIs.
// Key-value storage uses kSecClassGenericPassword with kSecAttrAccount as the key.

private const val SERVICE_NAME = "com.mcscert.sleeptracker"  // matches iOS bundle identifier

@OptIn(ExperimentalForeignApi::class)
actual class SecureStorage {

    actual suspend fun setItem(key: String, value: String) {
        val query = buildQuery(key)
        val data = value.encodeToByteArray()

        // Try to update first, then add if not found
        val status: OSStatus = SecItemCopyMatching(query as platform.CoreFoundation.CFDictionaryRef, null)
        if (status == errSecItemNotFound) {
            val addQuery = (query as Map<Any?, *>).toMutableMap().apply {
                put(kSecValueData, NSString.create(string = value))
            }
            SecItemAdd(addQuery as platform.CoreFoundation.CFDictionaryRef, null)
        } else {
            val updateAttributes = mapOf<Any?, Any?>(
                kSecValueData to NSString.create(string = value)
            )
            SecItemUpdate(query as platform.CoreFoundation.CFDictionaryRef,
                updateAttributes as platform.CoreFoundation.CFDictionaryRef)
        }
    }

    actual suspend fun getItem(key: String): String? {
        val query = buildQuery(key).toMutableMap().apply {
            put(kSecReturnData, true)
            put(kSecMatchLimit, kSecMatchLimitOne)
        }
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as platform.CoreFoundation.CFDictionaryRef, result.ptr)
            if (status == errSecSuccess) {
                val data = CFBridgingRelease(result.value) as? NSString
                return data?.toString()
            }
        }
        return null
    }

    actual suspend fun deleteItem(key: String) {
        val query = buildQuery(key)
        SecItemDelete(query as platform.CoreFoundation.CFDictionaryRef)
    }

    private fun buildQuery(key: String): Map<Any?, Any?> = mapOf(
        kSecClass        to kSecClassGenericPassword,
        kSecAttrService  to SERVICE_NAME,
        kSecAttrAccount  to key
    )
}
