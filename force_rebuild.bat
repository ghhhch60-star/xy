@echo off
chcp 65001 >nul
echo ====================================
echo 强制重新构建 APK
echo ====================================
echo.

cd /d "%~dp0"

echo [1/3] 删除旧的 APK 文件...
del /f /q app\build\outputs\apk\debug\*.apk 2>nul
del /f /q app\build\outputs\apk\debug\*.json 2>nul
echo ✓ 旧文件已删除

echo.
echo [2/3] 创建时间戳文件...
echo Build Time: %date% %time% > app\src\main\assets\build_timestamp.txt
echo Version: 2.0 - Green UI Update >> app\src\main\assets\build_timestamp.txt
echo ✓ 时间戳已更新

echo.
echo [3/3] 现在请在 Android Studio 中执行：
echo.
echo 1. Build → Clean Project
echo 2. Build → Rebuild Project
echo 3. Build → Build APK(s)
echo.
echo 或者在此窗口运行 Gradle 命令：
echo.
pause

echo.
echo 正在使用 Gradle 构建...
call gradle clean assembleDebug

echo.
echo ====================================
echo 构建完成！检查新 APK：
echo ====================================
dir app\build\outputs\apk\debug\*.apk

pause

