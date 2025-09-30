# 📦 XY Root Manager 版本发布指南

## 🎯 发布新版本的完整流程

### 📋 发布前准备清单

#### 1. 更新版本信息
在 `app/build.gradle` 中更新版本：
```gradle
defaultConfig {
    applicationId "com.xy.root.manager"
    minSdk 24
    targetSdk 35
    versionCode 7        // 从当前的6增加到7
    versionName "1.6"    // 从当前的1.5更新到1.6
    // ...
}
```

#### 2. 构建Release APK
```bash
# 清理并构建release版本
.\gradlew clean
.\gradlew assembleRelease
```

#### 3. 测试新版本
- 安装并测试所有功能
- 验证更新功能是否正常工作
- 检查新功能是否按预期运行

---

## 🚀 方式一：GitHub Releases（推荐）

### 步骤1：初始化Git仓库
```bash
# 初始化Git仓库
git init

# 添加所有文件
git add .

# 提交初始版本
git commit -m "Initial commit - XY Root Manager v1.5"
```

### 步骤2：创建GitHub仓库
1. 访问 [GitHub](https://github.com)
2. 点击 "New repository"
3. 填写仓库信息：
   - Repository name: `xy-root-manager`
   - Description: `XY Root Manager - Android Root File Manager`
   - 选择 Public 或 Private
4. 点击 "Create repository"

### 步骤3：连接远程仓库
```bash
# 添加远程仓库（替换为您的GitHub用户名）
git remote add origin https://github.com/YOUR_USERNAME/xy-root-manager.git

# 推送到GitHub
git branch -M main
git push -u origin main
```

### 步骤4：创建Release
1. 在GitHub仓库页面点击 "Releases"
2. 点击 "Create a new release"
3. 填写Release信息：
   - **Tag version**: `v1.6`
   - **Release title**: `XY Root Manager v1.6`
   - **Description**: 
     ```markdown
     ## 🎉 新功能
     - ✅ 添加应用内更新功能
     - ✅ 一键检查和下载更新
     - ✅ 智能版本比较
     - ✅ 安全APK安装
     
     ## 🔧 改进
     - 优化用户界面
     - 提升稳定性
     
     ## 📱 安装说明
     1. 下载下方的APK文件
     2. 允许安装未知来源应用
     3. 安装并享受新功能
     ```

4. 上传APK文件：
   - 点击 "Attach binaries"
   - 上传 `app/build/outputs/apk/release/app-release.apk`
   - 重命名为 `xy-root-manager-v1.6.apk`

5. 点击 "Publish release"

### 步骤5：更新API配置
在 `UpdateManager.kt` 中更新API URL：
```kotlin
private const val UPDATE_CHECK_URL = "https://api.github.com/repos/YOUR_USERNAME/xy-root-manager/releases/latest"
```

---

## 🌐 方式二：自建服务器

### 步骤1：准备服务器文件
创建一个简单的API接口，返回JSON格式：
```json
{
  "tag_name": "v1.6",
  "name": "XY Root Manager v1.6",
  "body": "更新日志内容...",
  "assets": [
    {
      "name": "xy-root-manager-v1.6.apk",
      "browser_download_url": "https://your-server.com/downloads/xy-root-manager-v1.6.apk",
      "size": 8306311
    }
  ]
}
```

### 步骤2：上传文件
1. 将APK文件上传到您的服务器
2. 创建或更新API接口
3. 确保文件可以通过HTTP/HTTPS访问

### 步骤3：更新应用配置
```kotlin
private const val UPDATE_CHECK_URL = "https://your-server.com/api/check-update"
```

---

## 📱 方式三：应用商店发布

### Google Play Store
1. 注册Google Play开发者账号（$25一次性费用）
2. 创建应用列表
3. 上传APK或AAB文件
4. 填写应用信息和截图
5. 提交审核

### 其他应用商店
- **华为应用市场**
- **小米应用商店**  
- **OPPO软件商店**
- **vivo应用商店**
- **应用宝（腾讯）**
- **豌豆荚**
- **酷安**

---

## 🛠️ 自动化发布脚本

### 创建发布脚本 `release.bat`
```batch
@echo off
echo 开始构建新版本...

echo 1. 清理项目
call gradlew clean

echo 2. 构建Release APK
call gradlew assembleRelease

echo 3. 检查APK文件
if exist "app\build\outputs\apk\release\app-release.apk" (
    echo ✅ APK构建成功！
    echo 文件位置: app\build\outputs\apk\release\app-release.apk
    
    echo 4. 显示APK信息
    dir "app\build\outputs\apk\release\app-release.apk"
    
    echo.
    echo 📋 下一步操作：
    echo 1. 测试APK文件
    echo 2. 上传到GitHub Releases或您的服务器
    echo 3. 更新API配置
    echo 4. 通知用户更新
) else (
    echo ❌ APK构建失败！
    pause
    exit /b 1
)

pause
```

---

## 📋 版本发布检查清单

### 发布前检查 ✅
- [ ] 更新 `versionCode` 和 `versionName`
- [ ] 构建并测试Release APK
- [ ] 验证所有新功能正常工作
- [ ] 检查应用权限和签名
- [ ] 准备更新日志

### 发布过程 ✅
- [ ] 上传APK到发布平台
- [ ] 创建Release标签和描述
- [ ] 更新API配置中的URL
- [ ] 测试更新检查功能

### 发布后检查 ✅
- [ ] 验证下载链接可访问
- [ ] 测试应用内更新流程
- [ ] 监控用户反馈
- [ ] 记录发布日志

---

## 🎯 推荐发布流程

### 对于个人开发者：
1. **GitHub Releases** - 免费、简单、可靠
2. 使用Git进行版本控制
3. 自动化构建和发布

### 对于商业应用：
1. **Google Play Store** - 最大用户覆盖
2. 多个国内应用商店 - 覆盖中国用户
3. 自建CDN - 更好的下载体验

---

## 🔧 当前项目状态

- **当前版本**: v1.5 (versionCode: 6)
- **建议下个版本**: v1.6 (versionCode: 7)
- **APK大小**: ~8.3MB
- **最低Android版本**: 7.0 (API 24)

---

## 📞 技术支持

如需要帮助设置发布流程，请提供：
1. 您希望使用的发布方式
2. 是否有GitHub账号
3. 是否有自己的服务器
4. 目标用户群体（国内/国外）

选择最适合您的发布方式，我可以提供详细的配置帮助！🚀
