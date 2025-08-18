@echo off
echo 修复Gradle Wrapper...

REM 创建gradle/wrapper目录
if not exist "gradle\wrapper" mkdir "gradle\wrapper"

REM 尝试使用curl下载gradle-wrapper.jar
echo 尝试下载gradle-wrapper.jar...
curl -L -o "gradle\wrapper\gradle-wrapper.jar" "https://github.com/gradle/gradle/raw/v8.5/gradle/wrapper/gradle-wrapper.jar"

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ✓ gradle-wrapper.jar下载成功
) else (
    echo ⚠ 无法下载gradle-wrapper.jar，尝试使用PowerShell...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.5/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"
)

if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ✓ Gradle Wrapper修复完成
    echo 现在可以运行: gradlew.bat build
) else (
    echo ✗ 无法下载gradle-wrapper.jar
    echo 请手动下载或使用已安装的Gradle
)

pause
