@echo off
chcp 65001 >nul
echo ====================================
echo 清理和重建项目
echo ====================================
echo.

cd /d "%~dp0"

echo 步骤 1: 清理构建缓存...
if exist .gradle rd /s /q .gradle
if exist app\build rd /s /q app\build
if exist build rd /s /q build
echo ✓ 清理完成

echo.
echo 步骤 2: 清理 Android Studio 缓存...
if exist .idea\caches rd /s /q .idea\caches
echo ✓ 缓存清理完成

echo.
echo ====================================
echo 接下来在 Android Studio 中：
echo ====================================
echo.
echo 1. File → Sync Project with Gradle Files
echo    等待同步完成（查看底部状态栏）
echo.
echo 2. Build → Clean Project
echo.
echo 3. Build → Rebuild Project
echo.
echo 4. 如果仍有 BuildConfig 错误：
echo    - File → Invalidate Caches... → Invalidate and Restart
echo    - 重启后再次同步项目
echo.
echo 5. Build → Build Bundle(s) / APK(s) → Build APK(s)
echo.
echo ====================================
echo 提示：BuildConfig 文件位置
echo ====================================
echo BuildConfig.java 应该自动生成在：
echo app\build\generated\source\buildConfig\debug\com\xy\root\manager\
echo.
echo 如果文件未生成，说明构建过程未完成。
echo.
pause

