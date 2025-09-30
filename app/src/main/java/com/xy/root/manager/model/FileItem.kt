package com.xy.root.manager.model

data class FileItem(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val modifiedTime: Long,
    val isSelected: Boolean = false
) {
    fun getFormattedSize(): String {
        return when {
            size == 0L -> ""
            size >= 1024L * 1024L * 1024L -> String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0))
            size >= 1024L * 1024L -> String.format("%.2f MB", size / (1024.0 * 1024.0))
            size >= 1024L -> String.format("%.2f KB", size / 1024.0)
            else -> "$size B"
        }
    }
    
    fun getFormattedTime(): String {
        return if (modifiedTime > 0) {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(modifiedTime * 1000))
        } else {
            ""
        }
    }
}
