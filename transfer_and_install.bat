@echo off
echo =============================================
echo    Android APK 传输和安装工具
echo =============================================
echo.

:: 设置APK文件名
set APK_FILE=app-release-jiagu-signed.apk
set PACKAGE_NAME=com.xy.rootmanager

echo [1/5] 检查ADB连接...
adb devices
if %ERRORLEVEL% neq 0 (
    echo ❌ ADB连接失败！请检查设备连接和ADB环境。
    pause
    exit /b 1
)

echo.
echo [2/5] 检查APK文件是否存在...
if not exist "%APK_FILE%" (
    echo ❌ 找不到APK文件: %APK_FILE%
    echo 请确保文件在当前目录中。
    pause
    exit /b 1
)
echo ✅ 找到APK文件: %APK_FILE%

echo.
echo [3/5] 卸载旧版本应用（如果存在）...
adb uninstall %PACKAGE_NAME% 2>nul
echo ✅ 旧版本卸载完成（如果存在）

echo.
echo [4/5] 传输APK到设备...
adb push "%APK_FILE%" "/sdcard/%APK_FILE%"
if %ERRORLEVEL% neq 0 (
    echo ❌ APK传输失败！
    pause
    exit /b 1
)
echo ✅ APK传输成功

echo.
echo [5/5] 安装APK...
adb install "%APK_FILE%"
if %ERRORLEVEL% neq 0 (
    echo ❌ APK安装失败！
    echo 可能的原因：
    echo - 设备存储空间不足
    echo - 签名冲突
    echo - USB调试未开启
    echo - 未知来源安装被禁用
    pause
    exit /b 1
)

echo.
echo ✅ 安装成功！
echo.
echo =============================================
echo    安装完成！您可以在设备上启动应用了
echo =============================================
echo.
echo 应用信息：
echo - 包名: %PACKAGE_NAME%
echo - 文件: %APK_FILE%
echo - 大小: 
dir "%APK_FILE%" | findstr /C:"%APK_FILE%"
echo.

echo [额外] 启动应用...
adb shell am start -n %PACKAGE_NAME%/.MainActivity
if %ERRORLEVEL% neq 0 (
    echo ⚠️  自动启动失败，请手动启动应用
) else (
    echo ✅ 应用已启动
)

echo.
echo 传输和安装完成！
pause
