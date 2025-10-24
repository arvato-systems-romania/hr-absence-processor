@echo off
REM === Print start time ===
echo Build started at: %date% %time%

REM === Set JAVA, MAVEN, and PROJECT_HOME variables ===
set "JAVA_HOME=C:\Software\Java\jdk-17"
set "MAVEN_HOME=C:\Software\Java\apache-maven-3.9.5"
set "PROJECT_HOME=C:\work\2025-HR\hr-absence-processor"

REM === Check if JAVA_HOME is a valid path ===
if not exist "%JAVA_HOME%" (
    echo JAVA_HOME path "%JAVA_HOME%" does not exist.
    exit /b 1
)

REM === Check if MAVEN_HOME is a valid path ===
if not exist "%MAVEN_HOME%" (
    echo MAVEN_HOME path "%MAVEN_HOME%" does not exist.
    exit /b 1
)

REM === Check if PROJECT_HOME is a valid path ===
if not exist "%PROJECT_HOME%" (
    echo PROJECT_HOME path "%PROJECT_HOME%" does not exist.
    exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

cd "%PROJECT_HOME%"

REM === Compile backend jar ===
cd backend
call mvn clean package
if errorlevel 1 (
    echo Maven build failed.
    exit /b 1
)

REM === Create release folder if not exists ===
cd ..
if exist release (
    del /q release\hr-absence-processor*
    for /d %%i in (release\*) do rmdir /s /q "%%i"
) else (
    mkdir release
)

REM === Copy backend jar to release ===
copy backend\target\hr-absence-processor-1.0.0.jar release\hr-absence-processor.jar

REM === Switch to frontend folder ===
cd frontend

REM === Check if npm is installed ===
where npm >nul 2>nul
if errorlevel 1 (
    echo npm is not installed. Please install Node.js and npm.
    exit /b 1
)

REM === Install npm dependencies ===
call npm install
if errorlevel 1 (
    echo npm install failed.
    exit /b 1
)

REM === Compile frontend (electron build) ===
call npm run electron-dist
if errorlevel 1 (
    echo Electron build failed.
    exit /b 1
)

REM === Copy frontend dist to release folder ===
cd ..
xcopy frontend\dist release\dist /E /I /Y

echo Delivery build complete.
echo Build finished at: %date% %time%
