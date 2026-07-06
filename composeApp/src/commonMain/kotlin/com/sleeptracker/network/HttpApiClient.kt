package com.sleeptracker.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// MIGRATION: TypeScript custom `HttpClient.ts` interface (get/post/put/delete)
// → Ktor `HttpClient` wrapper.
// The TypeScript implementation used `fetch()` with manual Bearer token headers.
// Ktor provides a built-in `Auth` plugin for Bearer tokens and `ContentNegotiation` for JSON.

// MIGRATION: Backend URL from .env `BACKEND_URL` → build config or runtime configuration.
// MIGRATION_FLAG: In React Native, BACKEND_URL came from Expo's env system.
// In Android/iOS production, this should come from BuildConfig or a config file.
// Default is provided here for development.
const val DEFAULT_BACKEND_URL = "http://YOUR_LAN_IP:7000/api"

class HttpApiClient(
    @PublishedApi internal val baseUrl: String = DEFAULT_BACKEND_URL,
    private val getToken: () -> String? = { null }
) {
    // MIGRATION: Ktor HttpClient configured similarly to the TypeScript HttpClient:
    // - JSON content negotiation with lenient parser (TypeScript was permissive with `any`)
    // - Bearer token auth matching TypeScript `Authorization: Bearer ${token}` header
    // - Logging equivalent to TypeScript console.error on failure
    @PublishedApi internal val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true    // TypeScript used `any` — be lenient with extra fields
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        // MIGRATION: Manual `Authorization: Bearer ${token}` in TypeScript httpClient
        // → Ktor Auth plugin
        install(Auth) {
            bearer {
                loadTokens {
                    val token = getToken()
                    if (token != null) BearerTokens(token, "") else null
                }
            }
        }
    }

    // MIGRATION: TypeScript `get<T>(path: string, token?: string): Promise<T>`
    // → suspend fun with reified type
    suspend inline fun <reified T> get(path: String, token: String? = null): T =
        client.get("$baseUrl$path") {
            if (token != null) header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
        }.body()

    // MIGRATION: TypeScript `post<T>(path: string, body: any, token?: string): Promise<T>`
    suspend inline fun <reified T> post(
        path: String,
        body: Any,
        token: String? = null
    ): T = client.post("$baseUrl$path") {
        if (token != null) header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(body)
    }.body()

    suspend inline fun <reified T> put(
        path: String,
        body: Any,
        token: String? = null
    ): T = client.put("$baseUrl$path") {
        if (token != null) header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(body)
    }.body()

    suspend inline fun <reified T> delete(
        path: String,
        token: String? = null
    ): T = client.delete("$baseUrl$path") {
        if (token != null) header("Authorization", "Bearer $token")
    }.body()
}
