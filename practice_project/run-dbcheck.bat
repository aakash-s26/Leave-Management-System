@echo off
cd /d %~dp0
set /p CP=<cp.txt
javac -cp "%CP%" DbCheck.java
java -cp "%CP%;." DbCheck
