@echo off
cd /d "%~dp0"
start "今日页-后端" cmd /k "cd /d "%~dp0backend" && java -jar target\qingjian-backend-0.2.0.jar"
start "今日页-前端" cmd /k "cd /d "%~dp0frontend" && npm run dev"
