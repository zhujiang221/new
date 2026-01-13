@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Redis 启动脚本
color 0A

echo ========================================
echo         Redis 自动启动脚本
echo ========================================
echo.

REM 检查 Redis 是否已经在运行
tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I /N "redis-server.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [信息] Redis 服务已经在运行中！
    echo [信息] 进程信息：
    tasklist /FI "IMAGENAME eq redis-server.exe"
    echo.
    pause
    exit /b
)

echo [信息] 正在查找 Redis 安装路径...
echo.

REM 定义可能的 Redis 安装路径
set REDIS_FOUND=0
set REDIS_PATH=

REM 检查方式1: 系统 PATH 中的 redis-server
where redis-server >nul 2>&1
if %ERRORLEVEL% == 0 (
    set REDIS_PATH=redis-server
    set REDIS_FOUND=1
    echo [成功] 在系统 PATH 中找到 Redis
    goto :START_REDIS
)

REM 检查方式2: 常见的安装路径
if exist "C:\Program Files\Redis\redis-server.exe" (
    set REDIS_PATH=C:\Program Files\Redis\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: C:\Program Files\Redis\redis-server.exe
    goto :START_REDIS
)

if exist "C:\Program Files (x86)\Redis\redis-server.exe" (
    set REDIS_PATH=C:\Program Files (x86)\Redis\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: C:\Program Files (x86)\Redis\redis-server.exe
    goto :START_REDIS
)

if exist "D:\Redis\redis-server.exe" (
    set REDIS_PATH=D:\Redis\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: D:\Redis\redis-server.exe
    goto :START_REDIS
)

if exist "C:\redis\redis-server.exe" (
    set REDIS_PATH=C:\redis\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: C:\redis\redis-server.exe
    goto :START_REDIS
)

if exist "D:\redis\redis-server.exe" (
    set REDIS_PATH=D:\redis\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: D:\redis\redis-server.exe
    goto :START_REDIS
)

if exist "%USERPROFILE%\Redis\redis-server.exe" (
    set REDIS_PATH=%USERPROFILE%\Redis\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: %USERPROFILE%\Redis\redis-server.exe
    goto :START_REDIS
)

if exist "%CD%\Redis\redis-server.exe" (
    set REDIS_PATH=%CD%\Redis\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: %CD%\Redis\redis-server.exe
    goto :START_REDIS
)

if exist "%CD%\redis-server.exe" (
    set REDIS_PATH=%CD%\redis-server.exe
    set REDIS_FOUND=1
    echo [成功] 找到 Redis: %CD%\redis-server.exe
    goto :START_REDIS
)

REM 检查方式3: 通过 WSL 运行 Redis
where wsl >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo [信息] 检测到 WSL，尝试通过 WSL 启动 Redis...
    wsl redis-server --daemonize yes >nul 2>&1
    if %ERRORLEVEL% == 0 (
        echo [成功] 已通过 WSL 启动 Redis
        echo.
        echo [提示] Redis 正在 WSL 中运行
        pause
        exit /b
    )
)

REM 如果都没找到，显示错误信息
if %REDIS_FOUND% == 0 (
    echo [错误] 未找到 Redis 安装！
    echo.
    echo 请选择以下方式之一安装 Redis：
    echo.
    echo 方式1: 下载 Windows 版本
    echo   - 访问: https://github.com/microsoftarchive/redis/releases
    echo   - 下载并安装 Redis for Windows
    echo.
    echo 方式2: 使用 WSL
    echo   - 在 WSL 中安装: sudo apt-get install redis-server
    echo   - 然后运行: wsl redis-server
    echo.
    echo 方式3: 使用 Docker
    echo   - 运行: docker run -d -p 6379:6379 redis
    echo.
    echo 方式4: 手动指定路径
    echo   - 将 Redis 可执行文件放在项目根目录
    echo   - 或修改此脚本中的路径配置
    echo.
    pause
    exit /b 1
)

:START_REDIS
echo.
echo [信息] 正在启动 Redis 服务...
echo [信息] Redis 将运行在: localhost:6379
echo.

REM 启动 Redis（不显示窗口，后台运行）
start "" /B "%REDIS_PATH%"

REM 等待一下让 Redis 启动
timeout /t 2 /nobreak >nul

REM 检查 Redis 是否成功启动
timeout /t 1 /nobreak >nul
tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I /N "redis-server.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [成功] Redis 服务已成功启动！
    echo.
    echo [信息] Redis 进程信息：
    tasklist /FI "IMAGENAME eq redis-server.exe"
    echo.
    echo [提示] Redis 正在后台运行
    echo [提示] 要停止 Redis，请运行: taskkill /F /IM redis-server.exe
    echo.
) else (
    echo [警告] Redis 可能启动失败，请检查错误信息
    echo.
)

pause
