package com.xy.root.update

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 更新信息数据类
 */
@Parcelize
data class UpdateInfo(
    val tagName: String,            // 版本标签，如 "v1.1.0"
    val name: String,               // 版本名称
    val body: String,               // 版本说明
    val publishedAt: String,        // 发布时间
    val assets: List<Asset>,        // 附件列表
    val htmlUrl: String,            // GitHub Release页面URL
    val prerelease: Boolean = false // 是否为预发布版本
) : Parcelable {
    
    /**
     * 获取APK下载链接
     */
    fun getApkDownloadUrl(): String? {
        return assets.firstOrNull { it.name.endsWith(".apk") }?.downloadUrl
    }
    
    /**
     * 获取APK文件名
     */
    fun getApkFileName(): String? {
        return assets.firstOrNull { it.name.endsWith(".apk") }?.name
    }
    
    /**
     * 获取APK文件大小
     */
    fun getApkFileSize(): Long {
        return assets.firstOrNull { it.name.endsWith(".apk") }?.size ?: 0L
    }
    
    /**
     * 格式化文件大小显示
     */
    fun getFormattedFileSize(): String {
        val size = getApkFileSize()
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            else -> "${"%.1f".format(size / 1024.0 / 1024.0)}MB"
        }
    }
}

@Parcelize
data class Asset(
    val name: String,               // 文件名
    val downloadUrl: String,        // 下载链接
    val size: Long,                 // 文件大小
    val contentType: String         // 文件类型
) : Parcelable
