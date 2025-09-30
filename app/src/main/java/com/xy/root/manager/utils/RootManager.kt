package com.xy.root.manager.utils

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object RootManager {
    
    // 检测Root类型
    enum class RootType {
        NONE,
        MAGISK,
        KERNEL_SU,
        OTHER
    }
    
    suspend fun checkRootAccess(): Pair<Boolean, RootType> = withContext(Dispatchers.IO) {
        try {
            // 首先尝试使用 su 命令
            val process = Runtime.getRuntime().exec("su -c id")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            process.waitFor()
            
            if (process.exitValue() == 0 && result != null && result.contains("uid=0")) {
                // 检测Root类型
                val rootType = detectRootType()
                return@withContext Pair(true, rootType)
            }
        } catch (e: Exception) {
            // 如果直接su失败，尝试使用libsu
            try {
                val shell = Shell.getShell()
                if (shell.isRoot) {
                    val rootType = detectRootType()
                    return@withContext Pair(true, rootType)
                }
            } catch (e2: Exception) {
                // 忽略
            }
        }
        
        Pair(false, RootType.NONE)
    }
    
    private suspend fun detectRootType(): RootType = withContext(Dispatchers.IO) {
        try {
            // 检测 KernelSU
            val kernelSuResult = Shell.cmd("[ -d /data/adb/ksu ] && echo 'found' || echo 'not found'").exec()
            if (kernelSuResult.isSuccess && kernelSuResult.out.firstOrNull() == "found") {
                return@withContext RootType.KERNEL_SU
            }
            
            // 检测 Magisk
            val magiskResult = Shell.cmd("[ -f /data/adb/magisk/magisk ] && echo 'found' || echo 'not found'").exec()
            if (magiskResult.isSuccess && magiskResult.out.firstOrNull() == "found") {
                return@withContext RootType.MAGISK
            }
            
            // 检测其他Root
            val suResult = Shell.cmd("which su").exec()
            if (suResult.isSuccess && suResult.out.isNotEmpty()) {
                return@withContext RootType.OTHER
            }
        } catch (e: Exception) {
            // 忽略错误
        }
        
        RootType.OTHER
    }
    
    suspend fun executeCommand(command: String): Shell.Result? = withContext(Dispatchers.IO) {
        try {
            Shell.cmd(command).exec()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun testRootCommand(): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = Shell.cmd("id").exec()
            result.isSuccess && result.out.any { it.contains("uid=0") }
        } catch (e: Exception) {
            false
        }
    }

    // 解析符号链接的真实块设备路径（如 /dev/block/mmcblk0p1 或 /dev/block/dm-0）
    suspend fun resolveRealBlockPath(byNamePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val real = Shell.cmd("realpath $byNamePath").exec()
            if (real.isSuccess && real.out.isNotEmpty()) real.out[0].trim() else null
        } catch (_: Exception) {
            null
        }
    }

    // 从 sysfs 读取块设备大小（字节）。当 blockdev 不可用时的回退方案
    suspend fun readBlockSizeBytes(realBlockPath: String): Long = withContext(Dispatchers.IO) {
        try {
            val blockName = realBlockPath.substringAfterLast('/')
            val sectorRes = Shell.cmd("cat /sys/class/block/$blockName/size 2>/dev/null").exec()
            if (sectorRes.isSuccess && sectorRes.out.isNotEmpty()) {
                val sectors = sectorRes.out[0].trim().toLongOrNull() ?: return@withContext 0L
                return@withContext sectors * 512L
            }
        } catch (_: Exception) { }
        0L
    }
}
