package com.xy.root.manager.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Partition(
    val name: String,
    val path: String,
    val size: Long,
    val isBackedUp: Boolean = false,
    val isImageFile: Boolean = false, // 标识是否为映像文件
    val isSelected: Boolean = false // 标识是否被选中
) : Parcelable {
    fun getSizeInMB(): String {
        return String.format("%.2f MB", size / (1024.0 * 1024.0))
    }
    
    fun getSizeInGB(): String {
        return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
    }
    
    fun getFormattedSize(): String {
        return when {
            size >= 1024L * 1024L * 1024L -> getSizeInGB()
            else -> getSizeInMB()
        }
    }
}

