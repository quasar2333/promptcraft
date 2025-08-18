@echo off
echo Fixing JAVA_HOME and running client...
echo ====================================

echo Current JAVA_HOME: %JAVA_HOME%

REM Try common Java 21 installation paths
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
    echo Set JAVA_HOME to Adoptium JDK 21
    goto :verify
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.8+9" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.8+9"
    echo Set JAVA_HOME to Adoptium JDK 21
    goto :verify
)

if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    echo Set JAVA_HOME to Oracle JDK 21
    goto :verify
)

REM Try to find from java.exe location
for /f "tokens=*" %%i in ('where java 2^>nul') do (
    echo Found java at: %%i
    
    REM Check if it's Java 21
    "%%i" -version 2>&1 | findstr "21.0" >nul
    if not errorlevel 1 (
        echo This is Java 21, deriving JAVA_HOME...
        
        REM Get directory path
        for %%j in ("%%i") do set "JAVA_BIN=%%~dpj"
        
        REM Remove \bin\ to get JAVA_HOME
        set "JAVA_BIN=%JAVA_BIN:~0,-1%"
        for %%k in ("%JAVA_BIN%") do set "JAVA_HOME=%%~dpk"
        set "JAVA_HOME=%JAVA_HOME:~0,-1%"
        
        echo Derived JAVA_HOME: %JAVA_HOME%
        goto :verify
    )
)

echo Could not find Java 21 installation!
pause
exit /b 1

:verify
echo.
echo New JAVA_HOME: %JAVA_HOME%
echo Verifying Java version:
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo Starting Minecraft client...
echo This will download dependencies and may take several minutes...
echo.

gradlew.bat runClient

echo.
echo Client finished with exit code: %ERRORLEVEL%

if %ERRORLEVEL% EQU 0 (
    echo ✓ Client ran successfully!
) else (
    echo ✗ Client failed to start. Check the logs above.
)

pause
