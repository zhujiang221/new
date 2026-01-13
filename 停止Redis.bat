@echo off
chcp 65001 >nul
title Redis 停止脚本
color 0C

echo ========================================
echo         Redis 停止脚本
echo ========================================
echo.

REM 检查 Redis 是否在运行
tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I /N "redis-server.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [信息] 正在停止 Redis 服务...
    echo.
    taskkill /F /IM redis-server.exe
    if %ERRORLEVEL% == 0 (
        echo [成功] Redis 服务已成功停止！
    ) else (
        echo [错误] 停止 Redis 失败，可能需要管理员权限
    )
) else (
    echo [信息] Redis 服务未运行
)

echo.
pause
