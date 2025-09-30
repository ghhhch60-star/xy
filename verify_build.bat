@echo off
chcp 65001 >nul
echo ====================================
echo 验证构建配置
echo ====================================
echo.

cd /d "%~dp0"

echo [检查] 依赖配置...
findstr "swiperefreshlayout" app\build.gradle >nul
if %errorlevel% equ 0 (
    echo ✓ SwipeRefreshLayout 依赖已添加
) else (
    echo ✗ SwipeRefreshLayout 依赖缺失
)

echo.
echo [检查] 代码文件...
if exist "app\src\main\java\com\xy\root\manager\XYApplication.kt" (
    echo ✓ XYApplication.kt 存在
) else (
    echo ✗ XYApplication.kt 缺失
)

if exist "app\src\main\java\com\xy\root\manager\utils\RootManager.kt" (
    echo ✓ RootManager.kt 存在
) else (
    echo ✗ RootManager.kt 缺失
)

echo.
echo ====================================
echo 所有检查完成！
echo ====================================
echo.
echo 现在请在 Android Studio 中：
echo 1. File → Sync Project with Gradle Files
echo 2. Build → Clean Project  
echo 3. Build → Rebuild Project
echo 4. Build → Build APK(s)
echo.
pause

