# 应用内更新功能使用指南

## 🎉 功能概述

成功为XY Root Manager添加了完整的应用内更新功能！用户现在可以通过联网检查并下载安装应用更新。

## ✨ 新增功能

### 1. 主要特性
- ✅ **一键检查更新** - 点击"检查更新"按钮即可检查最新版本
- ✅ **自动版本比较** - 智能比较当前版本与服务器版本
- ✅ **下载进度显示** - 实时显示下载进度和状态
- ✅ **安全APK安装** - 使用FileProvider安全安装APK
- ✅ **网络状态检测** - 自动处理网络错误
- ✅ **用户友好界面** - Material Design更新对话框

### 2. 技术实现
- **网络请求**: OkHttp + Gson
- **文件下载**: Android DownloadManager
- **版本管理**: 语义化版本号比较
- **安全安装**: FileProvider + REQUEST_INSTALL_PACKAGES权限

## 🔧 配置说明

### 1. 更新API配置

需要修改`UpdateManager.kt`中的API URL：

```kotlin
// 在 UpdateManager.kt 中，将此URL替换为您的实际API
private const val UPDATE_CHECK_URL = "https://api.github.com/repos/your-username/your-repo/releases/latest"
```

### 2. GitHub Releases API格式

如果使用GitHub Releases，API返回格式应为：
```json
{
  "tag_name": "v1.6",
  "name": "版本 1.6",
  "body": "更新日志内容...",
  "assets": [
    {
      "name": "app-release.apk",
      "browser_download_url": "https://github.com/user/repo/releases/download/v1.6/app-release.apk",
      "size": 8306311
    }
  ]
}
```

### 3. 自定义API格式

如果使用自定义API，请相应修改`UpdateManager.kt`中的JSON解析逻辑。

## 📱 用户使用流程

### 1. 检查更新
1. 用户点击主界面的"检查更新"按钮
2. 应用显示"正在检查更新..."状态
3. 后台请求服务器获取最新版本信息

### 2. 发现更新
1. 如果有新版本，显示更新对话框
2. 对话框包含：
   - 当前版本号
   - 新版本号
   - 更新包大小
   - 更新日志
3. 用户可选择"立即更新"或"稍后更新"

### 3. 下载安装
1. 用户确认更新后开始下载
2. 显示下载进度对话框
3. 下载完成后询问是否立即安装
4. 用户确认后启动系统安装程序

## 🛠️ 版本号规则

### 1. 版本号格式
- **versionName**: "1.5" (用户看到的版本)
- **versionCode**: 6 (内部版本号，用于比较)

### 2. 版本比较逻辑
```kotlin
// 版本号解析示例
// "1.5" -> versionCode = 10500
// "1.6" -> versionCode = 10600  
// "2.0" -> versionCode = 20000
```

### 3. 发布新版本步骤
1. 更新`app/build.gradle`中的`versionCode`和`versionName`
2. 构建release APK
3. 上传到GitHub Releases或您的服务器
4. 用户即可检查到更新

## 📂 文件结构

### 新增文件
```
app/src/main/java/com/xy/root/manager/
├── model/
│   └── UpdateInfo.kt          # 更新信息数据类
├── utils/
│   └── UpdateManager.kt       # 更新管理器
└── MainActivity.kt            # 添加了更新检查功能

app/src/main/res/
├── drawable/
│   └── ic_update.xml          # 更新图标
├── layout/
│   └── activity_main_anime.xml # 添加了更新按钮
├── values/
│   └── strings.xml            # 添加了更新相关字符串
└── xml/
    └── file_paths.xml         # FileProvider配置
```

### 权限添加
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

## 🚀 已完成的功能

- ✅ 网络权限配置
- ✅ 更新检查API
- ✅ 版本比较逻辑
- ✅ UI界面集成
- ✅ 下载管理器
- ✅ APK安装功能
- ✅ 错误处理
- ✅ 用户体验优化

## 🔍 测试建议

### 1. 本地测试
1. 修改API URL为可访问的测试服务器
2. 模拟不同版本号进行测试
3. 测试网络错误情况

### 2. 发布测试
1. 发布新版本到GitHub Releases
2. 使用旧版本应用测试更新流程
3. 验证下载和安装流程

## 🎯 后续优化建议

1. **增量更新**: 实现差分包更新减少下载大小
2. **后台更新**: 添加静默下载功能
3. **强制更新**: 支持重要版本的强制更新
4. **多语言**: 添加英文等多语言支持
5. **更新统计**: 记录更新成功率和用户行为

## 📞 技术支持

如需要修改或优化更新功能，请参考以下关键文件：
- `UpdateManager.kt` - 核心更新逻辑
- `MainActivity.kt` - UI集成
- `UpdateInfo.kt` - 数据模型

更新功能已完全集成到应用中，用户现在可以享受便捷的一键更新体验！🎉
