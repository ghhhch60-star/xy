# Android Studio 配置脚本 (PowerShell 版本)
# 使用管理员权限运行效果更好

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "Android Studio 环境自动配置脚本" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# 检查 Gradle 目录
Write-Host "[步骤 1/5] 检查和创建 Gradle 目录..." -ForegroundColor Yellow
$gradleDir = "$env:USERPROFILE\.gradle"
if (!(Test-Path $gradleDir)) {
    New-Item -ItemType Directory -Path $gradleDir -Force | Out-Null
    Write-Host "    ✓ 创建 .gradle 目录" -ForegroundColor Green
} else {
    Write-Host "    ✓ .gradle 目录已存在" -ForegroundColor Green
}

# 配置 init.gradle
Write-Host ""
Write-Host "[步骤 2/5] 配置 Gradle 初始化脚本..." -ForegroundColor Yellow
$initGradle = @"
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
        // 阿里云镜像 - 提高国内访问速度
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        maven { url 'https://maven.aliyun.com/repository/jcenter' }
    }
}

// 设置下载超时时间
System.setProperty("org.gradle.internal.http.connectionTimeout", "120000")
System.setProperty("org.gradle.internal.http.socketTimeout", "120000")

// 全局排除某些传递依赖（如果需要）
allprojects {
    configurations.all {
        resolutionStrategy {
            // 强制使用特定版本（如果遇到版本冲突）
            // force 'com.android.support:support-v4:28.0.0'
        }
    }
}
"@
$initGradle | Out-File -FilePath "$gradleDir\init.gradle" -Encoding UTF8
Write-Host "    ✓ init.gradle 配置完成" -ForegroundColor Green

# 配置 gradle.properties
Write-Host ""
Write-Host "[步骤 3/5] 配置 Gradle 属性..." -ForegroundColor Yellow
$gradleProperties = @"
# Gradle 性能优化配置
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8

# 开启 Gradle 缓存
org.gradle.caching=true

# Android 相关优化
android.useAndroidX=true
android.enableJetifier=true
android.enableR8=true

# Kotlin 相关
kotlin.code.style=official
kotlin.incremental=true

# 网络超时设置
systemProp.org.gradle.internal.http.connectionTimeout=120000
systemProp.org.gradle.internal.http.socketTimeout=120000

# 代理设置（如果需要，请取消注释并修改）
# systemProp.http.proxyHost=127.0.0.1
# systemProp.http.proxyPort=7890
# systemProp.https.proxyHost=127.0.0.1
# systemProp.https.proxyPort=7890
# systemProp.http.nonProxyHosts=localhost|127.0.0.1|*.local
# systemProp.https.nonProxyHosts=localhost|127.0.0.1|*.local
"@
$gradleProperties | Out-File -FilePath "$gradleDir\gradle.properties" -Encoding UTF8
Write-Host "    ✓ gradle.properties 配置完成" -ForegroundColor Green

# 清理 Gradle 缓存（可选）
Write-Host ""
Write-Host "[步骤 4/5] 清理 Gradle 缓存..." -ForegroundColor Yellow
$clearCache = Read-Host "是否清理 Gradle 缓存？这可能解决一些依赖问题 (y/n)"
if ($clearCache -eq 'y') {
    $cacheDir = "$gradleDir\caches"
    if (Test-Path $cacheDir) {
        Remove-Item -Path "$cacheDir\modules-2" -Recurse -Force -ErrorAction SilentlyContinue
        Remove-Item -Path "$cacheDir\transforms-3" -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "    ✓ 缓存清理完成" -ForegroundColor Green
    }
} else {
    Write-Host "    → 跳过缓存清理" -ForegroundColor Gray
}

# 检查 Android SDK
Write-Host ""
Write-Host "[步骤 5/5] 检查 Android SDK..." -ForegroundColor Yellow
$androidHome = $env:ANDROID_HOME
if ($androidHome -and (Test-Path $androidHome)) {
    Write-Host "    ✓ Android SDK 路径: $androidHome" -ForegroundColor Green
} else {
    Write-Host "    ! Android SDK 未配置或路径无效" -ForegroundColor Red
    Write-Host "    请在 Android Studio 中配置 SDK 路径" -ForegroundColor Yellow
}

# 生成详细配置指南
$configGuide = @"
Android Studio 详细配置指南
==========================

1. 启动配置
   - 以管理员身份运行 Android Studio
   - 首次启动时选择 "Import Studio settings from..." 可以导入之前的设置

2. 内存优化 (Help -> Change Memory Settings)
   - IDE max heap size: 4096 MB
   - 如果电脑内存充足(16GB+)，可以设置为 6144 MB

3. Gradle 设置 (File -> Settings -> Build, Execution, Deployment -> Build Tools -> Gradle)
   - Gradle JDK: 选择 JDK 11 或更高版本
   - Use Gradle from: 'wrapper' (推荐)
   - Gradle user home: $gradleDir

4. 编译优化 (File -> Settings -> Build, Execution, Deployment -> Compiler)
   - ✓ Compile independent modules in parallel
   - ✓ Configure on demand
   - ✓ Use in-process build
   - Command-line Options: --offline (如果不需要实时更新依赖)

5. Android SDK 设置 (File -> Project Structure -> SDK Location)
   - Android SDK Location: 确保路径正确
   - Android NDK Location: 如果需要 NDK 开发
   - JDK Location: 建议使用 JDK 11+

6. 插件推荐
   - Rainbow Brackets: 彩虹括号
   - Key Promoter X: 快捷键提示
   - .ignore: Git 忽略文件支持
   - Material Theme UI: 美化界面

7. 代码模板 (File -> Settings -> Editor -> File and Code Templates)
   - 可以自定义文件头部注释模板

8. 版本控制 (File -> Settings -> Version Control -> Git)
   - Path to Git executable: 确保 Git 路径正确

9. 外观设置 (File -> Settings -> Appearance & Behavior -> Appearance)
   - Theme: 可选择 Darcula (暗色) 或 IntelliJ (亮色)
   - Use custom font: 可以设置界面字体

10. 编辑器设置 (File -> Settings -> Editor)
    - Font: 推荐 JetBrains Mono, Consolas, 或 Fira Code
    - Font Size: 14-16
    - Line height: 1.2
"@
$configGuide | Out-File -FilePath "android_studio_detailed_guide.txt" -Encoding UTF8

# 创建快速修复脚本
$quickFix = @"
@echo off
echo 快速修复 Gradle 同步问题...
echo.
cd /d %USERPROFILE%\Desktop\iyth
call gradlew clean --refresh-dependencies
call gradlew build --refresh-dependencies
echo.
echo 完成！如果仍有问题，请尝试：
echo 1. File -> Invalidate Caches and Restart
echo 2. Build -> Clean Project
echo 3. Build -> Rebuild Project
pause
"@
$quickFix | Out-File -FilePath "quick_fix_gradle.bat" -Encoding ASCII

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "配置完成！" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "接下来的步骤：" -ForegroundColor Yellow
Write-Host "1. 启动 Android Studio" -ForegroundColor White
Write-Host "2. 打开项目: $env:USERPROFILE\Desktop\iyth" -ForegroundColor White
Write-Host "3. 等待 Gradle 同步 (右下角会显示进度)" -ForegroundColor White
Write-Host "4. 如果出现 'Gradle sync failed':" -ForegroundColor White
Write-Host "   - 点击 'File -> Invalidate Caches and Restart'" -ForegroundColor Gray
Write-Host "   - 选择 'Invalidate and Restart'" -ForegroundColor Gray
Write-Host ""
Write-Host "其他文件：" -ForegroundColor Yellow
Write-Host "- android_studio_detailed_guide.txt (详细配置指南)" -ForegroundColor Gray
Write-Host "- quick_fix_gradle.bat (快速修复脚本)" -ForegroundColor Gray
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
Read-Host

