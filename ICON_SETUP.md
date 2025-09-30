# 应用图标设置说明

## 图标文件位置
应用图标文件 `ye.png` 已经在 `log/` 文件夹中。

## 设置步骤

1. 将 `log/ye.png` 文件复制到以下目录（需要调整大小）：
   - `app/src/main/res/mipmap-mdpi/` (48x48 px)
   - `app/src/main/res/mipmap-hdpi/` (72x72 px)
   - `app/src/main/res/mipmap-xhdpi/` (96x96 px)
   - `app/src/main/res/mipmap-xxhdpi/` (144x144 px)
   - `app/src/main/res/mipmap-xxxhdpi/` (192x192 px)

2. 文件命名：
   - 主图标: `ic_launcher.png`
   - 圆形图标: `ic_launcher_round.png`
   - 前景图标: `ic_launcher_foreground.png` (用于自适应图标)

3. 使用Android Studio的Image Asset Studio：
   - 右键点击 `app/src/main/res` 文件夹
   - 选择 New > Image Asset
   - 选择 Launcher Icons (Adaptive and Legacy)
   - 选择你的源图片 `log/ye.png`
   - 调整设置并生成所有尺寸的图标

## 注意事项
- 确保图标背景透明或使用纯色背景
- 图标应该清晰且在小尺寸下仍可识别
- 建议使用矢量图格式（SVG）以获得最佳缩放效果

