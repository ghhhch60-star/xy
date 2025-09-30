# 🚀 GitHub Releases 发布指南

## 📋 快速开始

### 第一次设置（只需做一次）

#### 1. 初始化Git仓库
```bash
# 在项目根目录执行
git init
git add .
git commit -m "Initial commit - XY Root Manager v1.5"
```

#### 2. 创建GitHub仓库
1. 访问 https://github.com
2. 点击右上角的 "+" → "New repository"
3. 填写信息：
   - **Repository name**: `xy-root-manager`
   - **Description**: `XY Root Manager - Android Root File Manager with Auto-Update`
   - 选择 **Public**（推荐）或 Private
   - ✅ 勾选 "Add a README file"
4. 点击 "Create repository"

#### 3. 连接本地仓库到GitHub
```bash
# 替换 YOUR_USERNAME 为您的GitHub用户名
git remote add origin https://github.com/YOUR_USERNAME/xy-root-manager.git
git branch -M main
git push -u origin main
```

---

## 🎯 发布新版本流程

### 步骤1: 准备新版本

#### 更新版本号
编辑 `app/build.gradle`：
```gradle
defaultConfig {
    versionCode 7        // 增加版本代码
    versionName "1.6"    // 更新版本名称
    // ...
}
```

#### 构建Release APK
```bash
# 运行发布脚本
.\release.bat

# 或手动执行
.\gradlew clean
.\gradlew assembleRelease
```

### 步骤2: 提交代码更改
```bash
# 添加更改
git add .
git commit -m "Release v1.6: Add auto-update feature"
git push origin main
```

### 步骤3: 创建GitHub Release

#### 方法A: 通过GitHub网页界面
1. 访问您的GitHub仓库
2. 点击 "Releases" 标签
3. 点击 "Create a new release"
4. 填写Release信息：

**Tag version**: `v1.6`
**Release title**: `XY Root Manager v1.6 - 应用内更新功能`
**Description**:
```markdown
## 🎉 新功能
- ✅ **应用内更新**: 一键检查和下载最新版本
- ✅ **智能版本比较**: 自动检测是否有新版本可用
- ✅ **安全下载**: 使用系统下载管理器，支持断点续传
- ✅ **便捷安装**: 下载完成后自动引导安装

## 🔧 技术改进
- 添加网络权限和状态检测
- 集成OkHttp网络库
- 优化用户界面和交互体验
- 完善错误处理和异常捕获

## 📱 使用说明
1. 点击主界面的"检查更新"按钮
2. 如有新版本，会显示更新详情
3. 确认后自动下载并引导安装
4. 支持GitHub Releases和自定义API

## 🛠️ 技术规格
- **最低Android版本**: 7.0 (API 24)
- **目标Android版本**: 14 (API 35)
- **APK大小**: ~8.3MB
- **架构支持**: Universal APK

## 📥 下载安装
1. 下载下方的APK文件
2. 在手机设置中允许"安装未知来源应用"
3. 点击APK文件进行安装
4. 首次使用需要授予Root权限

## 🔄 从旧版本升级
- 支持直接覆盖安装，无需卸载旧版本
- 应用数据和设置会自动保留
- 建议使用应用内更新功能进行升级

---
**完整更新日志**: [查看所有更改](https://github.com/YOUR_USERNAME/xy-root-manager/compare/v1.5...v1.6)
```

5. **上传APK文件**:
   - 点击 "Attach binaries by dropping them here or selecting them"
   - 选择 `app/build/outputs/apk/release/app-release.apk`
   - 等待上传完成

6. 点击 "Publish release"

#### 方法B: 使用GitHub CLI（可选）
```bash
# 安装GitHub CLI (如果还没有)
# 下载: https://cli.github.com/

# 登录GitHub
gh auth login

# 创建release
gh release create v1.6 \
  --title "XY Root Manager v1.6 - 应用内更新功能" \
  --notes-file release_notes.md \
  app/build/outputs/apk/release/app-release.apk
```

### 步骤4: 更新应用配置

编辑 `app/src/main/java/com/xy/root/manager/utils/UpdateManager.kt`：
```kotlin
// 替换为您的GitHub仓库API URL
private const val UPDATE_CHECK_URL = "https://api.github.com/repos/YOUR_USERNAME/xy-root-manager/releases/latest"
```

### 步骤5: 测试更新功能

1. 安装旧版本APK到测试设备
2. 点击"检查更新"按钮
3. 验证能正确检测到新版本
4. 测试下载和安装流程

---

## 🔧 自动化脚本

### 创建 `github_release.bat`
```batch
@echo off
echo 开始GitHub发布流程...

:: 检查Git状态
git status
if %errorlevel% neq 0 (
    echo 请先初始化Git仓库
    pause
    exit /b 1
)

:: 构建APK
call .\release.bat

:: 提交更改
set /p commit_msg="输入提交信息: "
git add .
git commit -m "%commit_msg%"
git push origin main

echo.
echo ✅ 代码已推送到GitHub
echo 📋 下一步: 访问GitHub创建Release
echo 🌐 GitHub仓库: https://github.com/YOUR_USERNAME/xy-root-manager
echo.
pause
```

---

## 📊 发布后的统计

GitHub Releases提供以下统计信息：
- **下载次数**: 每个文件的下载统计
- **Release浏览量**: Release页面访问次数
- **Star数量**: 用户收藏数
- **Fork数量**: 代码分支数
- **Issue反馈**: 用户问题和建议

---

## 🎯 最佳实践

### 版本号规范
- 使用语义化版本号: `主版本.次版本.修订版本`
- 示例: `1.6.0`, `1.6.1`, `2.0.0`
- Git标签格式: `v1.6.0`

### Release描述规范
- 使用Markdown格式
- 包含新功能、改进、修复
- 提供安装和升级说明
- 添加技术规格信息

### 文件命名规范
- APK文件: `xy-root-manager-v1.6.apk`
- 包含版本号便于识别
- 避免特殊字符和空格

### 发布频率建议
- **主要功能更新**: 每月1-2次
- **Bug修复**: 根据需要随时发布
- **安全更新**: 立即发布

---

## 🚨 注意事项

1. **APK签名**: 确保使用相同的签名密钥
2. **权限变更**: 新权限需要在描述中说明
3. **兼容性**: 测试不同Android版本
4. **备份**: 保存好签名密钥文件
5. **隐私**: 不要在公开仓库中包含敏感信息

---

## 📞 需要帮助？

如果在设置过程中遇到问题，请提供：
1. 您的GitHub用户名
2. 遇到的具体错误信息
3. 操作系统和Git版本
4. 是否是第一次使用GitHub

我可以提供详细的解决方案！🚀
