@echo off
chcp 65001 >nul
echo.
echo ========================================
echo    医疗管理系统 Web预览启动器
echo ========================================
echo.

REM 检查Python是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未检测到Python!
    echo 请先安装Python 3.x
    echo 下载地址: https://www.python.org/downloads/
    pause
    exit /b 1
)

REM 检查web-preview目录是否存在
if not exist "web-preview" (
    echo ❌ 错误: web-preview目录不存在!
    echo 请确保项目结构正确。
    pause
    exit /b 1
)

REM 检查index.html是否存在
if not exist "web-preview\index.html" (
    echo ❌ 错误: index.html文件不存在!
    pause
    exit /b 1
)

echo 🚀 正在启动Web预览服务器...
echo 📱 访问地址: http://localhost:8000
echo 📁 服务目录: web-preview
echo 🔄 按 Ctrl+C 停止服务器
echo.

REM 切换到web-preview目录并启动服务器
cd web-preview
python -m http.server 8000

echo.
echo 🛑 服务器已停止
pause
