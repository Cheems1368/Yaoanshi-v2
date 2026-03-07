@echo off
echo Starting Yaoshian Pharmacist Web...
echo.

node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Node.js is required.
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo Installing dependencies...
    npm install
    if %errorlevel% neq 0 (
        echo Error: dependency installation failed.
        pause
        exit /b 1
    )
)

echo Launching dev server on http://localhost:3003
echo Press Ctrl+C to stop.
echo.

npm run dev
pause
