@echo off
REM AWP 后端开机自启：等待约15秒(确保 MySQL 就绪)后，用 javaw 无窗口后台启动 jar
REM (ping 延时比 timeout 更可靠，无控制台也能用)
ping -n 16 127.0.0.1 >nul
start "" "C:\Program Files\Java\jdk-21.0.11\bin\javaw.exe" -jar "C:\Users\DZH16\Desktop\Work\Others\AWP\AWP-backend\target\awp-backend-0.0.1-SNAPSHOT.jar"
