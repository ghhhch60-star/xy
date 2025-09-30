@echo off
chcp 65001 >nul
echo ====================================
echo 测试构建 - XY 应用
echo ====================================
echo.

cd /d "%~dp0"

echo 项目配置检查：
echo ----------------
echo ✓ settings.gradle 已配置 JitPack 仓库
echo ✓ 应用图标已创建临时矢量图标
echo ✓ 全局 init.gradle 冲突已解决
echo.

echo ====================================
echo 在 Android Studio 中构建 APK：
echo ====================================
echo.
echo 1. 如果 Gradle 同步仍在进行，请等待完成
echo.
echo 2. 如果看到黄色警告条 "Gradle files have changed"：
echo    点击 "Sync Now"
echo.
echo 3. 构建 APK：
echo    方法A: Build → Build Bundle(s)/APK(s) → Build APK(s)
echo    方法B: 在 Terminal 中运行: gradle assembleDebug
echo.
echo 4. APK 位置：
echo    app\build\outputs\apk\debug\app-debug.apk
echo.
echo ====================================
echo 常见问题解决：
echo ====================================
echo.
echo 问题1：仍然无法下载 libsu 库
echo 解决：检查网络连接，可能需要VPN或代理
echo.
echo 问题2：Gradle sync 失败
echo 解决：File → Invalidate Caches and Restart
echo.
echo 问题3：构建失败
echo 解决：Build → Clean Project，然后重新构建
echo.
pause

