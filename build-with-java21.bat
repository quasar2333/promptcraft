@echo off
echo PromptCraft Build with Java 21
echo ===============================

echo Current JAVA_HOME: %JAVA_HOME%

REM Find Java 21 installation
echo Searching for Java 21...

REM Try to find Java 21 in common locations
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot" (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
    echo Found Java 21 at: %JAVA_HOME%
    goto :build
)

if exist "C:\Program Files\Java\jdk-21" (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    echo Found Java 21 at: %JAVA_HOME%
    goto :build
)

REM Try to detect from current java command
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
    echo Java version: %%i
    echo %%i | findstr "21.0" >nul
    if not errorlevel 1 (
        echo Current Java is version 21, trying to find JAVA_HOME...
        
        REM Get java.exe path and derive JAVA_HOME
        for /f "tokens=*" %%j in ('where java') do (
            set JAVA_EXE=%%j
            goto :found_java_exe
        )
    )
)

echo Could not find Java 21 installation.
echo Please install Java 21 or set JAVA_HOME manually.
pause
exit /b 1

:found_java_exe
echo Java executable: %JAVA_EXE%

REM Extract directory from java.exe path
for %%k in ("%JAVA_EXE%") do set JAVA_BIN=%%~dpk
set JAVA_BIN=%JAVA_BIN:~0,-1%

REM Go up one directory from bin to get JAVA_HOME
for %%l in ("%JAVA_BIN%") do set "JAVA_HOME=%%~dpl"
set JAVA_HOME=%JAVA_HOME:~0,-1%

echo Detected JAVA_HOME: %JAVA_HOME%

:build
echo.
echo Setting JAVA_HOME for this build: %JAVA_HOME%
echo Verifying Java version...
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo Starting Gradle build...
call gradlew.bat clean build

echo.
if %ERRORLEVEL% EQU 0 (
    echo BUILD SUCCESSFUL!
    echo.
    echo Generated files:
    if exist build\libs (
        dir build\libs\*.jar
    )
) else (
    echo BUILD FAILED with exit code %ERRORLEVEL%
)

pause
