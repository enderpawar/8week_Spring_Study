$ErrorActionPreference = "Stop"

$principal = [Security.Principal.WindowsPrincipal]::new(
    [Security.Principal.WindowsIdentity]::GetCurrent()
)

if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    throw "Run PowerShell as Administrator, then run this script again."
}

$mysqlHome = "C:\tools\mysql\mysql-9.6.0-winx64"
$dataDir = Join-Path $mysqlHome "data"
$mysqld = Join-Path $mysqlHome "bin\mysqld.exe"
$mysqladmin = Join-Path $mysqlHome "bin\mysqladmin.exe"
$myIni = Join-Path $mysqlHome "my.ini"
$serviceName = "MySQL96"

function Test-MySqlReady {
    & cmd.exe /d /c ('"{0}" -u root ping >nul 2>&1' -f $mysqladmin)
    return $LASTEXITCODE -eq 0
}

if (-not (Test-Path -LiteralPath $mysqld)) {
    throw "MySQL Server executable was not found: $mysqld"
}

if (-not (Test-Path -LiteralPath $dataDir)) {
    Write-Host "Initializing the MySQL data directory..."
    & $mysqld `
        --initialize-insecure `
        --console `
        "--basedir=$mysqlHome" `
        "--datadir=$dataDir"

    if ($LASTEXITCODE -ne 0) {
        throw "MySQL data directory initialization failed (exit code $LASTEXITCODE)."
    }
}
else {
    Write-Host "The MySQL data directory already exists; initialization was skipped."
}

$configuration = @"
[mysqld]
basedir=$($mysqlHome.Replace('\', '/'))
datadir=$($dataDir.Replace('\', '/'))
port=3306
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
"@
Set-Content -LiteralPath $myIni -Value $configuration -Encoding ASCII

$service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
if ($service) {
    $serviceRegistryPath = "HKLM:\SYSTEM\CurrentControlSet\Services\$serviceName"
    $registeredPath = (Get-ItemProperty -LiteralPath $serviceRegistryPath -Name ImagePath).ImagePath
    if ($registeredPath -notlike "*--defaults-file=*") {
        Write-Host "Removing the incorrectly registered $serviceName service..."
        if ($service.Status -ne "Stopped") {
            Stop-Service -Name $serviceName -Force
        }
        & sc.exe delete $serviceName | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "The incorrectly registered MySQL service could not be removed."
        }

        for ($attempt = 0; $attempt -lt 20; $attempt++) {
            if (-not (Get-Service -Name $serviceName -ErrorAction SilentlyContinue)) {
                break
            }
            Start-Sleep -Milliseconds 250
        }
        $service = $null
    }
}

if (-not $service) {
    Write-Host "Registering the $serviceName service with MySQL..."
    $installCommand = ('"{0}" --install {1} --defaults-file="{2}"' -f $mysqld, $serviceName, $myIni)
    & cmd.exe /d /c $installCommand

    $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
    if (-not $service) {
        throw "MySQL did not register the $serviceName service."
    }
}

$service = Get-Service -Name $serviceName
if ($service.Status -ne "Running") {
    if (Test-MySqlReady) {
        Write-Host "Stopping the standalone MySQL process..."
        & cmd.exe /d /c ('"{0}" -u root shutdown' -f $mysqladmin)
        if ($LASTEXITCODE -ne 0) {
            throw "The standalone MySQL process could not be stopped."
        }
        Start-Sleep -Seconds 2
    }

    Write-Host "Starting the $serviceName service..."
    Start-Service -Name $serviceName
}

$ready = $false
for ($attempt = 0; $attempt -lt 20; $attempt++) {
    if (Test-MySqlReady) {
        $ready = $true
        break
    }
    Start-Sleep -Milliseconds 500
}

if (-not $ready) {
    throw "The MySQL service started, but it did not become ready in time."
}

Write-Host ""
Write-Host "MySQL is ready." -ForegroundColor Green
Write-Host "Service: $serviceName"
Write-Host "User: root"
Write-Host "Password: empty (local study environment only)"
Write-Host "Connect with:"
Write-Host "  & '$mysqlHome\bin\mysql.exe' -u root"
