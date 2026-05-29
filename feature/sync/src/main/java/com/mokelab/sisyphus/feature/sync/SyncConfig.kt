package com.mokelab.sisyphus.feature.sync

/**
 * OneDrive 同步配置
 */
object SyncConfig {
    // Azure AD 应用注册信息
    // 注意：正式发布前需要在 Azure Portal 注册应用并替换以下值
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
