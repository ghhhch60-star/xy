@echo off
chcp 65001 >nul
echo ==========================================
echo    XY Root Manager 版本发布工具
echo ==========================================
echo.

:: 检查当前版本信息
echo 📋 当前版本信息：
findstr "versionCode\|versionName" app\build.gradle
echo.

:: 询问是否要更新版本
set /p update_version="是否要更新版本号？(y/n): "
if /i "%update_version%"=="y" (
    echo.
    echo 请手动编辑 app\build.gradle 文件更新版本号
    echo 当前版本: versionCode 6, versionName "1.5"
    echo 建议新版本: versionCode 7, versionName "1.6"
    echo.
    pause
)

echo.
echo 🧹 1. 清理项目...
call gradlew clean
if %errorlevel% neq 0 (
    echo ❌ 清理失败！
    pause
    exit /b 1
)

echo.
echo 🔨 2. 构建Release APK...
call gradlew assembleRelease
if %errorlevel% neq 0 (
    echo ❌ 构建失败！
    pause
    exit /b 1
)

echo.
echo 📦 3. 检查生成的APK文件...
set apk_path=app\build\outputs\apk\release\app-release.apk
if exist "%apk_path%" (
    echo ✅ APK构建成功！
    echo.
    echo 📁 文件位置: %apk_path%
    
    :: 显示文件信息
    for %%A in ("%apk_path%") do (
        echo 📊 文件大小: %%~zA 字节 ^(约 %%~zA/1024/1024 MB^)
        echo 🕒 修改时间: %%~tA
    )
    
    echo.
    echo 🎯 APK文件已准备就绪！
    echo.
    echo 📋 下一步操作：
    echo 1. 测试APK安装和功能
    echo 2. 选择发布方式：
    echo    - GitHub Releases ^(推荐^)
    echo    - 自建服务器
    echo    - 应用商店
    echo 3. 上传APK文件
    echo 4. 更新UpdateManager.kt中的API URL
    echo 5. 通知用户有新版本可用
    echo.
    
    :: 询问是否要打开APK所在文件夹
    set /p open_folder="是否要打开APK所在文件夹？(y/n): "
    if /i "%open_folder%"=="y" (
        explorer "app\build\outputs\apk\release"
    )
    
    :: 询问是否要复制APK到桌面
    set /p copy_apk="是否要复制APK到桌面？(y/n): "
    if /i "%copy_apk%"=="y" (
        copy "%apk_path%" "%USERPROFILE%\Desktop\xy-root-manager-release.apk"
        if %errorlevel% equ 0 (
            echo ✅ APK已复制到桌面: xy-root-manager-release.apk
        )
    )
    
) else (
    echo ❌ APK构建失败！未找到输出文件
    echo 请检查构建日志中的错误信息
)

echo.
echo ==========================================
echo           发布流程完成
echo ==========================================
pause
