@echo off
echo Starting Gradle build...
echo Current directory: %CD%

REM Check if gradlew.bat exists
if exist gradlew.bat (
    echo Found gradlew.bat
) else (
    echo ERROR: gradlew.bat not found
    pause
    exit /b 1
)

REM Check if gradle wrapper jar exists
if exist gradle\wrapper\gradle-wrapper.jar (
    echo Found gradle-wrapper.jar
) else (
    echo ERROR: gradle-wrapper.jar not found
    pause
    exit /b 1
)

REM Set JAVA_HOME if needed
echo Java version:
java -version

echo.
echo Running Gradle clean build...
echo.

REM Run gradle with full path
call "%CD%\gradlew.bat" clean build

echo.
echo Gradle build completed with exit code: %ERRORLEVEL%

if %ERRORLEVEL% EQU 0 (
    echo BUILD SUCCESSFUL!
    echo.
    echo Check build/libs/ for the generated mod jar file.
) else (
    echo BUILD FAILED!
    echo Check the error messages above.
)

pause
