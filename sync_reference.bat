@echo off
echo ========================================
echo XY Root Manager - 参考文件夹同步工具
echo ========================================
echo.

:: 检查源文件夹是否存在
if not exist "C:\Users\Qinwy\Desktop\images" (
    echo ❌ 错误: 参考文件夹不存在
    echo 请确保 C:\Users\Qinwy\Desktop\images 文件夹存在
    pause
    exit /b 1
)

:: 检查ADB连接
echo 🔍 检查ADB连接状态...
adb devices
if %errorlevel% neq 0 (
    echo ❌ 错误: ADB命令失败
    echo 请确保已安装ADB并添加到PATH环境变量
    pause
    exit /b 1
)

echo.
echo 📋 正在同步参考文件夹信息到Android设备...
echo 参考文件夹: C:\Users\Qinwy\Desktop\images
echo.

:: 创建设备上的参考信息文件夹
echo 📁 创建设备参考信息文件夹...
adb shell "mkdir -p /sdcard/pc_reference"

:: 获取PC上的.img文件列表
echo 📊 分析参考文件夹中的.img文件...
echo.

:: 生成参考文件列表
set temp_file=%TEMP%\reference_list.txt
if exist "%temp_file%" del "%temp_file%"

for %%f in ("C:\Users\Qinwy\Desktop\images\*.img") do (
    echo %%~nxf >> "%temp_file%"
)

:: 检查是否有文件
for /f %%i in ('type "%temp_file%" 2^>nul ^| find /c /v ""') do set file_count=%%i

if %file_count%==0 (
    echo ❌ 错误: 未找到.img文件
    echo 请确保 C:\Users\Qinwy\Desktop\images 文件夹中包含.img文件
    pause
    exit /b 1
)

echo 📊 找到 %file_count% 个.img参考文件:
type "%temp_file%"
echo.

:: 将参考文件列表传输到设备
echo 📤 传输参考文件列表到设备...
adb push "%temp_file%" /sdcard/pc_reference/reference_list.txt

if %errorlevel% neq 0 (
    echo ❌ 传输失败
    pause
    exit /b 1
)

:: 创建参考信息文件
echo 📝 创建参考信息文件...
set info_file=%TEMP%\reference_info.txt
echo PC参考文件夹: C:\Users\Qinwy\Desktop\images > "%info_file%"
echo 同步时间: %date% %time% >> "%info_file%"
echo 参考文件数量: %file_count% >> "%info_file%"
echo. >> "%info_file%"
echo 参考文件列表: >> "%info_file%"
type "%temp_file%" >> "%info_file%"

adb push "%info_file%" /sdcard/pc_reference/info.txt

:: 清理临时文件
del "%temp_file%" "%info_file%"

echo.
echo ✅ 参考文件夹信息同步完成！
echo.
echo 📋 同步摘要:
echo   • PC参考文件夹: C:\Users\Qinwy\Desktop\images
echo   • 设备参考路径: /sdcard/pc_reference/
echo   • 参考文件数量: %file_count%
echo.
echo 💡 提示:
echo   1. 现在可以打开XY Root Manager应用
echo   2. 点击"刷新"按钮扫描分区
echo   3. 应用将根据参考文件夹扫描设备上对应的分区
echo   4. 只有参考文件夹中存在的分区类型才会被扫描和显示
echo.

pause
