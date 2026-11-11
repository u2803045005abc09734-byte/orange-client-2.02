@echo off
setlocal
set ROOT=%~dp0..\
if not exist "%ROOT%reference\orange-client-1.1.1-rounded.jar" (
  echo Original JAR is missing from reference\
  exit /b 1
)
java -jar "%ROOT%tools\cfr-0.152.jar" "%ROOT%reference\orange-client-1.1.1-rounded.jar" --outputdir "%ROOT%src\main\java" --silent true --comments true --showversion false
if errorlevel 1 exit /b 1
echo Decompiled Java sources refreshed in src\main\java
endlocal
