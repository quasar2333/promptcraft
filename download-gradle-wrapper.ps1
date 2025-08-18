# PowerShell script to download Gradle Wrapper
Write-Host "Downloading Gradle Wrapper..." -ForegroundColor Green

$gradleVersion = "8.10.2"
$wrapperUrl = "https://github.com/gradle/gradle/raw/v$gradleVersion/gradle/wrapper/gradle-wrapper.jar"
$wrapperPath = "gradle/wrapper/gradle-wrapper.jar"

try {
    # Create directory if it doesn't exist
    $wrapperDir = Split-Path $wrapperPath -Parent
    if (!(Test-Path $wrapperDir)) {
        New-Item -ItemType Directory -Path $wrapperDir -Force
    }
    
    # Download the wrapper jar
    Write-Host "Downloading from: $wrapperUrl"
    Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperPath -UseBasicParsing
    
    if (Test-Path $wrapperPath) {
        $fileSize = (Get-Item $wrapperPath).Length
        Write-Host "Successfully downloaded gradle-wrapper.jar ($fileSize bytes)" -ForegroundColor Green
        
        # Try to run gradle wrapper to verify
        Write-Host "Testing Gradle Wrapper..." -ForegroundColor Yellow
        & ".\gradlew.bat" --version
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Gradle Wrapper is working correctly!" -ForegroundColor Green
        } else {
            Write-Host "Gradle Wrapper test failed, but file was downloaded." -ForegroundColor Yellow
        }
    } else {
        Write-Host "Failed to download gradle-wrapper.jar" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Error downloading Gradle Wrapper: $($_.Exception.Message)" -ForegroundColor Red
    
    # Try alternative download method
    Write-Host "Trying alternative download method..." -ForegroundColor Yellow
    try {
        $webClient = New-Object System.Net.WebClient
        $webClient.DownloadFile($wrapperUrl, $wrapperPath)
        Write-Host "Alternative download successful!" -ForegroundColor Green
    } catch {
        Write-Host "Alternative download also failed: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "Gradle Wrapper setup complete!" -ForegroundColor Green
