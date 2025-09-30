# 🚀 上传新版本到GitHub的详细步骤

## 📋 第一步：连接到您的GitHub仓库

### 1.1 初始化本地Git仓库
```bash
# 在项目根目录执行
git init
```

### 1.2 连接到您的GitHub仓库
```bash
# 替换为您的GitHub仓库URL
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git

# 如果您的仓库是SSH格式，使用：
# git remote add origin git@github.com:YOUR_USERNAME/YOUR_REPO_NAME.git
```

### 1.3 首次推送代码
```bash
# 添加所有文件
git add .

# 创建初始提交
git commit -m "Initial commit - XY Root Manager v1.5"

# 设置主分支
git branch -M main

# 推送到GitHub（如果仓库已有内容，可能需要先pull）
git pull origin main --allow-unrelated-histories
git push -u origin main
```

---

## 📈 第二步：更新版本号

### 2.1 编辑 `app/build.gradle`
找到以下行并更新：
```gradle
defaultConfig {
    applicationId "com.xy.root.manager"
    minSdk 24
    targetSdk 35
    versionCode 7        // 从 6 改为 7
    versionName "1.6"    // 从 "1.5" 改为 "1.6"
    // ...
}
```

### 2.2 更新 UpdateManager.kt 中的API URL
编辑 `app/src/main/java/com/xy/root/manager/utils/UpdateManager.kt`：
```kotlin
// 替换为您的GitHub仓库API URL
private const val UPDATE_CHECK_URL = "https://api.github.com/repos/YOUR_USERNAME/YOUR_REPO_NAME/releases/latest"
```

---

## 🔨 第三步：构建新版本APK

### 3.1 使用自动化脚本（推荐）
```bash
.\release.bat
```

### 3.2 手动构建
```bash
# 清理项目
.\gradlew clean

# 构建Release APK
.\gradlew assembleRelease
```

构建完成后，APK文件位于：`app\build\outputs\apk\release\app-release.apk`

---

## 📤 第四步：提交代码更改

### 4.1 查看更改
```bash
git status
git diff
```

### 4.2 提交更改
```bash
# 添加更改的文件
git add .

# 创建提交
git commit -m "Release v1.6: Add auto-update feature and UI improvements"

# 推送到GitHub
git push origin main
```

---

## 🎉 第五步：在GitHub创建Release

### 5.1 访问GitHub仓库
1. 打开浏览器，访问您的GitHub仓库
2. 点击 "Releases" 标签页
3. 点击 "Create a new release" 按钮

### 5.2 填写Release信息

**Tag version**: `v1.6`

**Release title**: `XY Root Manager v1.6 - 应用内更新功能`

**Description**（复制以下内容）:
```markdown
## 🎉 新功能
- ✅ **应用内更新**: 一键检查和下载最新版本
- ✅ **智能版本比较**: 自动检测是否有新版本可用
- ✅ **安全下载**: 使用系统下载管理器，支持断点续传
- ✅ **便捷安装**: 下载完成后自动引导安装

## 🔧 技术改进
- 添加网络权限和状态检测
- 集成OkHttp网络库进行网络请求
- 优化用户界面和交互体验
- 完善错误处理和异常捕获
- 支持GitHub Releases API

## 📱 使用说明
1. 点击主界面的"检查更新"按钮
2. 如有新版本，会显示更新详情
3. 确认后自动下载并引导安装
4. 支持后台下载，无需等待

## 🛠️ 技术规格
- **最低Android版本**: 7.0 (API 24)
- **目标Android版本**: 14 (API 35)
- **APK大小**: ~8.3MB
- **架构支持**: Universal APK
- **权限**: Root权限 + 网络权限

## 📥 安装方法
1. 下载下方的APK文件
2. 在手机设置中允许"安装未知来源应用"
3. 点击APK文件进行安装
4. 首次使用需要授予Root权限

## 🔄 从旧版本升级
- ✅ 支持直接覆盖安装，无需卸载
- ✅ 应用数据和设置自动保留
- ✅ 推荐使用应用内更新功能

## 🐛 已修复问题
- 修复文件管理器某些情况下的崩溃问题
- 优化Root权限检测逻辑
- 改进UI响应速度

---
**完整更新日志**: [查看所有更改](https://github.com/YOUR_USERNAME/YOUR_REPO_NAME/compare/v1.5...v1.6)
```

### 5.3 上传APK文件
1. 在Release编辑页面，找到 "Attach binaries" 区域
2. 点击或拖拽上传 `app\build\outputs\apk\release\app-release.apk`
3. 建议重命名为：`xy-root-manager-v1.6.apk`
4. 等待上传完成

### 5.4 发布Release
1. 确认所有信息正确
2. 点击 "Publish release" 按钮
3. Release将立即生效

---

## ✅ 第六步：验证发布

### 6.1 检查Release页面
- 访问：`https://github.com/YOUR_USERNAME/YOUR_REPO_NAME/releases`
- 确认v1.6版本已发布
- 测试APK下载链接

### 6.2 测试应用内更新
1. 安装旧版本APK到测试设备
2. 打开应用，点击"检查更新"
3. 验证能正确检测到v1.6版本
4. 测试下载和安装流程

---

## 🔄 后续版本发布流程

对于后续版本，流程更简单：

```bash
# 1. 更新版本号（在build.gradle中）
# 2. 构建APK
.\release.bat

# 3. 提交代码
git add .
git commit -m "Release v1.7: [描述新功能]"
git push origin main

# 4. 在GitHub创建新Release
# 5. 上传新APK
```

---

## 🚨 重要提醒

### APK签名
- ⚠️ 确保使用相同的签名密钥
- 💾 备份 `app-release-key.keystore` 文件
- 🔒 妥善保管密钥密码

### URL配置
记得将以下占位符替换为实际值：
- `YOUR_USERNAME` → 您的GitHub用户名
- `YOUR_REPO_NAME` → 您的仓库名称

### 文件大小限制
- GitHub Release单个文件限制：2GB
- 当前APK约8.3MB，完全没问题

---

## 📞 需要帮助？

如果在任何步骤遇到问题，请告诉我：
1. 您的GitHub仓库URL
2. 具体的错误信息
3. 当前执行到哪个步骤

我会提供具体的解决方案！🚀
