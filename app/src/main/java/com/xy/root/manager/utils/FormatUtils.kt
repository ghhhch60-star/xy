package com.xy.root.manager.utils

object FormatUtils {
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
        }
    }
}
