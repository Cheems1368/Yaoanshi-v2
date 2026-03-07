@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 医疗管理系统 - Web端启动脚本
echo ========================================
echo.

echo 📋 检查环境...
echo.

REM 检查Node.js是否安装
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js 未安装
    echo.
    echo 📥 请下载并安装Node.js:
    echo https://nodejs.org/
    echo.
    pause
    exit /b 1
)

echo ✅ Node.js 已安装
node --version

REM 检查npm是否可用
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ npm 不可用
    pause
    exit /b 1
)

echo ✅ npm 可用
npm --version

echo.
echo 📁 检查项目目录...
if not exist "web-admin" (
    echo ❌ web-admin 目录不存在
    echo.
    echo 📁 请确保在正确的项目目录中运行此脚本
    echo 当前目录: %CD%
    echo.
    pause
    exit /b 1
)

echo ✅ 项目目录存在
echo.

echo 📦 安装依赖...
cd web-admin
if not exist "node_modules" (
    echo 正在安装依赖包，请稍候...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ 依赖安装失败
        pause
        exit /b 1
    )
    echo ✅ 依赖安装完成
) else (
    echo ✅ 依赖已存在
)

echo.
echo 🎯 启动开发服务器...
echo.

REM 启动开发服务器
npm run dev

echo.
echo 📱 开发服务器已启动
echo.
echo 📋 访问地址:
echo http://localhost:3000
echo.
echo 📖 详细指南请查看: web-admin/README.md
echo.

pause



