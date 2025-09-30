# XY 应用更新说明

## 🆕 新功能和修复

### 1. **内核级 Root 支持**
- ✅ 自动检测 Root 类型（KernelSU/SukiSU Ultra/Magisk）
- ✅ 为内核级 Root 添加专门提示："小Y提示"
- ✅ 支持手动授权流程
- ✅ 添加"打开Root管理器"按钮

### 2. **修复 Root 权限崩溃问题**
- ✅ 优化 Shell 初始化流程
- ✅ 使用更安全的命令执行方式
- ✅ 避免在应用启动时强制初始化 Shell

### 3. **全新高级 UI 设计**
- ✅ Material Design 3 风格
- ✅ 折叠式工具栏设计
- ✅ 卡片式布局with圆角和阴影
- ✅ 下拉刷新功能
- ✅ 空状态提示
- ✅ 动画效果（按钮旋转、卡片淡入）
- ✅ 线性进度条with圆角

## 📱 构建步骤

1. **清理项目**
   ```
   Build → Clean Project
   ```

2. **重建项目**
   ```
   Build → Rebuild Project
   ```

3. **生成 APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

## 🚀 新特性详解

### 内核级 Root 检测
应用现在会自动识别您使用的 Root 方案：
- **KernelSU/SukiSU Ultra**: 显示"小Y提示"，引导手动授权
- **Magisk**: 使用传统的自动弹窗授权
- **其他Root**: 通用提示

### UI 改进
- 顶部使用了可折叠的标题栏
- 卡片使用了更大的圆角（12dp）
- 添加了阴影效果增强层次感
- 按钮使用了 Material3 的 TonalButton 和 ElevatedButton 样式
- 进度条使用了圆角设计

### 安全性改进
- 使用 RootManager 统一管理 Root 操作
- 避免了直接的 Shell 初始化
- 添加了异常处理防止崩溃

## ⚠️ 注意事项

1. 首次安装后如果遇到崩溃，请：
   - 确保已在 Root 管理器中授予权限
   - 重新打开应用

2. 对于内核级 Root 用户：
   - 看到"小Y提示"后，请手动打开 Root 管理器
   - 在 Root 管理器中找到 XY 应用并授予权限
   - 返回应用点击"检查权限"

3. 备份位置：`/sdcard/XY_Backups/`

## 📋 测试建议

1. 测试不同 Root 方案的识别
2. 测试授权后是否正常工作
3. 测试备份功能是否正常
4. 测试 UI 动画是否流畅

