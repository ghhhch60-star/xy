# APK安装问题解决方案

## 问题描述
错误代码：`-124: Failed parse during installPackageLI: Targeting R+ (version 30 and above) requires the resources.arsc of installed APKs to be stored uncompressed and aligned on a 4-byte boundary`

## 问题原因
1. **Android 11+ 严格要求**：从Android 11 (API 30)开始，系统要求APK中的`resources.arsc`文件必须：
   - 未压缩存储
   - 4字节边界对齐

2. **加固工具影响**：加固工具在处理APK时可能：
   - 重新压缩了资源文件
   - 破坏了文件对齐
   - 修改了APK内部结构

## 已尝试的解决方案
✅ **已测试但失败的方法：**
- `adb install --bypass-low-target-sdk-block`
- `adb install --force-sdk`
- `adb install --force-queryable`
- 组合参数安装
- 从`/data/local/tmp/`目录安装

## 推荐解决方案

### 方案1：使用原始APK（推荐）
```bash
# 使用未加固的原始APK进行安装
adb install app-release.apk
```

### 方案2：修复加固APK
```bash
# 1. 解压APK
mkdir temp_apk
cd temp_apk
unzip ../app-release-jiagu-signed.apk

# 2. 使用zipalign重新对齐
zipalign -f -p 4 ../app-release-jiagu-signed.apk app-aligned.apk

# 3. 重新签名
apksigner sign --ks your-keystore.jks --out app-fixed.apk app-aligned.apk

# 4. 安装修复后的APK
adb install app-fixed.apk
```

### 方案3：降级目标API
如果可以修改源码：
```xml
<!-- 在AndroidManifest.xml中降低targetSdkVersion -->
<uses-sdk
    android:minSdkVersion="21"
    android:targetSdkVersion="29" />
```

### 方案4：使用兼容的加固服务
1. **360加固保** - 支持Android 11+
2. **腾讯乐固** - 有Android 11+兼容版本
3. **梆梆安全** - 提供资源文件对齐选项

### 方案5：开发者选项（临时）
在测试设备上：
```bash
# 启用开发者选项中的"强制允许在外部存储上的应用"
adb shell settings put global force_allow_on_external 1

# 或者降级设备到Android 10进行测试
```

## 技术细节

### 资源文件对齐要求
- **对齐边界**：4字节 (32位)
- **压缩要求**：resources.arsc必须未压缩
- **工具**：使用`zipalign`进行对齐

### 检查APK对齐状态
```bash
# 检查APK是否正确对齐
zipalign -c -v 4 app-release-jiagu-signed.apk
```

### 手动修复步骤
```bash
# 1. 创建未压缩的APK
zip -0 app-uncompressed.zip -r . (在解压的APK目录中)

# 2. 重命名为APK
mv app-uncompressed.zip app-uncompressed.apk

# 3. 对齐
zipalign -f 4 app-uncompressed.apk app-aligned.apk

# 4. 签名
apksigner sign --ks keystore.jks app-aligned.apk
```

## 预防措施

### 选择加固服务时
1. 确认支持Android 11+
2. 询问是否保持资源文件对齐
3. 要求提供兼容性测试报告

### 构建流程优化
```gradle
android {
    compileSdkVersion 33
    
    defaultConfig {
        targetSdkVersion 33
        // 确保资源不被压缩
        aaptOptions {
            noCompress 'arsc'
        }
    }
}
```

## 联系支持
如果问题持续存在：
1. 联系加固服务提供商技术支持
2. 提供具体的错误信息和设备信息
3. 要求提供Android 11+兼容的加固版本

---
**注意**：建议在生产环境中使用经过充分测试的加固方案，确保兼容性和安全性。
