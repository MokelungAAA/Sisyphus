package com.mokelab.sisyphus.feature.sync

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * OAuth Token 数据
 */
@Serializable
data class OAuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long, // Unix 毫秒
    val scope: String
)

/**
 * Token 存储管理
 * 注意：生产环境应使用 EncryptedSharedPreferences
 */
class TokenStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "sync_tokens", Context.MODE_PRIVATE
    )

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 保存 tokens
     */
    fun saveTokens(tokens: OAuthTokens) {
        prefs.edit()
            .putString(KEY_TOKENS, json.encodeToString(tokens))
            .apply()
    }

    /**
     * 获取 tokens
     */
    fun getTokens(): OAuthTokens? {
        val raw = prefs.getString(KEY_TOKENS, null) ?: return null
        return try {
            json.decodeFromString<OAuthTokens>(raw)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 清除 tokens
     */
    fun clearTokens() {
        prefs.edit().remove(KEY_TOKENS).apply()
    }

    /**
     * 是否已认证
     */
    fun isAuthenticated(): Boolean {
        val tokens = getTokens() ?: return false
        return tokens.expiresAt > System.currentTimeMillis()
    }

    /**
     * 保存最后同步时间
     */
    fun saveLastSyncTime(timeMillis: Long) {
        prefs.edit()
            .putLong(KEY_LAST_SYNC, timeMillis)
            .apply()
    }

    /**
     * 获取最后同步时间
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC, 0L)
    }

    companion object {
        private const val KEY_TOKENS = "oauth_tokens"
        private const val KEY_LAST_SYNC = "last_sync_time"
    }
}
