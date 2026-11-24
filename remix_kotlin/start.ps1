# start-services.ps1 - 一键启动 Nacos / MySQL / Redis
# 用法: .\start-services.ps1 [start|stop|status]

param(
    [ValidateSet("start", "stop", "status")]
    [string]$Action = "start"
)

$NACOS_HOME = "D:\tools\nacos\nacos"
$MYSQL_SERVICE = "MySQL80"
$REDIS_PORT = 6379

function Write-Status($name, $status) {
    $color = if ($status -eq "running") { "Green" } elseif ($status -eq "stopped") { "Red" } else { "Yellow" }
    Write-Host "  [$status] $name" -ForegroundColor $color
}

function Get-PortInUse([int]$port) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("127.0.0.1", $port)
        $tcp.Close()
        return $true
    } catch {
        return $false
    }
}

function Start-MySQL {
    Write-Host "`n[MySQL]" -NoNewline
    $svc = Get-Service -Name $MYSQL_SERVICE -ErrorAction SilentlyContinue
    if (-not $svc) {
        Write-Status "MySQL" "not found"
        return
    }
    if ($svc.Status -eq "Running") {
        Write-Status "MySQL" "already running"
    } else {
        Write-Host " starting..." -NoNewline
        Start-Service -Name $MYSQL_SERVICE
        Start-Sleep -Seconds 3
        $svc = Get-Service -Name $MYSQL_SERVICE
        if ($svc.Status -eq "Running") {
            Write-Status "MySQL" "running"
        } else {
            Write-Status "MySQL" "failed"
        }
    }
}

function Stop-MySQL {
    Write-Host "`n[MySQL]" -NoNewline
    $svc = Get-Service -Name $MYSQL_SERVICE -ErrorAction SilentlyContinue
    if (-not $svc) {
        Write-Status "MySQL" "not found"
        return
    }
    if ($svc.Status -eq "Running") {
        Write-Host " stopping..." -NoNewline
        Stop-Service -Name $MYSQL_SERVICE
        Start-Sleep -Seconds 2
        Write-Status "MySQL" "stopped"
    } else {
        Write-Status "MySQL" "already stopped"
    }
}

function Start-Nacos {
    Write-Host "`n[Nacos]" -NoNewline
    if (Get-PortInUse 8848) {
        Write-Status "Nacos" "already running (port 8848)"
        return
    }
    Write-Host " starting (standalone)..." -NoNewline
    $startupCmd = Join-Path $NACOS_HOME "bin\startup.cmd"
    Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"$startupCmd`" -m standalone" -WorkingDirectory $NACOS_HOME -WindowStyle Hidden
    # 等待 Nacos 启动
    $timeout = 60
    $elapsed = 0
    while ($elapsed -lt $timeout) {
        Start-Sleep -Seconds 3
        $elapsed += 3
        if (Get-PortInUse 8848) {
            Write-Status "Nacos" "running (port 8848)"
            return
        }
    }
    Write-Status "Nacos" "timeout (check logs)"
}

function Stop-Nacos {
    Write-Host "`n[Nacos]" -NoNewline
    if (-not (Get-PortInUse 8848)) {
        Write-Status "Nacos" "not running"
        return
    }
    Write-Host " stopping..." -NoNewline
    $shutdownCmd = Join-Path $NACOS_HOME "bin\shutdown.cmd"
    try {
        & $shutdownCmd 2>$null
    } catch {
        # fallback: kill java process running nacos
        Get-WmiObject Win32_Process -Filter "Name='java.exe'" |
            Where-Object { $_.CommandLine -match "nacos" } |
            ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
    }
    Start-Sleep -Seconds 3
    if (Get-PortInUse 8848) {
        Write-Status "Nacos" "still running (force kill?)"
    } else {
        Write-Status "Nacos" "stopped"
    }
}

function Start-Redis {
    Write-Host "`n[Redis]" -NoNewline
    if (Get-PortInUse $REDIS_PORT) {
        Write-Status "Redis" "already running (port $REDIS_PORT)"
        return
    }
    Write-Host " starting (WSL)..." -NoNewline
    wsl -d Ubuntu -- bash -c "redis-server --daemonize yes --port $REDIS_PORT --bind 0.0.0.0" 2>$null
    Start-Sleep -Seconds 2
    if (Get-PortInUse $REDIS_PORT) {
        Write-Status "Redis" "running (port $REDIS_PORT)"
    } else {
        Write-Status "Redis" "failed"
    }
}

function Stop-Redis {
    Write-Host "`n[Redis]" -NoNewline
    if (-not (Get-PortInUse $REDIS_PORT)) {
        Write-Status "Redis" "not running"
        return
    }
    Write-Host " stopping..." -NoNewline
    wsl -d Ubuntu -- bash -c "redis-cli -p $REDIS_PORT shutdown nosave" 2>$null
    Start-Sleep -Seconds 2
    if (Get-PortInUse $REDIS_PORT) {
        Write-Status "Redis" "still running"
    } else {
        Write-Status "Redis" "stopped"
    }
}

function Show-Status {
    Write-Host "`n=== Service Status ==="
    # MySQL
    $svc = Get-Service -Name $MYSQL_SERVICE -ErrorAction SilentlyContinue
    if ($svc) {
        $st = if ($svc.Status -eq "Running") { "running" } else { "stopped" }
        Write-Status "MySQL" $st
    } else {
        Write-Status "MySQL" "not found"
    }
    # Nacos
    $st = if (Get-PortInUse 8848) { "running" } else { "stopped" }
    Write-Status "Nacos" $st
    # Redis
    $st = if (Get-PortInUse $REDIS_PORT) { "running" } else { "stopped" }
    Write-Status "Redis" $st
    Write-Host ""
}

# === Main ===
switch ($Action) {
    "start" {
        Write-Host "=== Starting services ==="
        Start-MySQL
        Start-Nacos
        Start-Redis
        Show-Status
    }
    "stop" {
        Write-Host "=== Stopping services ==="
        Stop-Nacos
        Stop-Redis
        Stop-MySQL
        Show-Status
    }
    "status" {
        Show-Status
    }
}
