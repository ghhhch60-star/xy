# GitHub Release 创建指南

## 手动创建 Release

由于GitHub CLI不可用，请按以下步骤手动创建Release：

### 1. 访问GitHub仓库
打开浏览器访问：https://github.com/ghhhch60-star/xy

### 2. 创建新Release
1. 点击右侧的 "Releases" 链接
2. 点击 "Create a new release" 按钮
3. 填写以下信息：

**Tag version:** `v1.3.0` (已推送)

**Release title:** `IYTH App v1.3.0 - 优化更新体验`

**Release description:**
```markdown
# IYTH App v1.3.0 - 优化更新体验

## 新功能
- 🎨 全新Material Design 3风格更新弹窗
- ✨ 优化版本检测逻辑，确保准确识别新版本
- 🔧 修复图标资源缺失问题
- 📱 改进用户界面体验

## 界面优化
- 使用MaterialAlertDialogBuilder替代传统AlertDialog
- 添加图标和表情符号增强视觉效果
- 优化弹窗布局和文案显示
- 统一应用整体设计风格

## 技术改进
- 修复版本比较算法
- 添加缺失的矢量图标资源
- 优化构建流程
- 提升应用稳定性

## 系统要求
- Android 7.0 (API 24) 或更高版本
- 网络连接（用于检查更新）
- 约8MB存储空间

## 技术栈
- Kotlin + Material Design 3
- OkHttp 4.12.0 + Gson 2.10.1
- Android DownloadManager
```

### 3. 上传APK文件
在 "Attach binaries" 区域，上传以下文件：
- `iyth-app-v1.3.0.apk` (主要APK文件)

### 4. 发布Release
点击 "Publish release" 按钮完成发布。

## 文件位置
- APK文件位于项目根目录：`iyth-app-v1.3.0.apk`
- 文件大小：约6.5MB
- 版本代码：10300
- 版本名称：1.3.0

## 测试更新功能
1. 安装旧版本APK (如果有的话)
2. 在应用中点击"检查更新"按钮
3. 应该能看到新的Material Design 3风格弹窗
4. 测试下载和安装流程

## 注意事项
- 确保APK文件完整上传
- 检查Release描述格式正确
- 验证下载链接可访问