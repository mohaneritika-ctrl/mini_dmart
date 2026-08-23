@echo off
cd /d "%~dp0backend"
call "%USERPROFILE%\.m2\maven-3.9.9\apache-maven-3.9.9\bin\mvn.cmd" %*
