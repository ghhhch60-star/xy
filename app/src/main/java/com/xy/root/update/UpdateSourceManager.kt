package com.xy.root.update

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 更新源管理器
 * 智能选择最佳的更新源和下载镜像
 */
class UpdateSourceManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "update_source_prefs"
        private const val KEY_LAST_SUCCESSFUL_SOURCE = "last_successful_source"
        private const val KEY_LAST_SUCCESSFUL_MIRROR = "last_successful_mirror"
        private const val KEY_NETWORK_ENVIRONMENT = "network_environment"
        private const val KEY_LAST_TEST_TIME = "last_test_time"
        private const val TEST_INTERVAL = 24 * 60 * 60 * 1000L // 24小时
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // 更新源列表（按优先级排序）
    private val updateSources = mapOf(
        NetworkEnvironment.CHINA_MAINLAND to listOf(
            "https://ghproxy.com/https://api.github.com/repos/ghhhch60-star/xy/releases/latest",
            "https://mirror.ghproxy.com/https://api.github.com/repos/ghhhch60-star/xy/releases/latest",
            "https://gh-proxy.com/https://api.github.com/repos/ghhhch60-star/xy/releases/latest",
            "https://cdn.jsdelivr.net/gh/ghhhch60-star/xy@main/release-info.json"
        ),
        NetworkEnvironment.GLOBAL to listOf(
            "https://api.github.com/repos/ghhhch60-star/xy/releases/latest",
            "https://ghproxy.com/https://api.github.com/repos/ghhhch60-star/xy/releases/latest"
        ),
        NetworkEnvironment.OVERSEAS to listOf(
            "https://api.github.com/repos/ghhhch60-star/xy/releases/latest",
            "https://ghproxy.com/https://api.github.com/repos/ghhhch60-star/xy/releases/latest"
        )
    )
    
    // 下载镜像列表
    private val downloadMirrors = mapOf(
        NetworkEnvironment.CHINA_MAINLAND to listOf(
            "https://ghproxy.com/",
            "https://mirror.ghproxy.com/",
            "https://gh-proxy.com/",
            "https://download.fastgit.org/",
            ""
        ),
        NetworkEnvironment.GLOBAL to listOf(
            "",
            "https://ghproxy.com/"
        ),
        NetworkEnvironment.OVERSEAS to listOf(
            "",
            "https://ghproxy.com/"
        )
    )
    
    /**
     * 获取最佳更新源列表
     */
    suspend fun getBestUpdateSources(): List<String> = withContext(Dispatchers.IO) {
        val networkEnv = getNetworkEnvironment()
        val sources = updateSources[networkEnv] ?: updateSources[NetworkEnvironment.GLOBAL]!!
        
        // 如果有上次成功的源，将其放在首位
        val lastSuccessfulSource = prefs.getString(KEY_LAST_SUCCESSFUL_SOURCE, null)
        if (lastSuccessfulSource != null && sources.contains(lastSuccessfulSource)) {
            val reorderedSources = mutableListOf(lastSuccessfulSource)
            reorderedSources.addAll(sources.filter { it != lastSuccessfulSource })
            reorderedSources
        } else {
            sources
        }
    }
    
    /**
     * 获取最佳下载镜像
     */
    suspend fun getBestDownloadMirror(originalUrl: String): String = withContext(Dispatchers.IO) {
        if (!originalUrl.contains("github.com")) {
            return@withContext originalUrl
        }
        
        val networkEnv = getNetworkEnvironment()
        val mirrors = downloadMirrors[networkEnv] ?: downloadMirrors[NetworkEnvironment.GLOBAL]!!
        
        // 如果有上次成功的镜像，优先使用
        val lastSuccessfulMirror = prefs.getString(KEY_LAST_SUCCESSFUL_MIRROR, null)
        if (lastSuccessfulMirror != null && mirrors.contains(lastSuccessfulMirror)) {
            return@withContext if (lastSuccessfulMirror.isEmpty()) originalUrl else "$lastSuccessfulMirror$originalUrl"
        }
        
        // 否则使用第一个镜像
        val bestMirror = mirrors.first()
        return@withContext if (bestMirror.isEmpty()) originalUrl else "$bestMirror$originalUrl"
    }
    
    /**
     * 记录成功的更新源
     */
    fun recordSuccessfulSource(sourceUrl: String) {
        prefs.edit().putString(KEY_LAST_SUCCESSFUL_SOURCE, sourceUrl).apply()
    }
    
    /**
     * 记录成功的下载镜像
     */
    fun recordSuccessfulMirror(mirrorUrl: String) {
        val mirror = if (mirrorUrl.contains("github.com")) {
            // 提取镜像前缀
            val githubIndex = mirrorUrl.indexOf("https://github.com")
            if (githubIndex > 0) {
                mirrorUrl.substring(0, githubIndex)
            } else {
                ""
            }
        } else {
            ""
        }
        prefs.edit().putString(KEY_LAST_SUCCESSFUL_MIRROR, mirror).apply()
    }
    
    /**
     * 获取网络环境
     */
    private suspend fun getNetworkEnvironment(): NetworkEnvironment {
        val lastTestTime = prefs.getLong(KEY_LAST_TEST_TIME, 0)
        val currentTime = System.currentTimeMillis()
        
        // 如果距离上次测试不到24小时，使用缓存的结果
        if (currentTime - lastTestTime < TEST_INTERVAL) {
            val cachedEnv = prefs.getString(KEY_NETWORK_ENVIRONMENT, null)
            if (cachedEnv != null) {
                try {
                    return NetworkEnvironment.valueOf(cachedEnv)
                } catch (e: IllegalArgumentException) {
                    // 忽略无效的缓存值
                }
            }
        }
        
        // 重新检测网络环境
        val networkEnv = NetworkTestUtil.detectNetworkEnvironment()
        
        // 缓存结果
        prefs.edit()
            .putString(KEY_NETWORK_ENVIRONMENT, networkEnv.name)
            .putLong(KEY_LAST_TEST_TIME, currentTime)
            .apply()
        
        return networkEnv
    }
    
    /**
     * 清除缓存，强制重新检测网络环境
     */
    fun clearCache() {
        prefs.edit()
            .remove(KEY_NETWORK_ENVIRONMENT)
            .remove(KEY_LAST_TEST_TIME)
            .apply()
    }
    
    /**
     * 获取网络环境描述
     */
    suspend fun getNetworkEnvironmentDescription(): String {
        return when (getNetworkEnvironment()) {
            NetworkEnvironment.CHINA_MAINLAND -> "中国大陆网络 (使用镜像源)"
            NetworkEnvironment.GLOBAL -> "全球网络 (直连GitHub)"
            NetworkEnvironment.OVERSEAS -> "海外网络"
            NetworkEnvironment.LIMITED -> "受限网络环境"
            NetworkEnvironment.UNKNOWN -> "未知网络环境"
        }
    }
}
