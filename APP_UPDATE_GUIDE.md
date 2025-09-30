# IYTH App 应用内更新功能使用指南

## 🎉 功能介绍

IYTH App 现在支持应用内自动更新功能！用户可以在应用内直接检查、下载和安装最新版本，无需手动访问GitHub。

## 📱 用户使用流程

### 1. 检查更新
- 打开IYTH App主界面
- 点击界面中的「检查更新」按钮
- 应用会自动连接GitHub检查最新版本

### 2. 发现新版本
- 如果有新版本可用，会弹出更新对话框
- 对话框显示：
  - 新版本号和大小
  - 发布时间
  - 更新说明
- 用户可选择「立即更新」或「稍后提醒」

### 3. 下载更新
- 点击「立即更新」开始下载
- 显示实时下载进度
- 包含下载速度和剩余时间
- 可以取消下载

### 4. 安装更新
- 下载完成后自动启动Android安装程序
- 按照系统提示完成安装
- 安装完成后重启应用即可使用新版本

## 🔧 技术实现

### 核心组件

1. **UpdateManager.kt** - 更新管理器
   - 版本检查和比较
   - GitHub API集成
   - 下载管理
   - APK安装处理

2. **UpdateDialog.kt** - 更新对话框
   - 更新信息展示
   - 下载进度显示
   - 用户交互处理

3. **UpdateInfo.kt** - 更新信息数据类
   - GitHub Release数据解析
   - 版本信息封装

### 权限配置

应用已配置必要权限：
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
```

### FileProvider配置

为了在Android 7.0+上安全安装APK：
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## 🌐 网络配置

### GitHub API集成
- 使用GitHub Releases API获取最新版本信息
- API端点：`https://api.github.com/repos/ghhhch60-star/xy/releases/latest`
- 支持版本比较和APK文件检测

### 下载管理
- 使用Android DownloadManager进行文件下载
- 支持断点续传和进度监控
- 自动处理网络错误和重试

## 📋 系统要求

- **Android版本**：Android 5.0 (API 21) 或更高
- **网络连接**：需要Internet连接检查和下载更新
- **存储空间**：约8-10MB用于下载APK文件
- **权限**：需要「安装未知应用」权限

## 🚀 版本发布流程

### 开发者发布新版本：

1. **更新版本号**
   ```gradle
   // app/build.gradle
   versionCode 8
   versionName "1.7"
   ```

2. **构建APK**
   ```bash
   ./gradlew assembleRelease
   ```

3. **创建GitHub Release**
   ```bash
   gh release create v1.7.0 --title "IYTH App v1.7.0" --notes "更新说明" app/build/outputs/apk/release/app-release-unsigned.apk
   ```

4. **用户自动收到更新通知**
   - 用户打开应用时可检查更新
   - 或手动点击「检查更新」按钮

## 🛠️ 故障排除

### 常见问题

1. **无法检查更新**
   - 检查网络连接
   - 确认GitHub API可访问
   - 查看应用日志错误信息

2. **下载失败**
   - 检查存储空间是否充足
   - 确认网络连接稳定
   - 重试下载

3. **安装失败**
   - 确认已开启「安装未知应用」权限
   - 检查APK文件是否完整
   - 重新下载安装包

4. **版本检查异常**
   - 确认GitHub仓库配置正确
   - 检查Release是否包含APK文件
   - 验证版本号格式

### 调试信息

应用会在日志中输出详细的更新过程信息：
- 版本检查结果
- 下载进度
- 安装状态
- 错误信息

## 📈 未来改进

### 计划功能
- [ ] 增量更新支持
- [ ] 更新提醒设置
- [ ] 后台自动检查
- [ ] 多语言支持
- [ ] 更新统计分析

### 性能优化
- [ ] 缓存机制优化
- [ ] 下载速度提升
- [ ] 电池使用优化
- [ ] 数据使用控制

## 📞 技术支持

如有问题或建议，请联系：
- **QQ**: 3302719731
- **GitHub**: https://github.com/ghhhch60-star/xy
- **Issues**: https://github.com/ghhhch60-star/xy/issues

---

*更新时间：2025-09-30*  
*文档版本：v1.0*
