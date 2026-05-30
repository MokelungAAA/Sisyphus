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

        var conn: HttpURLConnection? = null
        try {
            // 确保同步文件夹存在
            ensureFolderExists(token)

            // 上传文件
            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}/${SyncConfig.SYNC_FILE}:/content")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
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
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * 从 OneDrive 下载数据
     */
    suspend fun downloadData(): Result<String> = withContext(Dispatchers.IO) {
        val token = authManager.getValidAccessToken().getOrElse {
            return@withContext Result.failure(it)
        }

        var conn: HttpURLConnection? = null
        try {
            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}/${SyncConfig.SYNC_FILE}:/content")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000

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
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * 上传单个实体
     */
    suspend fun uploadEntity(entityId: String, data: String): Result<Unit> = withContext(Dispatchers.IO) {
        val token = authManager.getValidAccessToken().getOrElse {
            return@withContext Result.failure(it)
        }

        var conn: HttpURLConnection? = null
        try {
            ensureFolderExists(token)
            ensureEntitiesFolderExists(token)

            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}/entities/${entityId}.json:/content")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.doOutput = true

            conn.outputStream.bufferedWriter().use { it.write(data) }

            if (conn.responseCode in 200..299) {
                Result.success(Unit)
            } else {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "Upload failed"
                Result.failure(Exception("Upload entity failed: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * 下载单个实体
     */
    suspend fun downloadEntity(entityId: String): Result<String> = withContext(Dispatchers.IO) {
        val token = authManager.getValidAccessToken().getOrElse {
            return@withContext Result.failure(it)
        }

        var conn: HttpURLConnection? = null
        try {
            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}/entities/${entityId}.json:/content")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000

            if (conn.responseCode == 200) {
                val data = conn.inputStream.bufferedReader().readText()
                Result.success(data)
            } else if (conn.responseCode == 404) {
                Result.failure(Exception("Entity not found"))
            } else {
                val error = conn.errorStream?.bufferedReader()?.readText() ?: "Download failed"
                Result.failure(Exception("Download entity failed: $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * 确保同步文件夹存在
     * 添加超时和连接关闭，防止资源泄漏
     */
    private suspend fun ensureFolderExists(token: String) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000

            if (conn.responseCode == 404) {
                // 创建文件夹
                var createConn: HttpURLConnection? = null
                try {
                    val createUrl = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root/children")
                    createConn = createUrl.openConnection() as HttpURLConnection
                    createConn.requestMethod = "POST"
                    createConn.setRequestProperty("Authorization", "Bearer $token")
                    createConn.setRequestProperty("Content-Type", "application/json")
                    createConn.connectTimeout = 15_000
                    createConn.readTimeout = 30_000
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
                } finally {
                    createConn?.disconnect()
                }
            }
        } finally {
            conn?.disconnect()
        }
    }

    /**
     * 确保实体文件夹存在
     * 添加超时和连接关闭，防止资源泄漏
     */
    private suspend fun ensureEntitiesFolderExists(token: String) {
        var conn: HttpURLConnection? = null
        try {
            val url = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}/entities")
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000

            if (conn.responseCode == 404) {
                var createConn: HttpURLConnection? = null
                try {
                    val createUrl = URL("${SyncConfig.ONEDRIVE_BASE_URL}/root:/${SyncConfig.SYNC_FOLDER}:/children")
                    createConn = createUrl.openConnection() as HttpURLConnection
                    createConn.requestMethod = "POST"
                    createConn.setRequestProperty("Authorization", "Bearer $token")
                    createConn.setRequestProperty("Content-Type", "application/json")
                    createConn.connectTimeout = 15_000
                    createConn.readTimeout = 30_000
                    createConn.doOutput = true

                    val body = """
                        {
                            "name": "entities",
                            "folder": {},
                            "@microsoft.graph.conflictBehavior": "rename"
                        }
                    """.trimIndent()

                    createConn.outputStream.bufferedWriter().use { it.write(body) }
                    createConn.responseCode
                } finally {
                    createConn?.disconnect()
                }
            }
        } finally {
            conn?.disconnect()
        }
    }
}
