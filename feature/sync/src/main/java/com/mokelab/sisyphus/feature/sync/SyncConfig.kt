package com.mokelab.sisyphus.feature.sync

/**
 * OneDrive 同步配置
 *
 * ⚠️ 警告：CLIENT_ID 当前为占位符，OneDrive 同步功能不可用！
 * 要启用同步功能，需要：
 * 1. 在 Azure Portal (https://portal.azure.com) 注册应用
 * 2. 将 CLIENT_ID 替换为真实的应用 ID
 * 3. 将 REDIRECT_URI 替换为真实的回调地址
 *
 * 在功能就绪前，UI 中应隐藏同步相关入口
 */
object SyncConfig {
    // Azure AD 应用注册信息
    // TODO: 替换为真实的 Azure AD 应用 ID
    const val CLIENT_ID = "YOUR_CLIENT_ID"
    const val REDIRECT_URI = "msauth://com.mokelab.sisyphus/YOUR_SIGNATURE"

    // OAuth 2.0 端点
    const val AUTHORITY = "https://login.microsoftonline.com/common"
    const val AUTH_ENDPOINT = "$AUTHORITY/oauth2/v2.0/authorize"
    const val TOKEN_ENDPOINT = "$AUTHORITY/oauth2/v2.0/token"

    // Microsoft Graph API
    const val GRAPH_BASE_URL = "https://graph.microsoft.com/v1.0"
    const val ONEDRIVE_BASE_URL = "$GRAPH_BASE_URL/me/drive"

    // 同步范围
    const val SCOPES = "Files.ReadWrite offline_access"

    // 同步文件夹
    const val SYNC_FOLDER = "Sisyphus"
    const val SYNC_FILE = "sisyphus_data.json"
}
