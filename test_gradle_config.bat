@echo off
chcp 65001 >nul
echo ====================================
echo 测试 Gradle 配置
echo ====================================
echo.

cd /d "%~dp0"

echo 清理项目...
call gradlew clean

echo.
echo 刷新依赖...
call gradlew --refresh-dependencies

echo.
echo 尝试构建项目...
call gradlew assembleDebug

echo.
echo ====================================
echo 测试完成！
echo ====================================
echo.
echo 如果看到 BUILD SUCCESSFUL，说明配置成功！
echo 如果仍有错误，请检查网络连接或代理设置。
echo.
pause

