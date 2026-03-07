@echo off
echo ========================================
echo 舟山分岛医疗管理系统 - 后端集成启动
echo ========================================
echo.

echo 正在检查后端服务状态...
curl -s http://localhost:8080/actuator/health > nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ 后端服务已运行在 http://localhost:8080
) else (
    echo ❌ 后端服务未运行，请先启动后端服务
    echo.
    echo 启动后端服务命令：
    echo cd medicine-server
    echo mvn spring-boot:run
    echo.
    pause
    exit /b 1
)

echo.
echo 正在启动前端开发服务器...
cd web-admin
start "前端开发服务器" cmd /k "npm run dev"

echo.
echo ✅ 前端服务启动中...
echo 📱 前端地址: http://localhost:3000
echo 🔧 后端地址: http://localhost:8080
echo 📚 API文档: http://localhost:8080/swagger-ui.html
echo.
echo 按任意键退出...
pause > nul

