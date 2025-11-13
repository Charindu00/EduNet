@echo off
chcp 65001 >nul
echo ================================================
echo   EduNet - Starting Client
echo ================================================
cd /d "%~dp0"

REM Compile with UTF-8 encoding if needed
if not exist "bin\client\ui\LoginWindow.class" (
    echo Compiling client files...
    javac -encoding UTF-8 -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java
)

echo.
echo Starting client GUI...
java -cp bin client.ui.LoginWindow
pause
