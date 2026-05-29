package com.mokelab.sisyphus.feature.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

/**
 * OneDrive 同步服务
 * 处理文件上传/下载
 */
class SyncService(
    private val authManager: AuthManager,
    private val tokenStorage: TokenStorage
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    /**
     * 上传数据到 OneDrive
     */
    suspend fun uploadData(data: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = authManager.getValidAccessToken().getOrElse {
            return@withContext Result.failure(it)
        }

        try {
            // 确保同步文件夹存在
            ensureFolderExists(token)

            // 上传文件
            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}/${SyncConfig.SYNC_FILE}:/content")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            conn.outputStream.bufferedWriter().use { it.write(data) }

            if (conn.responseCode in 200..299) {
                tokenStorage.saveLastSyncTime(System.currentTimeMillis())
                Result.success(Unit)
            } else {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "Upload failed"
                Result.failure(Exception("Upload failed: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 OneDrive 下载数据
     */
    suspend fun downloadData(): Result<String> = withContext(Dispatchers.IO) {
        val token = authManager.getValidAccessToken().getOrElse {
            return@withContext Result.failure(it)
        }

        try {
            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}/${SyncConfig.SYNC_FILE}:/content")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")

            if (conn.responseCode == 200) {
                val data = conn.inputStream.bufferedReader().readText()
                tokenStorage.saveLastSyncTime(System.currentTimeMillis())
                Result.success(data)
            } else if (conn.responseCode == 404) {
                // 文件不存在
                Result.failure(Exception("No synced data found"))
            } else {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "Download failed"
                Result.failure(Exception("Download failed: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 确保同步文件夹存在
     */
    private suspend fun ensureFolderExists(token: String) {
        val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $token")

        if (conn.responseCode == 404) {
            // 创建文件夹
            val createUrl = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root/children")
            val createConn = createUrl.openConnection() as HttpURLConnection
            createConn.requestMethod = "POST"
            createConn.setRequestProperty("Authorization", "Bearer $token")
            createConn.setRequestProperty("Content-Type", "application/json")
            createConn.doOutput = true

            val body = """
                {
                    "name": "${SyncConfig.SYNC_FOLDER}",
                    "folder": {},
                    "@microsoft.graph.conflictBehavior": "rename"
                }
            """.trimIndent()

            createConn.outputStream.bufferedWriter().use { it.write(body) }
            createConn.responseCode // 触发请求
        }
    }
}
