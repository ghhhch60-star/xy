# 下载 Gradle Wrapper
$wrapperVersion = "8.2"
$wrapperDir = "gradle\wrapper"
$wrapperJarUrl = "https://services.gradle.org/distributions/gradle-$wrapperVersion-bin.zip"

# 创建目录
New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null

# 下载 gradle-wrapper.jar
$wrapperJarPath = "$wrapperDir\gradle-wrapper.jar"
if (!(Test-Path $wrapperJarPath)) {
    Write-Host "正在下载 gradle-wrapper.jar..."
    # 使用临时的 gradle 来生成 wrapper
    $tempDir = "$env:TEMP\gradle_temp"
    New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
    
    # 创建临时 build.gradle
    @"
task wrapper(type: Wrapper) {
    gradleVersion = '8.2'
}
"@ | Out-File -FilePath "$tempDir\build.gradle" -Encoding UTF8
    
    Write-Host "请手动下载 gradle-wrapper.jar 从："
    Write-Host "https://github.com/gradle/gradle/blob/master/gradle/wrapper/gradle-wrapper.jar"
    Write-Host "并放置到 gradle\wrapper\ 目录"
} else {
    Write-Host "gradle-wrapper.jar 已存在"
}

