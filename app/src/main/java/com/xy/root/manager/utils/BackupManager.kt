package com.xy.root.manager.utils

import android.content.Context
import android.os.Environment
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.io.SuFile
import com.topjohnwu.superuser.io.SuFileInputStream
import com.xy.root.manager.model.Partition
import com.xy.root.manager.utils.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    suspend fun backupPartition(
        partition: Partition,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 直接备份到Download文件夹，不创建子目录
            val downloadDir = File("/storage/emulated/0/Download")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            // 生成备份文件名（包含时间戳）
            val timestamp = dateFormat.format(Date())
            val tempBackupFile = File(downloadDir, "${partition.name}_${timestamp}.img")
            val finalBackupFile = File(downloadDir, "${partition.name}_${timestamp}.img.gz")
            
            onProgress(10)
            
            // 备份分区数据到临时文件
            val backupSuccess = if (partition.isImageFile) {
                // 如果是映像文件，直接复制（这种情况在新逻辑中不应该出现）
                val sourceFile = File(partition.path)
                if (sourceFile.exists()) {
                    sourceFile.copyTo(tempBackupFile, overwrite = true)
                    tempBackupFile.exists() && tempBackupFile.length() == sourceFile.length()
                } else {
                    false
                }
            } else {
                // 备份真实分区
                backupRealPartition(partition, tempBackupFile, onProgress)
            }
            
            if (!backupSuccess || !tempBackupFile.exists()) {
                tempBackupFile.delete()
                return@withContext false
            }
            
            onProgress(70)
            
            // 压缩备份文件
            val compressionSuccess = compressFile(tempBackupFile, finalBackupFile, onProgress)
            
            // 删除临时文件
            tempBackupFile.delete()
            
            if (compressionSuccess && finalBackupFile.exists()) {
                onProgress(95)
                
                // 创建备份信息文件
                val infoFile = File(downloadDir, "${partition.name}_${timestamp}.info")
                infoFile.writeText("""
                    小Y字库备份信息
                    ====================
                    作者：小Y
                    🐧：3302719731
                    
                    分区名称: ${partition.name}
                    分区路径: ${partition.path}
                    分区大小: ${partition.getFormattedSize()}
                    备份时间: ${dateFormat.format(Date())}
                    设备型号: ${android.os.Build.MODEL}
                    Android版本: ${android.os.Build.VERSION.RELEASE}
                    压缩格式: GZIP
                    压缩文件: ${finalBackupFile.name}
                """.trimIndent())
                
                // 计算压缩率
                val originalSize = partition.size
                val compressedSize = finalBackupFile.length()
                val compressionRatio = if (originalSize > 0) {
                    ((originalSize - compressedSize) * 100 / originalSize).toInt()
                } else {
                    0
                }
                
                infoFile.appendText("\n压缩率: ${compressionRatio}%")
                
                onProgress(100)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // 备份真实分区 (公开版本供批量备份使用)
    suspend fun backupRealPartition(
        partition: Partition,
        backupFile: File,
        onProgress: (Int) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 首先尝试使用dd命令
            val command = "dd if=${partition.path} of=${backupFile.absolutePath} bs=4096"
            val result = RootManager.executeCommand(command)
            
            if (result != null && result.isSuccess) {
                onProgress(60)
                return@withContext backupFile.exists() && backupFile.length() > 0
            }
            
            // 如果dd命令失败，使用替代方法
            return@withContext backupWithAlternativeMethod(partition, backupFile, onProgress)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // 压缩文件
    private suspend fun compressFile(
        inputFile: File,
        outputFile: File,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val bufferSize = 1024 * 1024 // 1MB buffer
            val buffer = ByteArray(bufferSize)
            val totalSize = inputFile.length()
            var totalRead = 0L
            
            FileInputStream(inputFile).use { input ->
                GZIPOutputStream(FileOutputStream(outputFile)).use { gzipOutput ->
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        gzipOutput.write(buffer, 0, read)
                        totalRead += read
                        
                        // 更新进度（70-90%）
                        val progress = if (totalSize > 0) {
                            (70 + (totalRead * 20 / totalSize)).toInt()
                        } else {
                            80
                        }
                        onProgress(progress.coerceIn(70, 90))
                    }
                }
            }
            
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private suspend fun backupWithAlternativeMethod(
        partition: Partition,
        backupFile: File,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // 使用SuFile读取分区
            val suFile = SuFile(partition.path)
            if (!suFile.exists() || !suFile.canRead()) {
                return@withContext false
            }
            
            onProgress(20)
            
            // 复制分区内容
            val bufferSize = 1024 * 1024 // 1MB buffer
            val buffer = ByteArray(bufferSize)
            var totalRead = 0L
            val totalSize = partition.size
            
            SuFileInputStream.open(suFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        totalRead += read
                        
                        // 更新进度（20-60%）
                        val progress = if (totalSize > 0) {
                            (20 + (totalRead * 40 / totalSize)).toInt()
                        } else {
                            40
                        }
                        onProgress(progress.coerceIn(20, 60))
                    }
                }
            }
            
            onProgress(60)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getBackupDirectory(): File {
        val backupDir = File("/storage/emulated/0/Download")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }
    
    fun getBackupList(): List<File> {
        val backupDir = getBackupDirectory()
        return backupDir.listFiles { file ->
            file.isFile && (file.name.endsWith(".img.gz") || file.name.endsWith(".info"))
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    fun deleteBackup(backupFile: File): Boolean {
        return try {
            if (backupFile.exists()) {
                if (backupFile.isDirectory) {
                    backupFile.deleteRecursively()
                } else {
                    backupFile.delete()
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    // 批量备份到同一个.zip文件
    suspend fun batchBackupToZip(
        partitions: List<Partition>,
        onProgress: (Int, String, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val downloadDir = File("/storage/emulated/0/Download")
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            
            val timestamp = dateFormat.format(Date())
            val zipFile = File(downloadDir, "小Y字库备份_${timestamp}.zip")
            val tempDir = File(downloadDir, "temp_backup_${timestamp}")
            
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            
            try {
                onProgress(5, "创建批量备份文件...", "初始化")
                
                ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                    partitions.forEachIndexed { index, partition ->
                        val baseProgress = (index * 80) / partitions.size
                        onProgress(5 + baseProgress, "正在备份 ${partition.name} (${index + 1}/${partitions.size})", "${partition.name}.img")
                        
                        // 备份单个分区到临时文件
                        val tempBackupFile = File(tempDir, "${partition.name}.img")
                        val backupSuccess = backupRealPartition(partition, tempBackupFile) { progress ->
                            val totalProgress = 5 + baseProgress + (progress * 80 / partitions.size / 100)
                            onProgress(totalProgress, "正在备份 ${partition.name} ($progress%)", "${partition.name}.img")
                        }
                        
                        if (backupSuccess && tempBackupFile.exists()) {
                            // 将备份文件添加到ZIP
                            val zipEntry = ZipEntry("${partition.name}.img")
                            zipOut.putNextEntry(zipEntry)
                            
                            FileInputStream(tempBackupFile).use { input ->
                                val buffer = ByteArray(1024 * 1024) // 1MB buffer
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    zipOut.write(buffer, 0, read)
                                }
                            }
                            
                            zipOut.closeEntry()
                            
                            // 删除临时文件
                            tempBackupFile.delete()
                        } else {
                            onProgress(5 + baseProgress, "备份 ${partition.name} 失败", "${partition.name}.img")
                        }
                    }
                    
                    // 添加备份信息文件
                    onProgress(85, "正在生成备份信息...", "backup_info.txt")
                    val infoEntry = ZipEntry("backup_info.txt")
                    zipOut.putNextEntry(infoEntry)
                    
                    val backupInfo = buildString {
                        appendLine("🌟 XY Root Manager 备份信息 🌟")
                        appendLine("=========================================")
                        appendLine()
                        appendLine("📱 设备信息:")
                        appendLine("   设备型号: ${android.os.Build.MODEL}")
                        appendLine("   品牌: ${android.os.Build.BRAND}")
                        appendLine("   Android版本: ${android.os.Build.VERSION.RELEASE}")
                        appendLine("   API级别: ${android.os.Build.VERSION.SDK_INT}")
                        appendLine("   主板: ${android.os.Build.BOARD}")
                        appendLine("   处理器: ${android.os.Build.HARDWARE}")
                        appendLine()
                        appendLine("📦 备份信息:")
                        appendLine("   备份时间: ${SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA).format(Date())}")
                        appendLine("   分区数量: ${partitions.size}")
                        appendLine("   备份类型: 完整分区镜像")
                        appendLine("   压缩格式: ZIP")
                        appendLine()
                        appendLine("📋 分区详情:")
                        partitions.forEachIndexed { index, partition ->
                            appendLine("   ${index + 1}. ${partition.name}")
                            appendLine("      路径: ${partition.path}")
                            appendLine("      大小: ${FormatUtils.formatFileSize(partition.size)}")
                            appendLine()
                        }
                        appendLine("⚠️ 重要提示:")
                        appendLine("   1. 请妥善保管此备份文件")
                        appendLine("   2. 刷入分区前请确保设备型号匹配")
                        appendLine("   3. 建议在专业人员指导下进行恢复操作")
                        appendLine("   4. 刷入错误的分区可能导致设备无法启动")
                        appendLine()
                        appendLine("=========================================")
                        appendLine("👨‍💻 作者：小Y")
                        appendLine("🐧 QQ：3302719731")
                        appendLine("💬 有任何问题和建议都能通过QQ向我提哦")
                        appendLine("=========================================")
                    }
                    
                    zipOut.write(backupInfo.toByteArray())
                    zipOut.closeEntry()
                }
                
                onProgress(95, "正在完成备份...", "清理临时文件")
                
                // 清理临时目录
                if (tempDir.exists()) {
                    tempDir.deleteRecursively()
                }
                
                onProgress(100, "批量备份完成", "所有文件")
                zipFile.exists() && zipFile.length() > 0
                
            } catch (e: Exception) {
                // 清理临时文件
                if (tempDir.exists()) {
                    tempDir.deleteRecursively()
                }
                if (zipFile.exists()) {
                    zipFile.delete()
                }
                throw e
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
