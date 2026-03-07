@echo off
echo 启动舟山分岛医疗管理系统 - Web管理�?
echo.

echo 检查Node.js环境...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Node.js，请先安装Node.js
    pause
    exit /b 1
)

echo 检查依赖包...
if not exist "node_modules" (
    echo 正在安装依赖�?..
    npm install
    if %errorlevel% neq 0 (
        echo 错误: 依赖包安装失�?
        pause
        exit /b 1
    )
)

echo 启动开发服务器...
echo 访问地址: http://localhost:3002
echo �?Ctrl+C 停止服务�?
echo.

npm run dev

pause


