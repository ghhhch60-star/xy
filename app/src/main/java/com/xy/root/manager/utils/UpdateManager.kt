package com.xy.root.manager.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.xy.root.manager.BuildConfig
import com.xy.root.manager.model.UpdateInfo
import com.xy.root.manager.model.UpdateResponse
import com.xy.root.update.UpdateSourceManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class UpdateManager(private val context: Context) {

    companion object {
        private const val UPDATE_DOWNLOAD_DIR = "updates"
        private const val APK_FILE_NAME = "iyth-app-update.apk"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val sourceManager = UpdateSourceManager(context)
    private var downloadId: Long = -1
    private var onDownloadComplete: ((Boolean, String?) -> Unit)? = null

    /**
     * 检查是否有新版本可用 - 使用智能源管理器
     */
    suspend fun checkForUpdate(): UpdateResponse = withContext(Dispatchers.IO) {
        try {
            val updateSources = sourceManager.getBestUpdateSources()
            
            for (updateUrl in updateSources) {
                try {
                    val request = Request.Builder()
                        .url(updateUrl)
                        .addHeader("Accept", "application/vnd.github+json")
                        .addHeader("User-Agent", "IYTH-App-Updater/1.0")
                        .build()

                    val response = client.newCall(request).execute()
                    
                    if (!response.isSuccessful) {
                        continue // 尝试下一个源
                    }

                    val jsonResponse = response.body?.string()
                    if (jsonResponse.isNullOrEmpty()) {
                        continue // 尝试下一个源
                    }

                    // 解析GitHub API响应
                    val releaseInfo = gson.fromJson(jsonResponse, Map::class.java)
                    val tagName = releaseInfo["tag_name"] as? String ?: continue
                    val name = releaseInfo["name"] as? String ?: tagName
                    val body = releaseInfo["body"] as? String ?: ""
                    val assets = releaseInfo["assets"] as? List<Map<String, Any>> ?: emptyList()

                    // 查找APK文件
                    val apkAsset = assets.find { asset ->
                        val assetName = asset["name"] as? String ?: ""
                        assetName.endsWith(".apk", ignoreCase = true)
                    }

                    if (apkAsset == null) {
                        continue // 尝试下一个源
                    }

                    val downloadUrl = apkAsset["browser_download_url"] as? String ?: continue
                    val fileSize = (apkAsset["size"] as? Number)?.toLong() ?: 0L

                    // 解析版本号（假设格式为 v1.6 或 1.6）
                    val versionName = tagName.removePrefix("v")
                    val versionCode = parseVersionCode(versionName)

                    // 检查是否为新版本
                    val currentVersionCode = BuildConfig.VERSION_CODE
                    val hasUpdate = versionCode > currentVersionCode

                    // 记录成功的源
                    sourceManager.recordSuccessfulSource(updateUrl)

                    if (hasUpdate) {
                        val updateInfo = UpdateInfo(
                            versionName = versionName,
                            versionCode = versionCode,
                            downloadUrl = downloadUrl,
                            fileSize = fileSize,
                            releaseNotes = body,
                            isForced = false // 可以根据需要设置强制更新逻辑
                        )
                        return@withContext UpdateResponse(true, updateInfo)
                    } else {
                        return@withContext UpdateResponse(false, null)
                    }

                } catch (e: Exception) {
                    // 继续尝试下一个源
                    continue
                }
            }
            
            // 所有源都失败了
            UpdateResponse(false, null)

        } catch (e: Exception) {
            e.printStackTrace()
            UpdateResponse(false, null)
        }
    }

    /**
     * 下载APK文件 - 使用镜像源优化
     */
    fun downloadUpdate(
        updateInfo: UpdateInfo,
        onProgress: ((Int) -> Unit)? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        this.onDownloadComplete = onComplete

        // 在协程中获取最佳下载链接
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val optimizedDownloadUrl = sourceManager.getBestDownloadMirror(updateInfo.downloadUrl)
                
                withContext(Dispatchers.Main) {
                    startDownloadWithUrl(updateInfo, optimizedDownloadUrl, onComplete)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "获取下载链接失败: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 使用指定URL开始下载
     */
    private fun startDownloadWithUrl(
        updateInfo: UpdateInfo,
        downloadUrl: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            // 创建下载目录
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), UPDATE_DOWNLOAD_DIR)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val destinationFile = File(downloadDir, APK_FILE_NAME)
            
            // 如果文件已存在，删除它
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl))
                .setTitle("IYTH应用更新")
                .setDescription("正在下载版本 ${updateInfo.versionName}")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destinationFile))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .addRequestHeader("User-Agent", "IYTH-App-Updater/1.0")

            downloadId = downloadManager.enqueue(request)
            
            // 记录成功的镜像
            sourceManager.recordSuccessfulMirror(downloadUrl)

            // 注册下载完成监听器
            registerDownloadReceiver()

        } catch (e: Exception) {
            e.printStackTrace()
            onComplete(false, "下载启动失败: ${e.message}")
        }
    }

    /**
     * 安装APK
     */
    fun installApk() {
        try {
            val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), UPDATE_DOWNLOAD_DIR)
            val apkFile = File(downloadDir, APK_FILE_NAME)
            
            if (!apkFile.exists()) {
                onDownloadComplete?.invoke(false, "APK文件不存在")
                return
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            onDownloadComplete?.invoke(false, "安装失败: ${e.message}")
        }
    }

    /**
     * 获取当前版本信息
     */
    fun getCurrentVersionInfo(): String {
        return "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }

    /**
     * 格式化文件大小
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "${bytes}B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1fKB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1fMB", mb)
        val gb = mb / 1024.0
        return String.format("%.1fGB", gb)
    }

    private fun parseVersionCode(versionName: String): Int {
        return try {
            // 简单的版本号解析逻辑，可以根据实际情况调整
            val parts = versionName.split(".")
            var code = 0
            for (i in parts.indices) {
                if (i < 3) { // 最多处理三位版本号
                    code += parts[i].toInt() * 100.0.pow((2 - i).toDouble()).toInt()
                }
            }
            code
        } catch (e: Exception) {
            0
        }
    }

    private fun registerDownloadReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    
                    if (cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                onDownloadComplete?.invoke(true, null)
                                context?.unregisterReceiver(this)
                            }
                            DownloadManager.STATUS_FAILED -> {
                                val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                                onDownloadComplete?.invoke(false, "下载失败，错误代码: $reason")
                                context?.unregisterReceiver(this)
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(receiver, filter)
    }

    /**
     * 计算文件MD5
     */
    private fun calculateMD5(file: File): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val inputStream = file.inputStream()
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                md.update(buffer, 0, bytesRead)
            }
            inputStream.close()
            
            val digest = md.digest()
            val hexString = StringBuilder()
            for (byte in digest) {
                val hex = Integer.toHexString(0xFF and byte.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            hexString.toString()
        } catch (e: Exception) {
            ""
        }
    }
}
