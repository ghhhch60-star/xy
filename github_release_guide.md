# GitHub Release 创建指南

## 准备工作

### 1. 上传APK到GitHub
1. 进入 GitHub 仓库: https://github.com/qinwy/iyth
2. 点击 "Releases" 标签
3. 点击 "Create a new release"

### 2. 填写Release信息

**Tag version**: `v1.3.0`
**Release title**: `IYTH App v1.3.0`

**Description**:
```markdown
# IYTH App v1.3.0 - App Update Feature

## 新功能
- 应用内自动更新检查
- 本地与远程版本比较
- 实时下载进度显示  
- 下载完成后智能安装

## 使用方法
1. 点击主界面"检查更新"按钮
2. 如有新版本会显示更新对话框
3. 点击"立即更新"开始下载
4. 下载完成后自动开始安装

## 技术改进
- 集成 OkHttp 网络请求
- Android DownloadManager 下载管理
- FileProvider 安全安装支持
- 实时进度监控和状态反馈
- 支持多镜像源（中国大陆访问优化）

## 系统要求
- Android 5.0 (API 21) 或更高版本
- 网络连接（用于更新检查）
- 约 8MB 存储空间

## 技术栈
- Kotlin + Material Design 3
- OkHttp 4.12.0 + Gson 2.10.1
- Android DownloadManager
```

### 3. 上传APK文件
- 将 `iyth-app-v1.3.0.apk` 拖拽到 "Attach binaries" 区域
- 确保文件名为 `iyth-app-v1.3.0.apk`

### 4. 发布设置
- ✅ Set as the latest release
- ❌ Set as a pre-release (除非是测试版本)

### 5. 点击 "Publish release"

## 发布后验证

### 1. 检查Release页面
- 访问: https://github.com/qinwy/iyth/releases/tag/v1.3.0
- 确认APK下载链接正常工作

### 2. 测试下载链接
```bash
curl -I https://github.com/qinwy/iyth/releases/download/v1.3.0/iyth-app-v1.3.0.apk
```
应该返回 200 状态码

### 3. 更新配置文件
确保 `release-info.json` 中的链接与实际Release匹配：
```json
{
  "tag_name": "v1.3.0",
  "name": "IYTH App v1.3.0",
  "assets": [
    {
      "name": "iyth-app-v1.3.0.apk",
      "browser_download_url": "https://github.com/qinwy/iyth/releases/download/v1.3.0/iyth-app-v1.3.0.apk"
    }
  ]
}
```

## 自动化选项

### GitHub Actions (可选)
可以创建 `.github/workflows/release.yml` 实现自动发布：

```yaml
name: Release APK
on:
  push:
    tags:
      - 'v*'
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Setup JDK
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Build APK
      run: ./gradlew assembleRelease
    - name: Create Release
      uses: softprops/action-gh-release@v1
      with:
        files: app/build/outputs/apk/release/app-release-unsigned.apk
        name: IYTH App ${{ github.ref_name }}
```

## 注意事项

1. **APK签名**: 生产环境建议使用签名APK
2. **版本一致性**: 确保所有地方的版本号一致
3. **下载测试**: 发布后测试下载链接
4. **更新通知**: 通知用户有新版本可用
