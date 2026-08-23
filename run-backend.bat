@echo off
echo Starting Mini D-Mart Backend (Spring Boot)...
cd /d "%~dp0backend"
call "%USERPROFILE%\.m2\maven-3.9.9\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
pause
