@echo off
chcp 65001 >nul
echo ================================================
echo   EduNet - Starting Server
echo ================================================
cd /d "%~dp0"

REM Compile with UTF-8 encoding if needed
if not exist "bin\server\ChatServer.class" (
    echo Compiling server files...
    javac -encoding UTF-8 -d bin -sourcepath src src\server\*.java src\client\*.java src\client\ui\*.java src\utils\*.java
)

echo.
echo Starting server...
java -cp bin server.ChatServer
pause
