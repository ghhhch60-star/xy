package com.xy.root.manager

import android.app.Application
import com.topjohnwu.superuser.Shell

class XYApplication : Application() {
    
    companion object {
        init {
            // 设置Shell配置 - 使用更安全的配置
            Shell.enableVerboseLogging = false
            Shell.setDefaultBuilder(Shell.Builder.create()
                .setFlags(Shell.FLAG_MOUNT_MASTER or Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10))
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        // 不在这里初始化Shell，让它按需初始化
        // 这样可以避免在获得Root权限时崩溃
    }
}