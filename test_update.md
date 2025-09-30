# IYTH App 更新测试指南

## 测试准备

已生成的文件：
- `iyth-app-v1.2.0.apk` - 旧版本 (6.24 MB)
- `iyth-app-v1.3.0.apk` - 新版本 (6.24 MB)
- `release-info.json` - 更新信息配置文件

## 测试步骤

### 1. 安装旧版本
```bash
adb install iyth-app-v1.2.0.apk
```

### 2. 启动应用并测试更新功能
1. 打开IYTH应用
2. 点击主界面的"检查更新"按钮
3. 应用会从 `release-info.json` 读取更新信息
4. 如果检测到新版本(v1.3.0)，会显示更新对话框
5. 点击"立即更新"开始下载
6. 下载完成后自动启动安装

### 3. 验证更新结果
- 检查应用版本是否更新到 v1.3.0
- 验证所有功能正常工作

## 更新机制说明

### 版本检测
- 当前版本: v1.2.0 (versionCode: 10200)
- 远程版本: v1.3.0 (versionCode: 10300)
- 检测逻辑: 比较 versionCode，远程版本大于本地版本时提示更新

### 下载源
应用会尝试以下下载源：
1. GitHub Release: `https://github.com/qinwy/iyth/releases/download/v1.3.0/iyth-app-v1.3.0.apk`
2. 备用镜像源（如果配置）

### 安装流程
1. 使用 Android DownloadManager 下载APK
2. 下载完成后使用 FileProvider 安全安装
3. 支持实时进度显示

## 注意事项

1. **网络连接**: 确保设备有网络连接
2. **存储空间**: 确保有足够空间下载APK（约8MB）
3. **安装权限**: 可能需要允许"未知来源"安装
4. **GitHub访问**: 如果GitHub访问受限，可配置镜像源

## 配置文件说明

`release-info.json` 包含：
- `tag_name`: 版本标签
- `name`: 版本名称  
- `body`: 更新说明
- `assets`: APK下载链接
- `published_at`: 发布时间

## 成功标准

✅ 应用能正确检测到新版本
✅ 下载进度正常显示
✅ 安装过程顺利完成
✅ 更新后版本号正确
✅ 所有功能正常工作
