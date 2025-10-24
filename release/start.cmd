@echo off
REM === Check if java.exe is in PATH ===
where java >nul 2>nul
if errorlevel 1 (
    echo java.exe not found in PATH. Setting JAVA_HOME to default.
    set "JAVA_HOME=C:\Software\Java\jdk-17"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
) else (
    echo java.exe found in PATH.
)

REM === Start the backend jar ===
if exist hr-absence-processor.jar (
    echo Starting hr-absence-processor.jar ...
    start "HR Absence Processor Backend" java -jar hr-absence-processor.jar
) else (
    echo hr-absence-processor.jar not found!
    exit /b 1
)

REM === Wait for 5 seconds ===
timeout /t 5 /nobreak >nul

REM === Start the Electron frontend if exists ===
if exist "dist\HR Absence Processor 0.1.0.exe" (
    echo Starting HR Absence Processor 0.1.0.exe ...
    start "HR Absence Processor Frontend" "dist\HR Absence Processor 0.1.0.exe"
) else (
    echo "dist\HR Absence Processor 0.1.0.exe" not found!
)

