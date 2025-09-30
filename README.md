# XY - Android Root权限管理与分区备份工具

## 功能特性

- ✅ Root权限申请和检测
- ✅ 系统分区扫描和显示
- ✅ 单个分区备份
- ✅ 批量分区备份
- ✅ 备份进度显示
- ✅ 中文界面
- ✅ 支持Android 15
- ✅ Material Design 3 界面设计

## 系统要求

- Android 7.0 (API 24) 或更高版本
- 设备需要已获取Root权限
- 建议至少有10GB可用存储空间用于备份

## 构建说明

### 环境要求

1. Android Studio (推荐最新版本)
2. Android SDK 35 (Android 15)
3. JDK 8 或更高版本

### 构建步骤

1. **设置Android SDK路径**
   ```
   复制 local.properties.example 为 local.properties
   修改 sdk.dir 为你的Android SDK路径
   ```

2. **设置应用图标**
   - 按照 ICON_SETUP.md 中的说明设置应用图标
   - 使用 log/ye.png 作为源图标文件

3. **在Android Studio中打开项目**
   - 打开Android Studio
   - 选择 File > Open
   - 选择项目根目录

4. **同步项目**
   - 点击 "Sync Project with Gradle Files"
   - 等待依赖下载完成

5. **构建APK**
   - 选择 Build > Build Bundle(s) / APK(s) > Build APK(s)
   - APK文件将生成在 `app/build/outputs/apk/debug/` 目录

6. **生成签名的Release APK**
   - 选择 Build > Generate Signed Bundle / APK
   - 选择 APK
   - 创建或选择密钥库
   - 选择 release 构建类型
   - 完成后APK将在 `app/build/outputs/apk/release/` 目录

## 使用说明

### 首次使用

1. 安装APK到已Root的Android设备
2. 打开应用，授予存储权限
3. 点击"请求权限"按钮获取Root权限
4. 在弹出的Root授权对话框中选择"允许"

### 备份分区

1. Root权限获取成功后，应用会自动扫描系统分区
2. 点击"刷新分区"可以重新扫描
3. 单个备份：点击分区列表中的"备份"按钮
4. 批量备份：点击"备份全部"按钮

### 备份位置

备份文件保存在：
```
/sdcard/XY_Backups/[日期]/
```

每个备份包含：
- `.img` - 分区镜像文件
- `.md5` - 校验和文件
- `.info` - 备份信息文件

## 支持的分区

应用会自动识别并备份以下类型的系统分区（基于images文件夹模板）：
- 引导分区 (abl, xbl, uefi等)
- 系统分区 (dsp, bluetooth等)
- 安全分区 (keymaster, tz等)
- 配置分区 (devcfg, metadata等)
- 其他重要分区

## 注意事项

⚠️ **警告**：
- 本应用需要Root权限，使用前请确保了解相关风险
- 备份和恢复系统分区可能导致设备变砖
- 建议在操作前先备份重要数据
- 仅在了解操作含义的情况下使用本应用

## 技术栈

- Kotlin
- Android Jetpack (ViewModel, LiveData, ViewBinding)
- Material Design Components
- Coroutines
- libsu (Root权限库)

## 许可证

本项目仅供学习和研究使用。使用本软件的风险由用户自行承担。

## 问题反馈

如有问题，请在项目中创建Issue。

