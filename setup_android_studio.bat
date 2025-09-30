@echo off
chcp 65001 >nul
echo ====================================
echo Android Studio 环境配置脚本
echo ====================================
echo.

:: 创建 Gradle 目录
echo [1/4] 创建 Gradle 配置目录...
if not exist "%USERPROFILE%\.gradle" (
    mkdir "%USERPROFILE%\.gradle"
    echo     ✓ 创建 .gradle 目录
) else (
    echo     ✓ .gradle 目录已存在
)

:: 配置 init.gradle
echo.
echo [2/4] 配置 Gradle 初始化脚本...
(
echo allprojects {
echo     repositories {
echo         google()
echo         mavenCentral()
echo         maven { url 'https://jitpack.io' }
echo         // 国内镜像（如果 JitPack 访问慢可以启用）
echo         maven { url 'https://maven.aliyun.com/repository/public' }
echo         maven { url 'https://maven.aliyun.com/repository/google' }
echo         maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
echo     }
echo }
echo.
echo // 设置下载超时时间
echo System.setProperty("org.gradle.internal.http.connectionTimeout", "60000"^)
echo System.setProperty("org.gradle.internal.http.socketTimeout", "60000"^)
) > "%USERPROFILE%\.gradle\init.gradle"
echo     ✓ init.gradle 配置完成

:: 配置 gradle.properties
echo.
echo [3/4] 配置 Gradle 属性...
(
echo # Gradle 性能优化配置
echo org.gradle.daemon=true
echo org.gradle.parallel=true
echo org.gradle.configureondemand=true
echo org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
echo.
echo # 开启 Gradle 缓存
echo org.gradle.caching=true
echo.
echo # 网络超时设置
echo systemProp.org.gradle.internal.http.connectionTimeout=60000
echo systemProp.org.gradle.internal.http.socketTimeout=60000
echo.
echo # 如果需要使用代理，取消下面的注释并修改
echo # systemProp.http.proxyHost=127.0.0.1
echo # systemProp.http.proxyPort=7890
echo # systemProp.https.proxyHost=127.0.0.1
echo # systemProp.https.proxyPort=7890
echo # systemProp.http.nonProxyHosts=localhost^|127.0.0.1
echo # systemProp.https.nonProxyHosts=localhost^|127.0.0.1
) > "%USERPROFILE%\.gradle\gradle.properties"
echo     ✓ gradle.properties 配置完成

:: 创建 Android Studio 配置建议文件
echo.
echo [4/4] 生成 Android Studio 配置建议...
(
echo Android Studio 推荐配置：
echo ========================
echo.
echo 1. 内存设置（Help -^> Change Memory Settings）：
echo    - Xmx: 4096 MB
echo.
echo 2. Gradle 设置（File -^> Settings -^> Build, Execution, Deployment -^> Build Tools -^> Gradle）：
echo    - Gradle JDK: 选择 JDK 11 或更高版本
echo    - Use Gradle from: 'wrapper' (推荐)
echo.
echo 3. 编译器设置（File -^> Settings -^> Build, Execution, Deployment -^> Compiler）：
echo    - 勾选 "Compile independent modules in parallel"
echo    - 勾选 "Configure on demand"
echo.
echo 4. 网络设置（如果需要代理）：
echo    File -^> Settings -^> Appearance ^& Behavior -^> System Settings -^> HTTP Proxy
echo.
echo 5. SDK 设置：
echo    File -^> Project Structure -^> SDK Location
echo    确保 Android SDK 路径正确
) > android_studio_config_guide.txt

echo.
echo ====================================
echo 配置完成！
echo ====================================
echo.
echo 接下来请：
echo 1. 启动 Android Studio
echo 2. 打开项目
echo 3. 点击 File -^> Invalidate Caches and Restart
echo 4. 重启后等待 Gradle 同步完成
echo.
echo 如果仍有问题，请查看 android_studio_config_guide.txt
echo.
pause

