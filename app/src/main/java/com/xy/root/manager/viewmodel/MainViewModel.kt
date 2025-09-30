package com.xy.root.manager.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import com.xy.root.manager.model.FileItem
import com.xy.root.manager.model.Partition
import com.xy.root.manager.utils.BackupManager
import com.xy.root.manager.utils.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _isRooted = MutableLiveData<Boolean>()
    val isRooted: LiveData<Boolean> = _isRooted
    
    private val _rootType = MutableLiveData<RootManager.RootType>()
    val rootType: LiveData<RootManager.RootType> = _rootType
    
    private val _partitionList = MutableLiveData<List<Partition>>()
    val partitionList: LiveData<List<Partition>> = _partitionList
    
    private val _selectedPartitions = MutableLiveData<List<Partition>>()
    val selectedPartitions: LiveData<List<Partition>> = _selectedPartitions
    
    private val _backupProgress = MutableLiveData<Int>()
    val backupProgress: LiveData<Int> = _backupProgress
    
    // 详细备份进度信息
    private val _backupProgressText = MutableLiveData<String>()
    val backupProgressText: LiveData<String> = _backupProgressText
    
    private val _currentBackupFile = MutableLiveData<String>()
    val currentBackupFile: LiveData<String> = _currentBackupFile
    
    private val _isBackupInProgress = MutableLiveData<Boolean>()
    val isBackupInProgress: LiveData<Boolean> = _isBackupInProgress
    
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    
    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning
    
    // 分区大小缓存，避免重复获取
    private val partitionSizeCache = mutableMapOf<String, Long>()
    
    private val backupManager = BackupManager(application)
    
    // 不需要分区名称过滤器，扫描所有分区
    
    // 切换备份模式
    
    fun checkRootStatus() {
        viewModelScope.launch {
            _statusMessage.postValue("正在检查Root权限...")
            val (hasRoot, type) = RootManager.checkRootAccess()
            _isRooted.postValue(hasRoot)
            _rootType.postValue(type)
            
            if (hasRoot) {
                _statusMessage.postValue("Root权限已获取 (${type.name})")
            } else {
                _statusMessage.postValue("未检测到Root权限")
            }
        }
    }
    
    /**
     * 自动检测Root权限并在成功时加载分区
     * 用于启动时的静默检测
     */
    fun autoCheckRootAndLoadPartitions() {
        viewModelScope.launch {
            try {
                val (hasRoot, type) = RootManager.checkRootAccess()
                _isRooted.postValue(hasRoot)
                _rootType.postValue(type)
                
                if (hasRoot) {
                    _statusMessage.postValue("Root权限已获取 (${type.name})")
                    // 自动加载分区列表
                    loadPartitions()
                } else {
                    _statusMessage.postValue("未检测到Root权限")
                }
            } catch (e: Exception) {
                // 静默处理错误，不显示错误消息
                _isRooted.postValue(false)
                _statusMessage.postValue("未检测到Root权限")
            }
        }
    }
    
    fun detectRootType(callback: (RootManager.RootType) -> Unit) {
        viewModelScope.launch {
            val (_, type) = RootManager.checkRootAccess()
            withContext(Dispatchers.Main) {
                callback(type)
            }
        }
    }
    
    fun requestRootPermission() {
        viewModelScope.launch {
            _statusMessage.postValue("正在请求Root权限...")
            try {
                // 使用更安全的方式请求Root
                val hasRoot = withContext(Dispatchers.IO) {
                    RootManager.testRootCommand()
                }
                
                if (hasRoot) {
                    val (_, type) = RootManager.checkRootAccess()
                    _isRooted.postValue(true)
                    _rootType.postValue(type)
                    _statusMessage.postValue("Root权限获取成功")
                    loadPartitions()
                } else {
                    _isRooted.postValue(false)
                    _errorMessage.postValue("Root权限获取失败，请确保设备已Root并授予权限")
                }
            } catch (e: Exception) {
                _isRooted.postValue(false)
                _errorMessage.postValue("Root权限请求失败: ${e.message}")
            }
        }
    }
    
    fun loadPartitions() {
        viewModelScope.launch {
            _isScanning.postValue(true)
            _statusMessage.postValue("正在扫描设备分区文件...")
            
            val partitions = withContext(Dispatchers.IO) {
                try {
                    // 首先获取本地参考文件夹的分区类型
                    val referencePartitions = getReferencePartitionTypes()
                    
                    if (referencePartitions.isEmpty()) {
                        android.util.Log.w("MainViewModel", "参考文件夹为空: C:\\Users\\Qinwy\\Desktop\\images")
                        return@withContext emptyList<Partition>()
                    }
                    
                    android.util.Log.d("MainViewModel", "参考分区类型: $referencePartitions")
                    
                    // 扫描设备上的实际分区
                    val foundPartitions = scanDevicePartitions(referencePartitions)
                    
                    // 同时扫描备份文件夹中已备份的分区文件
                    val backupPartitions = scanBackupPartitions(referencePartitions)
                    
                    // 合并结果，优先显示设备分区
                    val allPartitions = mutableListOf<Partition>()
                    allPartitions.addAll(foundPartitions)
                    
                    // 添加备份文件（仅当设备上没有对应分区时）
                    backupPartitions.forEach { backup ->
                        if (!allPartitions.any { it.name == backup.name }) {
                            allPartitions.add(backup)
                        }
                    }
                    
                    // 按名称排序
                    allPartitions.sortBy { it.name }
                    
                    android.util.Log.d("MainViewModel", "扫描完成，总共找到 ${allPartitions.size} 个分区")
                    
                    allPartitions
                    
                } catch (e: Exception) {
                    android.util.Log.e("MainViewModel", "扫描分区失败", e)
                    _errorMessage.postValue("扫描分区失败: ${e.message}")
                    emptyList()
                }
            }
            
            _partitionList.postValue(partitions)
            updateStatusMessage(partitions)
            _isScanning.postValue(false)
        }
    }
    
    private suspend fun getReferencePartitionTypes(): Set<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 首先尝试读取同步的参考文件列表
                val referenceResult = Shell.sh("cat /sdcard/pc_reference/reference_list.txt 2>/dev/null").exec()
                
                if (referenceResult.isSuccess && referenceResult.out.isNotEmpty()) {
                    // 成功读取到参考文件列表
                    val referenceFiles = referenceResult.out
                        .filter { it.isNotEmpty() && it.endsWith(".img") }
                        .map { it.removeSuffix(".img") }
                        .toSet()
                    
                    android.util.Log.d("MainViewModel", "从同步的参考列表获取到 ${referenceFiles.size} 个分区类型")
                    android.util.Log.d("MainViewModel", "参考分区类型: $referenceFiles")
                    
                    if (referenceFiles.isNotEmpty()) {
                        return@withContext referenceFiles
                    }
                }
                
                // 如果没有同步的参考信息，使用基于您images文件夹的预定义分区列表
                android.util.Log.d("MainViewModel", "未找到同步的参考信息，使用预定义分区列表")
                
                setOf(
                    "abl_a", "abl_b", "ALIGN_TO_128K_1", "ALIGN_TO_128K_2", "aop_a", "aop_b",
                    "bluetooth_a", "bluetooth_b", "boot_a", "boot_b", "cache", "cdt",
                    "cmnlib_a", "cmnlib_b", "cmnlib64_a", "cmnlib64_b", "ddr", "devcfg_a", "devcfg_b",
                    "devinfo", "dip", "dsp_a", "dsp_b", "dtbo_a", "dtbo_b", "fdemeta", "fsc", "fsg",
                    "hyp_a", "hyp_b", "imagefv_a", "imagefv_b", "keymaster_a", "keymaster_b",
                    "keystore", "limits", "logdump", "logfs", "mdtp_a", "mdtp_b", "mdtpsecapp_a", "mdtpsecapp_b",
                    "misc", "modem_a", "modem_b", "modemst1", "modemst2", "msadp", "multiimgoem_a", "multiimgoem_b",
                    "multiimgqti_a", "multiimgqti_b", "persist", "qupfw_a", "qupfw_b", "rawdump",
                    "recovery_a", "recovery_b", "sec", "spunvm", "splash", "spss_a", "spss_b",
                    "ssd", "storsec_a", "storsec_b", "system_a", "system_b", "systemrw_a", "systemrw_b",
                    "toolsfv", "tz_a", "tz_b", "uefisecapp_a", "uefisecapp_b", "userdata",
                    "vbmeta_a", "vbmeta_b", "vbmeta_system_a", "vbmeta_system_b", "vendor_a", "vendor_b",
                    "xbl_a", "xbl_b", "xbl_config_a", "xbl_config_b"
                )
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "获取参考分区类型失败", e)
                emptySet()
            }
        }
    }
    
    private suspend fun scanDevicePartitions(referencePartitions: Set<String>): List<Partition> {
        return withContext(Dispatchers.IO) {
            val foundPartitions = mutableListOf<Partition>()
            
            // 定义要扫描的分区路径
            val partitionPaths = listOf(
                "/dev/block/bootdevice/by-name",
                "/dev/block/platform/soc/1d84000.ufshc/by-name",
                "/dev/block/platform/soc/7c4000.sdhci/by-name", 
                "/dev/block/by-name"
            )
            
            for (basePath in partitionPaths) {
                try {
                    val result = Shell.sh("ls -la $basePath 2>/dev/null").exec()
                    if (result.isSuccess && result.out.isNotEmpty()) {
                        android.util.Log.d("MainViewModel", "扫描分区路径: $basePath")
                        
                        for (line in result.out) {
                            if (line.contains("->")) {
                                val parts = line.split("->")
                                if (parts.size >= 2) {
                                    val linkName = parts[0].trim().split(" ").lastOrNull()
                                    val targetPath = parts[1].trim()
                                    
                                    // 只扫描参考文件夹中存在的分区类型
                                    if (!linkName.isNullOrEmpty() && referencePartitions.contains(linkName)) {
                                        val size = getPartitionSize(targetPath)
                                        foundPartitions.add(
                                            Partition(
                                                name = linkName,
                                                path = targetPath,
                                                size = size,
                                                isBackedUp = checkIfBackedUp(linkName),
                                                isImageFile = false // 这是设备分区，不是映像文件
                                            )
                                        )
                                        android.util.Log.d("MainViewModel", "找到匹配分区: $linkName -> $targetPath")
                                    }
                                }
                            }
                        }
                        break // 找到有效路径就停止
                    }
                } catch (e: Exception) {
                    android.util.Log.w("MainViewModel", "扫描路径失败: $basePath", e)
                    // 继续尝试下一个路径
                }
            }
            
            foundPartitions.distinctBy { it.name }
        }
    }
    
    private suspend fun scanBackupPartitions(referencePartitions: Set<String>): List<Partition> {
        return withContext(Dispatchers.IO) {
            val backupPartitions = mutableListOf<Partition>()
            
            val downloadDir = File("/storage/emulated/0/Download")
            if (downloadDir.exists()) {
                downloadDir.listFiles { file ->
                    file.isFile && file.name.endsWith(".img.gz")
                }?.forEach { file ->
                    // 从文件名提取分区名称 (例如: abl_a_20250929_143022.img.gz -> abl_a)
                    val fileName = file.nameWithoutExtension.removeSuffix(".img")
                    val partitionName = fileName.substringBeforeLast("_").substringBeforeLast("_")
                    
                    // 只添加参考文件夹中存在的分区类型
                    if (partitionName.isNotEmpty() && referencePartitions.contains(partitionName)) {
                        backupPartitions.add(
                            Partition(
                                name = partitionName,
                                path = file.absolutePath,
                                size = file.length(),
                                isBackedUp = true,
                                isImageFile = true
                            )
                        )
                        android.util.Log.d("MainViewModel", "找到备份分区: $partitionName")
                    }
                }
            }
            
            backupPartitions
        }
    }
    
    private suspend fun getPartitionSize(devicePath: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                if (partitionSizeCache.containsKey(devicePath)) {
                    return@withContext partitionSizeCache[devicePath] ?: 0L
                }
                
                val result = Shell.sh("blockdev --getsize64 '$devicePath' 2>/dev/null").exec()
                if (result.isSuccess && result.out.isNotEmpty()) {
                    val size = result.out[0].toLongOrNull() ?: 0L
                    partitionSizeCache[devicePath] = size
                    size
                } else {
                    0L
                }
            } catch (e: Exception) {
                0L
            }
        }
    }
    
    private fun updateStatusMessage(partitions: List<Partition>) {
        val totalPartitions = partitions.size
        val devicePartitions = partitions.count { !it.isImageFile }
        val backupPartitions = partitions.count { it.isBackedUp }
        
        if (partitions.isEmpty()) {
            _statusMessage.postValue("未找到匹配的分区\n\n📁 参考来源: C:\\Users\\Qinwy\\Desktop\\images\n\n💡 使用说明:\n1. 运行 sync_reference.bat 同步参考信息\n2. 或者应用将使用预设的分区列表\n3. 设备需要Root权限才能扫描分区")
        } else {
            _statusMessage.postValue("扫描完成 ✅\n\n📁 参考来源: C:\\Users\\Qinwy\\Desktop\\images\n🔍 匹配分区: $totalPartitions 个\n📱 设备分区: $devicePartitions 个\n💾 已备份: $backupPartitions 个\n\n💡 只显示参考文件夹中存在的分区类型")
        }
    }
    
    fun backupPartition(partition: Partition) {
        viewModelScope.launch {
            _statusMessage.postValue("正在备份 ${partition.name}...")
            _backupProgress.postValue(10)
            
            val success = withContext(Dispatchers.IO) {
                backupManager.backupPartition(partition) { progress ->
                    _backupProgress.postValue(progress)
                }
            }
            
            if (success) {
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val fileName = "${partition.name}_${timestamp}.img.gz"
                _statusMessage.postValue("${partition.name} 备份成功！\n\n📁 文件位置: /storage/emulated/0/Download/\n📄 文件名: $fileName\n💾 格式: GZIP压缩\n\n可在文件管理器的下载文件夹中找到备份文件")
                // 更新分区状态
                val updatedList = _partitionList.value?.map {
                    if (it.name == partition.name) {
                        it.copy(isBackedUp = true)
                    } else {
                        it
                    }
                }
                _partitionList.postValue(updatedList ?: emptyList())
            } else {
                _errorMessage.postValue("${partition.name} 备份失败")
            }
            
            _backupProgress.postValue(0)
        }
    }
    
    
    private fun checkIfBackedUp(partitionName: String): Boolean {
        // 检查Download文件夹中是否有该分区的压缩备份文件
        val backupDir = File("/storage/emulated/0/Download")
        backupDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name.startsWith("${partitionName}_") && file.name.endsWith(".img.gz")) {
                return true
            }
        }
        return false
    }
    
    // 选择管理功能
    fun updatePartitionSelection(partition: Partition, isSelected: Boolean) {
        val currentList = _partitionList.value ?: return
        val updatedList = currentList.map {
            if (it.name == partition.name && it.path == partition.path) {
                it.copy(isSelected = isSelected)
            } else {
                it
            }
        }.toMutableList() // 确保创建新的列表实例
        
        android.util.Log.d("MainViewModel", "updatePartitionSelection: ${partition.name} -> $isSelected")
        _partitionList.postValue(updatedList)
        _selectedPartitions.postValue(updatedList.filter { it.isSelected })
    }
    
    fun getSelectedPartitions(): List<Partition> {
        return _partitionList.value?.filter { it.isSelected } ?: emptyList()
    }
    
    fun selectAllPartitions(selectAll: Boolean) {
        val currentList = _partitionList.value ?: return
        
        // 批量更新所有分区的选中状态
        val updatedList = currentList.map { partition ->
            partition.copy(isSelected = selectAll)
        }
        
        android.util.Log.d("MainViewModel", "selectAllPartitions: $selectAll, 更新 ${updatedList.size} 个分区")
        
        // 打印更新前后的部分状态用于调试
        currentList.take(3).forEachIndexed { index, partition ->
            android.util.Log.d("MainViewModel", "  Before Item $index: ${partition.name} isSelected=${partition.isSelected}")
        }
        updatedList.take(3).forEachIndexed { index, partition ->
            android.util.Log.d("MainViewModel", "  After Item $index: ${partition.name} isSelected=${partition.isSelected}")
        }
        
        // 确保使用 postValue 触发 Observer
        _partitionList.postValue(updatedList)
        _selectedPartitions.postValue(if (selectAll) updatedList else emptyList())
    }
    
    fun batchBackupPartitions(partitions: List<Partition>) {
        viewModelScope.launch {
            _isBackupInProgress.postValue(true)
            _backupProgressText.postValue("准备开始备份...")
            _currentBackupFile.postValue("正在初始化...")
            _backupProgress.postValue(0)
            
            val success = withContext(Dispatchers.IO) {
                backupManager.batchBackupToZip(partitions) { progress, message, currentFile ->
                    _backupProgress.postValue(progress)
                    _backupProgressText.postValue(message)
                    _currentBackupFile.postValue("当前文件: $currentFile")
                }
            }
            
            // 更新分区状态
            val updatedList = _partitionList.value?.map { partition ->
                if (partitions.any { it.name == partition.name }) {
                    partition.copy(isBackedUp = checkIfBackedUp(partition.name), isSelected = false)
                } else {
                    partition
                }
            }
            _partitionList.postValue(updatedList ?: emptyList())
            
            if (success) {
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val fileName = "小Y字库备份_${timestamp}.zip"
                _statusMessage.postValue("🎉 批量备份成功完成！\n\n📦 已备份: ${partitions.size} 个分区\n📁 文件位置: /storage/emulated/0/Download/\n📄 文件名: $fileName\n💾 格式: ZIP压缩包\n\n✨ 压缩包内包含:\n• 所有分区的.img文件\n• backup_info.txt 详细信息\n\n可在文件管理器的下载文件夹中找到备份文件")
                _backupProgressText.postValue("备份完成！")
                _currentBackupFile.postValue("所有文件备份完成")
            } else {
                _errorMessage.postValue("批量备份失败，请检查设备存储空间和Root权限")
                _backupProgressText.postValue("备份失败")
                _currentBackupFile.postValue("备份过程中出现错误")
            }
            
            // 延迟隐藏进度卡片
            kotlinx.coroutines.delay(3000)
            _isBackupInProgress.postValue(false)
            _backupProgress.postValue(0)
            _selectedPartitions.postValue(emptyList())
        }
    }
}