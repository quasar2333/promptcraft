@echo off
echo Starting PromptCraft Minecraft Client...
echo ========================================

echo Java version:
java -version

echo.
echo Starting client (this may take several minutes)...
echo Press Ctrl+C to stop if needed.
echo.

gradlew.bat runClient

echo.
echo Client finished with exit code: %ERRORLEVEL%
pause
