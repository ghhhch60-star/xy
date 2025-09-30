# 🔍 如何查找GitHub仓库信息 - 详细图解

## 🌐 方法一：通过GitHub网站

### 第1步：访问GitHub
```
🌐 在浏览器中打开：https://github.com
👆 点击右上角 "Sign in" 登录
```

### 第2步：查看您的仓库
```
登录后的操作：
👤 点击右上角的头像/用户名
📁 选择 "Your repositories" 
📋 在仓库列表中找到您的项目
```

### 第3步：获取仓库信息
```
在仓库页面：
🟢 点击绿色的 "< > Code" 按钮
📋 选择 "HTTPS" 标签
📎 复制显示的URL链接
```

**复制的URL格式像这样：**
```
https://github.com/您的用户名/仓库名称.git
```

---

## 📱 方法二：如果您使用GitHub手机APP

### GitHub Mobile App步骤：
```
📱 打开GitHub手机应用
👤 点击底部的 "Profile"
📁 选择 "Repositories"
🔍 找到您的项目仓库
👆 点击进入仓库页面
🔗 点击 "Clone" 或分享按钮获取URL
```

---

## 💻 方法三：如果您之前克隆过仓库

如果您之前在其他地方克隆过这个仓库，可以在那个文件夹中查看：

### Windows方法：
```cmd
# 打开命令提示符或PowerShell
# 进入之前的项目文件夹
cd 您的项目文件夹路径
git remote -v
```

### 会显示类似：
```
origin  https://github.com/用户名/仓库名.git (fetch)
origin  https://github.com/用户名/仓库名.git (push)
```

---

## 🎯 我需要的具体信息

### 选项A - 完整URL（推荐）
```
https://github.com/用户名/仓库名.git
```

### 选项B - 分别提供
```
GitHub用户名：您的用户名
仓库名称：您的仓库名
```

### 选项C - 仓库页面链接
```
https://github.com/用户名/仓库名
```

---

## 📞 实际示例

**正确的信息格式：**

✅ **完整URL：**
```
https://github.com/qinwy/xy-root-manager.git
```

✅ **分别提供：**
```
用户名：qinwy
仓库名：xy-root-manager
```

✅ **仓库页面：**
```
https://github.com/qinwy/xy-root-manager
```

---

## 🚨 常见问题

### Q: 我忘记了仓库名称怎么办？
**A:** 登录GitHub后，点击头像 → "Your repositories"，查看所有仓库列表

### Q: 我找不到我的仓库？
**A:** 
1. 确认您登录了正确的GitHub账户
2. 检查是否在 "Your repositories" 页面
3. 使用页面上的搜索框搜索项目名称

### Q: 仓库是私有的会影响吗？
**A:** 不会影响，私有仓库也可以正常使用Release功能

### Q: 我有多个仓库，不确定是哪个？
**A:** 查看仓库的描述和最后更新时间，选择包含Android项目的仓库

---

## ⏰ 下一步

找到信息后，直接告诉我：
```
我的GitHub仓库是：[您复制的URL]
```

然后我会立即帮您：
✅ 连接本地项目到GitHub
✅ 更新版本号到v1.6  
✅ 构建新APK
✅ 创建Release发布
