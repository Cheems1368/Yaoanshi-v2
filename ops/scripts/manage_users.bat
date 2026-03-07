@echo off
chcp 65001 >nul
echo ========================================
echo 用户管理脚本 - 舟山分岛医疗管理系统
echo ========================================
echo.

echo 选择操作：
echo 1. 初始化用户账号
echo 2. 重置所有用户密码
echo 3. 查看用户列表
echo 4. 测试用户登录
echo 5. 退出
echo.

set /p choice=请输入选择 (1-5): 

if "%choice%"=="1" goto init_users
if "%choice%"=="2" goto reset_passwords
if "%choice%"=="3" goto list_users
if "%choice%"=="4" goto test_login
if "%choice%"=="5" goto exit
goto invalid_choice

:init_users
echo.
echo 正在初始化用户账号...
docker exec -i medicine-mysql mysql -uroot -p123456 < sql/user_management.sql
if %errorlevel% equ 0 (
    echo ✅ 用户账号初始化完成
) else (
    echo ❌ 用户账号初始化失败
)
goto end

:reset_passwords
echo.
echo 正在重置所有用户密码为 123456...
docker exec -i medicine-mysql mysql -uroot -p123456 -e "USE medicine; UPDATE system_users SET password = '\$2a\$10\$GP8zvqHB//TekuzYZSBYAuBQJiNq1.fxQVDYJ.uBCOnWCtDVKE4H6' WHERE username IN ('admin', 'doctor', 'patient', 'pharmacist');"
if %errorlevel% equ 0 (
    echo ✅ 密码重置完成
) else (
    echo ❌ 密码重置失败
)
goto end

:list_users
echo.
echo 当前用户列表：
docker exec -it medicine-mysql mysql -uroot -p123456 -e "USE medicine; SELECT username as '用户名', nickname as '昵称', status as '状态', create_time as '创建时间' FROM system_users WHERE status = 0 ORDER BY username;"
goto end

:test_login
echo.
echo 测试用户登录...
echo 测试 admin 账号...
curl -s -X POST http://localhost:48080/admin-api/system/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"admin123\"}" | findstr "accessToken" >nul
if %errorlevel% equ 0 (
    echo ✅ admin 登录成功
) else (
    echo ❌ admin 登录失败
)

echo 测试 doctor 账号...
curl -s -X POST http://localhost:48080/admin-api/system/auth/login -H "Content-Type: application/json" -d "{\"username\":\"doctor\",\"password\":\"123456\"}" | findstr "accessToken" >nul
if %errorlevel% equ 0 (
    echo ✅ doctor 登录成功
) else (
    echo ❌ doctor 登录失败
)
goto end

:invalid_choice
echo ❌ 无效选择，请重新输入
goto end

:exit
echo 退出用户管理脚本
goto end

:end
echo.
pause
