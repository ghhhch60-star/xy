package com.xy.root.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 应用更新管理器
 */
class UpdateManager(private val context: Context) {
    
    companion object {
        private const val UPDATE_NOTIFICATION_ID = 1001
        private const val CONNECTION_TIMEOUT = 15L // 缩短超时时间
        private const val READ_TIMEOUT = 30L
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val sourceManager = UpdateSourceManager(context)
    
    // LiveData for update status
    private val _updateStatus = MutableLiveData<UpdateStatus>()
    val updateStatus: LiveData<UpdateStatus> = _updateStatus
    
    // LiveData for download progress
    private val _downloadProgress = MutableLiveData<DownloadProgress>()
    val downloadProgress: LiveData<DownloadProgress> = _downloadProgress
    
    private var downloadId: Long = -1
    private var downloadReceiver: BroadcastReceiver? = null
    
    /**
     * 检查更新 - 使用智能源管理器进行 fallback
     */
    suspend fun checkForUpdate(): UpdateResult = withContext(Dispatchers.IO) {
        _updateStatus.postValue(UpdateStatus.Checking)
        
        var lastError: String? = null
        val updateSources = sourceManager.getBestUpdateSources()
        
        // 依次尝试每个更新源
        for ((index, updateUrl) in updateSources.withIndex()) {
            try {
                val request = Request.Builder()
                    .url(updateUrl)
                    .addHeader("Accept", "application/vnd.github+json")
                    .addHeader("User-Agent", "IYTH-App-Updater/1.0")
                    .build()
                    
                val response = okHttpClient.newCall(request).execute()
                
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrEmpty()) {
                        val updateInfo = parseUpdateInfo(responseBody, updateUrl)
                        if (updateInfo != null) {
                            // 记录成功的源
                            sourceManager.recordSuccessfulSource(updateUrl)
                            
                            val currentVersion = getCurrentVersion()
                            val hasUpdate = compareVersions(updateInfo.tagName, currentVersion)
                            
                            return@withContext if (hasUpdate) {
                                _updateStatus.postValue(UpdateStatus.UpdateAvailable(updateInfo))
                                UpdateResult.UpdateAvailable(updateInfo)
                            } else {
                                _updateStatus.postValue(UpdateStatus.NoUpdate)
                                UpdateResult.NoUpdate
                            }
                        }
                    }
                }
                
                lastError = "源 ${index + 1} 失败: HTTP ${response.code}"
                
            } catch (e: Exception) {
                lastError = "源 ${index + 1} 异常: ${e.message}"
                // 继续尝试下一个源
                continue
            }
        }
        
        // 所有源都失败了
        val networkEnvDesc = sourceManager.getNetworkEnvironmentDescription()
        val errorMsg = "所有更新源均不可用 ($networkEnvDesc)。最后错误: $lastError"
        _updateStatus.postValue(UpdateStatus.Error(errorMsg))
        UpdateResult.Error(errorMsg)
    }
    
    /**
     * 下载更新 - 使用镜像源优化下载
     */
    fun downloadUpdate(updateInfo: UpdateInfo) {
        val originalDownloadUrl = updateInfo.getApkDownloadUrl()
        if (originalDownloadUrl == null) {
            _updateStatus.postValue(UpdateStatus.Error("未找到APK下载链接"))
            return
        }
        
        // 在协程中选择最佳下载镜像
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val downloadUrl = sourceManager.getBestDownloadMirror(originalDownloadUrl)
                
                withContext(Dispatchers.Main) {
                    startDownload(updateInfo, downloadUrl)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _updateStatus.postValue(UpdateStatus.Error("获取下载链接失败: ${e.message}"))
                }
            }
        }
    }
    
    /**
     * 开始下载
     */
    private fun startDownload(updateInfo: UpdateInfo, downloadUrl: String) {
        try {
            val fileName = updateInfo.getApkFileName() ?: "iyth-app-update.apk"
            
            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("IYTH App 更新")
                .setDescription("正在下载 ${updateInfo.tagName}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .addRequestHeader("User-Agent", "IYTH-App-Updater/1.0")
            
            downloadId = downloadManager.enqueue(request)
            _updateStatus.postValue(UpdateStatus.Downloading)
            
            // 记录成功的镜像
            sourceManager.recordSuccessfulMirror(downloadUrl)
            
            // 注册下载完成监听器
            registerDownloadReceiver()
            
            // 开始监控下载进度
            startDownloadProgressMonitoring()
            
        } catch (e: Exception) {
            _updateStatus.postValue(UpdateStatus.Error("开始下载失败: ${e.message}"))
        }
    }
    
    
    /**
     * 安装APK
     */
    fun installApk(apkFile: File) {
        try {
            if (!apkFile.exists()) {
                _updateStatus.postValue(UpdateStatus.Error("APK文件不存在"))
                return
            }
            
            val intent = Intent(Intent.ACTION_VIEW)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android 7.0及以上使用FileProvider
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
            }
            
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            
            _updateStatus.postValue(UpdateStatus.ReadyToInstall)
            
        } catch (e: Exception) {
            _updateStatus.postValue(UpdateStatus.Error("安装失败: ${e.message}"))
        }
    }
    
    /**
     * 获取当前版本
     */
    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }
    
    /**
     * 解析更新信息 - 支持多种数据源格式
     */
    private fun parseUpdateInfo(jsonString: String, sourceUrl: String): UpdateInfo? {
        return try {
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            
            val assets = mutableListOf<Asset>()
            val assetsArray = jsonObject.getAsJsonArray("assets")
            
            for (assetElement in assetsArray) {
                val assetObj = assetElement.asJsonObject
                assets.add(
                    Asset(
                        name = assetObj.get("name").asString,
                        downloadUrl = assetObj.get("browser_download_url").asString,
                        size = assetObj.get("size").asLong,
                        contentType = assetObj.get("content_type").asString
                    )
                )
            }
            
            UpdateInfo(
                tagName = jsonObject.get("tag_name").asString,
                name = jsonObject.get("name").asString,
                body = jsonObject.get("body")?.asString ?: "",
                publishedAt = jsonObject.get("published_at").asString,
                assets = assets,
                htmlUrl = jsonObject.get("html_url").asString,
                prerelease = jsonObject.get("prerelease")?.asBoolean ?: false
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 比较版本号
     */
    private fun compareVersions(newVersion: String, currentVersion: String): Boolean {
        try {
            // 移除版本号前的'v'字符
            val newVer = newVersion.removePrefix("v")
            val currentVer = currentVersion.removePrefix("v")
            
            val newParts = newVer.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = currentVer.split(".").map { it.toIntOrNull() ?: 0 }
            
            val maxLength = maxOf(newParts.size, currentParts.size)
            
            for (i in 0 until maxLength) {
                val newPart = newParts.getOrNull(i) ?: 0
                val currentPart = currentParts.getOrNull(i) ?: 0
                
                when {
                    newPart > currentPart -> return true
                    newPart < currentPart -> return false
                }
            }
            
            return false // 版本相同
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * 注册下载完成监听器
     */
    private fun registerDownloadReceiver() {
        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                        
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                _updateStatus.postValue(UpdateStatus.DownloadCompleted)
                                localUri?.let { uri ->
                                    val file = File(Uri.parse(uri).path!!)
                                    installApk(file)
                                }
                            }
                            DownloadManager.STATUS_FAILED -> {
                                val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                                _updateStatus.postValue(UpdateStatus.Error("下载失败，错误码: $reason"))
                            }
                        }
                    }
                    cursor.close()
                    unregisterDownloadReceiver()
                }
            }
        }
        
        context.registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }
    
    /**
     * 注销下载监听器
     */
    private fun unregisterDownloadReceiver() {
        downloadReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                // 忽略已经注销的异常
            }
            downloadReceiver = null
        }
    }
    
    /**
     * 开始监控下载进度
     */
    private fun startDownloadProgressMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            while (downloadId != -1L) {
                try {
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val bytesDownloaded = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        )
                        val bytesTotal = cursor.getLong(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        )
                        val status = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                        )
                        
                        if (bytesTotal > 0) {
                            val progress = (bytesDownloaded * 100 / bytesTotal).toInt()
                            _downloadProgress.postValue(
                                DownloadProgress(
                                    progress = progress,
                                    bytesDownloaded = bytesDownloaded,
                                    bytesTotal = bytesTotal
                                )
                            )
                        }
                        
                        // 如果下载完成或失败，停止监控
                        if (status == DownloadManager.STATUS_SUCCESSFUL || 
                            status == DownloadManager.STATUS_FAILED) {
                            break
                        }
                    }
                    cursor.close()
                    
                    delay(500) // 每500ms更新一次进度
                } catch (e: Exception) {
                    break
                }
            }
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        unregisterDownloadReceiver()
        downloadId = -1
    }
}

/**
 * 更新状态
 */
sealed class UpdateStatus {
    object Checking : UpdateStatus()
    object NoUpdate : UpdateStatus()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateStatus()
    object Downloading : UpdateStatus()
    object DownloadCompleted : UpdateStatus()
    object ReadyToInstall : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

/**
 * 更新结果
 */
sealed class UpdateResult {
    object NoUpdate : UpdateResult()
    data class UpdateAvailable(val updateInfo: UpdateInfo) : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}

/**
 * 下载进度
 */
data class DownloadProgress(
    val progress: Int,
    val bytesDownloaded: Long,
    val bytesTotal: Long
) {
    fun getFormattedProgress(): String {
        return "${formatBytes(bytesDownloaded)} / ${formatBytes(bytesTotal)} ($progress%)"
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)}KB"
            else -> "${"%.1f".format(bytes / 1024.0 / 1024.0)}MB"
        }
    }
}
