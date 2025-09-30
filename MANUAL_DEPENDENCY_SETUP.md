# 手动安装 libsu 依赖

## 下载地址
1. 访问 https://github.com/topjohnwu/libsu/releases
2. 下载 v5.2.2 版本的 AAR 文件

## 本地安装步骤

### 方法 A：使用本地 libs 文件夹
1. 在 `app/libs/` 目录下创建文件夹（如果不存在）
2. 将下载的 AAR 文件放入该文件夹
3. 在 Android Studio 中，右键点击项目 → "Sync Project with Gradle Files"

### 方法 B：使用 Android Studio 的库管理器
1. 在 Android Studio 中：File → Project Structure
2. 选择 "Dependencies" → "app" 模块
3. 点击 "+" → "JAR/AAR Dependency"
4. 浏览并选择下载的 AAR 文件

## 临时解决方案：移除 Root 功能

如果您只是想快速构建 APK 进行测试，可以临时注释掉 Root 相关代码：

1. 在 `app/build.gradle` 中注释掉 libsu 依赖：
   ```gradle
   // implementation 'com.github.topjohnwu.libsu:core:5.2.2'
   // implementation 'com.github.topjohnwu.libsu:io:5.2.2'
   ```

2. 在代码中使用条件编译或 try-catch 包裹 Root 相关代码

注意：这样构建的 APK 将无法使用 Root 功能。

