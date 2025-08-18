@echo off
echo Building PromptCraft mod...

REM Clean previous build
call gradlew clean

REM Build the mod
call gradlew build

if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo.
    echo Running tests...
    
    REM Run with test flag
    call gradlew runClient -Dprompcraft.runTests=true
    
    echo.
    echo Build and test completed!
    echo Mod jar can be found in build/libs/
) else (
    echo Build failed!
    pause
)

pause
