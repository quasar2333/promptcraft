@echo off
echo Fixing JAVA_HOME for Gradle build...

echo Current JAVA_HOME: %JAVA_HOME%
echo Current PATH Java version:
java -version

echo.
echo Detecting Java 21 installation...

REM Try common Java 21 installation paths
set JAVA21_PATHS[0]=C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot
set JAVA21_PATHS[1]=C:\Program Files\Eclipse Adoptium\jdk-21*
set JAVA21_PATHS[2]=C:\Program Files\Java\jdk-21*
set JAVA21_PATHS[3]=C:\Program Files\OpenJDK\jdk-21*
set JAVA21_PATHS[4]=C:\Program Files\Microsoft\jdk-21*

REM Find where java.exe is located
for /f "tokens=*" %%i in ('where java 2^>nul') do (
    echo Found java.exe at: %%i
    
    REM Get the directory containing java.exe
    for %%j in ("%%i") do set JAVA_BIN_DIR=%%~dpj
    
    REM Remove trailing backslash and \bin to get JAVA_HOME
    set JAVA_BIN_DIR=!JAVA_BIN_DIR:~0,-1!
    for %%k in ("!JAVA_BIN_DIR!") do set POTENTIAL_JAVA_HOME=%%~dpk
    set POTENTIAL_JAVA_HOME=!POTENTIAL_JAVA_HOME:~0,-1!
    
    echo Potential JAVA_HOME: !POTENTIAL_JAVA_HOME!
    
    REM Check if this is Java 21
    "%%i" -version 2>&1 | findstr "21.0" >nul
    if !ERRORLEVEL! EQU 0 (
        echo Found Java 21 at: !POTENTIAL_JAVA_HOME!
        set NEW_JAVA_HOME=!POTENTIAL_JAVA_HOME!
        goto :found_java21
    )
)

echo Could not automatically detect Java 21 installation.
echo Please manually set JAVA_HOME to your Java 21 installation directory.
pause
exit /b 1

:found_java21
echo.
echo Setting JAVA_HOME to: %NEW_JAVA_HOME%
set JAVA_HOME=%NEW_JAVA_HOME%

echo.
echo Verifying Java version with new JAVA_HOME...
"%JAVA_HOME%\bin\java.exe" -version

echo.
echo JAVA_HOME is now set correctly for this session.
echo Running Gradle build...

call "%CD%\gradlew.bat" clean build

echo.
echo Build completed with exit code: %ERRORLEVEL%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ BUILD SUCCESSFUL!
    echo Generated mod jar should be in build\libs\
    
    if exist build\libs\*.jar (
        echo.
        echo Found generated files:
        dir /b build\libs\*.jar
    )
) else (
    echo.
    echo ✗ BUILD FAILED!
    echo Check the error messages above.
)

pause
