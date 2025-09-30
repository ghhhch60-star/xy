@echo off
chcp 65001 >nul
echo ====================================
echo 构建 APK - XY 应用
echo ====================================
echo.

cd /d "%~dp0"

echo 检查 Android SDK...
if not defined ANDROID_HOME (
    echo 错误：ANDROID_HOME 环境变量未设置！
    echo 请设置 ANDROID_HOME 指向您的 Android SDK 路径
    pause
    exit /b 1
)

echo Android SDK: %ANDROID_HOME%
echo.

echo 清理项目...
if exist .gradle rd /s /q .gradle
if exist app\build rd /s /q app\build

echo.
echo ====================================
echo 请在 Android Studio 中执行以下操作：
echo ====================================
echo.
echo 1. 打开 Android Studio
echo 2. 选择 "Open" 并打开此项目文件夹
echo 3. 等待 Gradle 同步完成（右下角会有进度条）
echo 4. 如果出现 "Gradle sync failed"：
echo    - 点击 "Try Again" 或 "Sync Now"
echo    - 或者选择 File -^> Sync Project with Gradle Files
echo 5. 同步成功后：
echo    - Build -^> Build Bundle(s) / APK(s) -^> Build APK(s)
echo 6. APK 文件将生成在：
echo    app\build\outputs\apk\debug\app-debug.apk
echo.
echo ====================================
echo 项目已配置完成！
echo ====================================
echo.
echo 提示：如果仍有依赖下载问题，请检查：
echo 1. 网络连接是否正常
echo 2. 是否需要配置代理
echo 3. settings.gradle 中是否包含了所有必要的仓库
echo.
pause

