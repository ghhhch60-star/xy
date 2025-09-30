package com.xy.root.manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import com.xy.root.manager.model.FileItem
import com.xy.root.manager.model.Partition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PartitionFilesViewModel : ViewModel() {
    
    private val _fileList = MutableLiveData<List<FileItem>>()
    val fileList: LiveData<List<FileItem>> = _fileList
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> = _statusMessage
    
    private val _selectedFiles = MutableLiveData<List<FileItem>>()
    val selectedFiles: LiveData<List<FileItem>> = _selectedFiles
    
    fun loadPartitionFiles(partition: Partition) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _statusMessage.postValue("正在加载文件列表...")
            
            val files = withContext(Dispatchers.IO) {
                try {
                    if (partition.isImageFile) {
                        // 处理映像文件
                        loadImageFileContents(partition)
                    } else {
                        // 处理系统分区
                        loadSystemPartitionFiles(partition)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PartitionFilesViewModel", "加载文件列表失败", e)
                    _statusMessage.postValue("加载失败: ${e.message}")
                    emptyList<FileItem>()
                }
            }
            
            _fileList.postValue(files)
            _isLoading.postValue(false)
            
            if (files.isEmpty()) {
                _statusMessage.postValue("未找到文件或无权限访问")
            } else {
                _statusMessage.postValue("加载完成，共 ${files.size} 个文件")
            }
        }
    }
    
    private suspend fun loadSystemPartitionFiles(partition: Partition): List<FileItem> {
        val files = mutableListOf<FileItem>()
        
        // 对于系统分区，我们需要先尝试挂载
        val mountPoint = "/tmp/partition_${partition.name}"
        
        try {
            // 创建挂载点
            Shell.cmd("mkdir -p $mountPoint").exec()
            
            // 尝试挂载分区（只读）
            val mountResult = when {
                partition.name.contains("system") -> {
                    Shell.cmd("mount -o ro ${partition.path} $mountPoint").exec()
                }
                partition.name.contains("vendor") -> {
                    Shell.cmd("mount -o ro ${partition.path} $mountPoint").exec()
                }
                partition.name.contains("boot") -> {
                    // boot分区通常不能直接挂载，尝试使用hexdump查看内容
                    return loadBootPartitionInfo(partition)
                }
                else -> {
                    // 其他分区尝试通用挂载
                    Shell.cmd("mount -o ro ${partition.path} $mountPoint").exec()
                }
            }
            
            if (mountResult.isSuccess) {
                // 挂载成功，列出文件
                val listResult = Shell.cmd("find $mountPoint -maxdepth 2 -type f | head -50").exec()
                if (listResult.isSuccess) {
                    listResult.out.forEach { filePath ->
                        val relativePath = filePath.removePrefix(mountPoint).removePrefix("/")
                        if (relativePath.isNotEmpty()) {
                            // 获取文件信息
                            val statResult = Shell.cmd("stat -c '%s %Y %n' '$filePath'").exec()
                            if (statResult.isSuccess && statResult.out.isNotEmpty()) {
                                val parts = statResult.out[0].split(" ", limit = 3)
                                if (parts.size >= 2) {
                                    files.add(
                                        FileItem(
                                            name = relativePath,
                                            path = filePath,
                                            size = parts[0].toLongOrNull() ?: 0L,
                                            isDirectory = false,
                                            modifiedTime = parts[1].toLongOrNull() ?: 0L
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 卸载分区
                Shell.cmd("umount $mountPoint").exec()
            } else {
                // 挂载失败，尝试其他方法
                return loadPartitionInfo(partition)
            }
            
        } finally {
            // 清理挂载点
            Shell.cmd("rmdir $mountPoint").exec()
        }
        
        return files
    }
    
    private suspend fun loadImageFileContents(partition: Partition): List<FileItem> {
        val files = mutableListOf<FileItem>()
        
        // 对于映像文件，显示基本信息
        val file = File(partition.path)
        files.add(
            FileItem(
                name = "映像文件信息",
                path = partition.path,
                size = file.length(),
                isDirectory = false,
                modifiedTime = file.lastModified() / 1000
            )
        )
        
        // 尝试分析映像文件类型
        val fileResult = Shell.cmd("file '${partition.path}'").exec()
        if (fileResult.isSuccess && fileResult.out.isNotEmpty()) {
            files.add(
                FileItem(
                    name = "文件类型: ${fileResult.out[0]}",
                    path = "",
                    size = 0L,
                    isDirectory = false,
                    modifiedTime = 0L
                )
            )
        }
        
        return files
    }
    
    private suspend fun loadBootPartitionInfo(partition: Partition): List<FileItem> {
        val files = mutableListOf<FileItem>()
        
        // 对于boot分区，显示分区头信息
        val hexdumpResult = Shell.cmd("hexdump -C ${partition.path} | head -20").exec()
        if (hexdumpResult.isSuccess) {
            files.add(
                FileItem(
                    name = "分区头信息 (前320字节)",
                    path = "hexdump",
                    size = 0L,
                    isDirectory = false,
                    modifiedTime = 0L
                )
            )
        }
        
        // 尝试检测Android boot image
        val magicResult = Shell.cmd("dd if=${partition.path} bs=8 count=1 2>/dev/null | strings").exec()
        if (magicResult.isSuccess && magicResult.out.isNotEmpty()) {
            files.add(
                FileItem(
                    name = "Boot Magic: ${magicResult.out.joinToString()}",
                    path = "",
                    size = 0L,
                    isDirectory = false,
                    modifiedTime = 0L
                )
            )
        }
        
        return files
    }
    
    private suspend fun loadPartitionInfo(partition: Partition): List<FileItem> {
        val files = mutableListOf<FileItem>()
        
        // 显示分区基本信息
        files.add(
            FileItem(
                name = "分区路径: ${partition.path}",
                path = "",
                size = partition.size,
                isDirectory = false,
                modifiedTime = 0L
            )
        )
        
        // 尝试获取文件系统类型
        val blkidResult = Shell.cmd("blkid ${partition.path}").exec()
        if (blkidResult.isSuccess && blkidResult.out.isNotEmpty()) {
            files.add(
                FileItem(
                    name = "文件系统信息",
                    path = blkidResult.out[0],
                    size = 0L,
                    isDirectory = false,
                    modifiedTime = 0L
                )
            )
        }
        
        return files
    }
    
    fun updateFileSelection(file: FileItem, isSelected: Boolean) {
        val currentList = _fileList.value ?: return
        val updatedList = currentList.map { fileItem ->
            if (fileItem.path == file.path && fileItem.name == file.name) {
                fileItem.copy(isSelected = isSelected)
            } else {
                fileItem
            }
        }
        _fileList.postValue(updatedList)
        updateSelectedFiles(updatedList)
    }
    
    fun selectAllFiles(selectAll: Boolean) {
        val currentList = _fileList.value ?: return
        
        // 如果状态没有变化，直接返回
        val allSelected = currentList.all { it.isSelected }
        val noneSelected = currentList.none { it.isSelected }
        
        if ((selectAll && allSelected) || (!selectAll && noneSelected)) {
            return
        }
        
        val updatedList = currentList.map { fileItem ->
            fileItem.copy(isSelected = selectAll)
        }
        _fileList.postValue(updatedList)
        updateSelectedFiles(updatedList)
    }
    
    fun refreshFileSelection() {
        val currentList = _fileList.value ?: return
        // 重新触发UI更新，保持当前选择状态
        _fileList.postValue(currentList)
        updateSelectedFiles(currentList)
        _statusMessage.postValue("已刷新文件选择状态")
    }
    
    private fun updateSelectedFiles(fileList: List<FileItem>) {
        val selected = fileList.filter { it.isSelected }
        _selectedFiles.postValue(selected)
    }
}
