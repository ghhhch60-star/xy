package com.xy.root.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * 网络连接测试工具
 * 用于测试不同镜像源的可用性
 */
object NetworkTestUtil {
    
    private val testClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    /**
     * 测试 URL 的可访问性
     */
    suspend fun testUrlAccessibility(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head() // 只获取头部信息，减少数据传输
                .addHeader("User-Agent", "IYTH-App-NetworkTest/1.0")
                .build()
                
            val response = testClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 测试多个 URL 并返回第一个可用的
     */
    suspend fun findFirstAccessibleUrl(urls: List<String>): String? = withContext(Dispatchers.IO) {
        for (url in urls) {
            if (testUrlAccessibility(url)) {
                return@withContext url
            }
        }
        null
    }
    
    /**
     * 检测网络环境（是否在中国大陆）
     */
    suspend fun detectNetworkEnvironment(): NetworkEnvironment = withContext(Dispatchers.IO) {
        try {
            // 尝试访问 GitHub API
            val githubAccessible = testUrlAccessibility("https://api.github.com")
            
            // 尝试访问百度（中国大陆网络测试）
            val baiduAccessible = testUrlAccessibility("https://www.baidu.com")
            
            when {
                githubAccessible && baiduAccessible -> NetworkEnvironment.GLOBAL
                !githubAccessible && baiduAccessible -> NetworkEnvironment.CHINA_MAINLAND
                githubAccessible && !baiduAccessible -> NetworkEnvironment.OVERSEAS
                else -> NetworkEnvironment.LIMITED
            }
        } catch (e: Exception) {
            NetworkEnvironment.UNKNOWN
        }
    }
}

/**
 * 网络环境类型
 */
enum class NetworkEnvironment {
    GLOBAL,         // 全球网络（可访问 GitHub）
    CHINA_MAINLAND, // 中国大陆网络（需要镜像）
    OVERSEAS,       // 海外网络（无法访问中国网站）
    LIMITED,        // 受限网络
    UNKNOWN         // 未知网络环境
}
