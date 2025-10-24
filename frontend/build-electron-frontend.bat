@echo off
REM Build and run or package the React frontend as a native Electron app

REM Change to the frontend directory
cd /d %~dp0

REM Install dependencies
echo Installing dependencies...
npm install
IF %ERRORLEVEL% NEQ 0 (
  echo npm install failed.
  exit /b %ERRORLEVEL%
)

IF "%1"=="dist" (
  REM Build and package as Windows .exe
  echo Building and packaging Electron app as .exe...
  npm run electron-dist
  IF %ERRORLEVEL% NEQ 0 (
    echo Electron .exe build failed.
    exit /b %ERRORLEVEL%
  )
  echo .exe and installer are in the dist/ folder.
) ELSE (
  REM Build the React app and start Electron for development
  echo Building and launching Electron app...
  npm run electron
  IF %ERRORLEVEL% NEQ 0 (
    echo Electron app failed to start.
    exit /b %ERRORLEVEL%
  )
)
