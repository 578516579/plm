# seed-all.ps1 — Windows PowerShell 版批量 seed
# 用法:
#   $env:DB_PASSWORD='...'; .\seed-all.ps1                # 跑所有
#   $env:DB_PASSWORD='...'; .\seed-all.ps1 -Modules project,sprint
#   $env:DB_PASSWORD='...'; .\seed-all.ps1 -Cleanup       # 反向清理

[CmdletBinding()]
param(
    [string[]] $Modules = @(),
    [switch] $Cleanup
)

$ErrorActionPreference = 'Stop'

if (-not $env:DB_PASSWORD) {
    Write-Host "❌ DB_PASSWORD 环境变量未设置" -ForegroundColor Red
    exit 1
}

# 找 mysql.exe
$mysqlBin = $env:MYSQL_BIN
if (-not $mysqlBin) {
    $candidates = @(
        'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe',
        'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { $mysqlBin = $c; break }
    }
    if (-not $mysqlBin) {
        $mysqlBin = (Get-Command mysql -ErrorAction SilentlyContinue).Source
    }
}
if (-not $mysqlBin -or -not (Test-Path $mysqlBin)) {
    Write-Host "❌ 找不到 mysql.exe,设 `$env:MYSQL_BIN" -ForegroundColor Red
    exit 1
}

$db = if ($env:DB_NAME) { $env:DB_NAME } else { 'plm' }
$seedDir = $PSScriptRoot

function Invoke-Mysql ([string] $file) {
    Write-Host "── seed: $file" -ForegroundColor Cyan
    & $mysqlBin "-uroot" "-p$($env:DB_PASSWORD)" "--default-character-set=utf8mb4" $db -e "source $file"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ $file 执行失败" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

if ($Cleanup) {
    Invoke-Mysql (Join-Path $seedDir 'seed-cleanup.sql')
    Write-Host "✅ seed cleanup 完成" -ForegroundColor Green
    exit 0
}

if ($Modules.Count -eq 0) {
    Get-ChildItem -Path $seedDir -Filter 'seed-*.sql' |
        Where-Object { $_.Name -ne 'seed-cleanup.sql' } |
        ForEach-Object { Invoke-Mysql $_.FullName }
}
else {
    foreach ($m in $Modules) {
        $f = Join-Path $seedDir "seed-$m.sql"
        if (-not (Test-Path $f)) {
            Write-Host "❌ 找不到 $f" -ForegroundColor Red
            exit 1
        }
        Invoke-Mysql $f
    }
}

Write-Host "✅ seed 完成" -ForegroundColor Green
