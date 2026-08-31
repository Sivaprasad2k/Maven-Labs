@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%
@if "%MAVEN_BATCH_PAUSE%" == "on" set PAUSE=%MAVEN_BATCH_PAUSE%
@setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
@set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties
if not exist "%WRAPPER_PROPERTIES%" (
  echo Error: Could not find %WRAPPER_PROPERTIES%
  exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%a in ("%WRAPPER_PROPERTIES%") do (
  if "%%a"=="distributionUrl" set MAVEN_URL=%%b
)

if "%MAVEN_URL%"=="" (
  echo Error: distributionUrl not found in %WRAPPER_PROPERTIES%
  exit /b 1
)

for %%F in ("%MAVEN_URL%") do set MAVEN_ZIP=%%~nxF
set MAVEN_DIR_NAME=%MAVEN_ZIP:-bin.zip=%

set MAVEN_USER_HOME=%USERPROFILE%\.m2
set WRAPPER_HOME=%MAVEN_USER_HOME%\wrapper\dists\%MAVEN_DIR_NAME%
set MAVEN_HOME=%WRAPPER_HOME%\%MAVEN_DIR_NAME%

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  if not exist "%WRAPPER_HOME%\apache-maven-3.9.6\bin\mvn.cmd" (
    echo Downloading Maven from %MAVEN_URL% ...
    if not exist "%WRAPPER_HOME%" mkdir "%WRAPPER_HOME%"
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%MAVEN_URL%', '%WRAPPER_HOME%\%MAVEN_ZIP%')"
    if errorlevel 1 (
      echo Error downloading Maven
      exit /b 1
    )
    echo Unpacking Maven ...
    powershell -Command "Expand-Archive -Path '%WRAPPER_HOME%\%MAVEN_ZIP%' -DestinationPath '%WRAPPER_HOME%' -Force"
    if errorlevel 1 (
      echo Error unpacking Maven
      exit /b 1
    )
    if exist "%WRAPPER_HOME%\%MAVEN_ZIP%" del "%WRAPPER_HOME%\%MAVEN_ZIP%"
  )
)

set MAVEN_CMD="%MAVEN_HOME%\bin\mvn.cmd"
if not exist %MAVEN_CMD% (
  for /d %%D in ("%WRAPPER_HOME%\apache-maven-*") do (
    if exist "%%D\bin\mvn.cmd" set MAVEN_CMD="%%D\bin\mvn.cmd"
  )
)

%MAVEN_CMD% %*
