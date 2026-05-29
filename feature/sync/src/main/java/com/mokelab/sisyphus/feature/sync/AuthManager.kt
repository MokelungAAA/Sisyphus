package com.mokelab.sisyphus.feature.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * OAuth 2.0 认证管理器
 * 处理 OneDrive 认证流程
 */
class AuthManager(
    private val context: Context,
    private val tokenStorage: TokenStorage
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 获取登录 Intent（用于启动浏览器）
     */
    fun getLoginIntent(): Intent {
        val authUrl = buildAuthUrl()
        return Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
    }

    /**
     * 构建授权 URL
     */
    private fun buildAuthUrl(): String {
        val encodedRedirect = URLEncoder.encode(SyncConfig.REDIRECT_URI, "UTF-8")
        val encodedScopes = URLEncoder.encode(SyncConfig.SCOPES, "UTF-8")
        return "${SyncConfig.AUTH_ENDPOINT}" +
            "?client_id=${SyncConfig.CLIENT_ID}" +
            "&response_type=code" +
            "&redirect_uri=$encodedRedirect" +
            "&scope=$encodedScopes" +
            "&response_mode=query"
    }

    /**
     * 处理回调，用授权码换取 tokens
     */
    suspend fun handleAuthCallback(code: String): Result<OAuthTokens> = withContext(Dispatchers.IO) {
        try {
            val url = URL(SyncConfig.TOKEN_ENDPOINT)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true

            val body = buildString {
                append("client_id=${SyncConfig.CLIENT_ID}")
                append("&code=$code")
                append("&redirect_uri=${URLEncoder.encode(SyncConfig.REDIRECT_URI, "UTF-8")}")
                append("&grant_type=authorization_code")
            }

            conn.outputStream.bufferedWriter().use { it.write(body) }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val tokenResponse = json.decodeFromString<TokenResponse>(response)
                val tokens = OAuthTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken ?: "",
                    expiresAt = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000),
                    scope = tokenResponse.scope ?: SyncConfig.SCOPES
                )
                tokenStorage.saveTokens(tokens)
                Result.success(tokens)
            } else {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                Result.failure(Exception("Token exchange failed: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 刷新 access token
     */
    suspend fun refreshTokens(): Result<OAuthTokens> = withContext(Dispatchers.IO) {
        val currentTokens = tokenStorage.getTokens()
            ?: return@withContext Result.failure(Exception("No refresh token available"))

        try {
            val url = URL(SyncConfig.TOKEN_ENDPOINT)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true

            val body = buildString {
                append("client_id=${SyncConfig.CLIENT_ID}")
                append("&refresh_token=${currentTokens.refreshToken}")
                append("&grant_type=refresh_token")
            }

            conn.outputStream.bufferedWriter().use { it.write(body) }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().readText()
                val tokenResponse = json.decodeFromString<TokenResponse>(response)
                val tokens = OAuthTokens(
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken ?: currentTokens.refreshToken,
                    expiresAt = System.currentTimeMillis() + (tokenResponse.expiresIn * 1000),
                    scope = tokenResponse.scope ?: currentTokens.scope
                )
                tokenStorage.saveTokens(tokens)
                Result.success(tokens)
            } else {
                Result.failure(Exception("Token refresh failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 获取有效的 access token（自动刷新）
     */
    suspend fun getValidAccessToken(): Result<String> {
        val tokens = tokenStorage.getTokens()
            ?: return Result.failure(Exception("Not authenticated"))

        // Token 未过期
        if (tokens.expiresAt > System.currentTimeMillis() + 60_000) {
            return Result.success(tokens.accessToken)
        }

        // 需要刷新
        return refreshTokens().map { it.accessToken }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        tokenStorage.clearTokens()
    }

    /**
     * 是否已认证
     */
    fun isAuthenticated(): Boolean = tokenStorage.isAuthenticated()

    /**
     * 获取授权 URL
     */
    fun getAuthorizationUrl(): String? {
        return try {
            buildAuthUrl()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 用授权码换取 tokens (别名)
     */
    suspend fun exchangeCode(code: String): Result<OAuthTokens> {
        return handleAuthCallback(code)
    }
}

/**
 * Token 端点响应
 */
@Serializable
private data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Long,
    val scope: String? = null,
    val tokenType: String? = null
)
