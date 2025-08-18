@echo off
echo PromptCraft - Running Minecraft Client
echo =====================================

echo Detecting Java 21...

REM Try to find Java 21 installation
for /f "tokens=*" %%i in ('where java 2^>nul') do (
    echo Found java.exe at: %%i
    
    REM Check if this is Java 21
    "%%i" -version 2>&1 | findstr "21.0" >nul
    if !ERRORLEVEL! EQU 0 (
        echo Found Java 21!
        
        REM Get the directory containing java.exe
        for %%j in ("%%i") do set JAVA_BIN_DIR=%%~dpj
        
        REM Remove trailing backslash and \bin to get JAVA_HOME
        set JAVA_BIN_DIR=!JAVA_BIN_DIR:~0,-1!
        for %%k in ("!JAVA_BIN_DIR!") do set "JAVA_HOME=%%~dpk"
        set JAVA_HOME=!JAVA_HOME:~0,-1!
        
        echo Setting JAVA_HOME to: !JAVA_HOME!
        goto :run_client
    )
)

echo Could not find Java 21. Please install Java 21 first.
pause
exit /b 1

:run_client
echo.
echo Verifying Java version...
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo Starting Minecraft client with PromptCraft mod...
echo This may take a few minutes to download dependencies and start...
echo.

call gradlew.bat runClient

echo.
if %ERRORLEVEL% EQU 0 (
    echo Client exited normally.
) else (
    echo Client exited with error code %ERRORLEVEL%
    echo Check the logs above for any issues.
)

pause
