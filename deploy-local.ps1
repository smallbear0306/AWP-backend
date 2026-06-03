# AWP 本地部署脚本：构建前端 → 拷入后端 static → 打包 → 运行（单端口 8080 同供 UI 与 API）
# 用法：在 AWP-backend 目录下执行  .\deploy-local.ps1
# 前置：已装 Node、Maven、JDK17+，且 MySQL(awp 库) 在运行；密码在 application-local.yml

$ErrorActionPreference = "Stop"
$be = $PSScriptRoot
$fe = Join-Path (Split-Path $be -Parent) "AWP-frontend"
$static = Join-Path $be "src\main\resources\static"

Write-Host "[1/4] 构建前端..." -ForegroundColor Cyan
Push-Location $fe
npm install
npm run build
Pop-Location

Write-Host "[2/4] 拷贝前端产物到后端 static..." -ForegroundColor Cyan
if (Test-Path $static) { Remove-Item -Recurse -Force $static }
New-Item -ItemType Directory -Force $static | Out-Null
Copy-Item -Recurse -Force (Join-Path $fe "dist\*") $static

Write-Host "[3/4] 打包后端 jar..." -ForegroundColor Cyan
Push-Location $be
mvn -DskipTests package
Pop-Location

$jar = Get-ChildItem (Join-Path $be "target\*.jar") | Select-Object -First 1
Write-Host "[4/4] 启动: http://localhost:8080  (Ctrl+C 停止)" -ForegroundColor Green
Write-Host "      另开一个终端运行  cpolar http 8080  即可对外暴露" -ForegroundColor Green
java -jar $jar.FullName
