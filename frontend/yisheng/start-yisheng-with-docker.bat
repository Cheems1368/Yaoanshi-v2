@echo off
echo ========================================
echo 舟山分岛医疗管理系统 - yisheng前端启动
echo ========================================
echo.

echo 正在检查Docker服务状态...
docker-compose ps | findstr "medicine-server"
if %errorlevel% equ 0 (
    echo ✅ Docker后端服务已运行
) else (
    echo ❌ Docker后端服务未运行，请先启动Docker服务
    echo.
    echo 启动命令：
    echo cd ..
    echo docker-compose --env-file docker.env up -d
    echo.
    pause
    exit /b 1
)

echo.
echo 正在启动yisheng前端服务...
cd web-admin
start "yisheng前端服务" cmd /k "npm run dev"

echo.
echo ✅ yisheng前端服务启动中...
echo 📱 yisheng前端地址: http://localhost:3000
echo 🔧 Docker后端地址: http://localhost:48080
echo 🌐 Docker前端地址: http://localhost:8080
echo 📚 API文档: http://localhost:48080/swagger-ui.html
echo.
echo 按任意键退出...
pause > nul
