@echo off
setlocal EnableDelayedExpansion
title The Abyss - NeoForge 26.3
cd /d "%~dp0"

rem ---- find a JDK 17+ to start Gradle (Gradle downloads Java 25 for the game itself) ----
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :menu
rem last match wins: JDK 21 (tested) is preferred over 23/25
for /d %%J in ("%USERPROFILE%\.jdks\*25*" "%USERPROFILE%\.jdks\*23*" "%USERPROFILE%\.jdks\*21*") do (
    if exist "%%~J\bin\java.exe" set "JAVA_HOME=%%~J"
)
if not defined JAVA_HOME (
    for /d %%J in ("%USERPROFILE%\.gradle\jdks\*25*") do (
        for /d %%K in ("%%~J" "%%~J\*") do if exist "%%~K\bin\java.exe" set "JAVA_HOME=%%~K"
    )
)
if not defined JAVA_HOME (
    echo Kein JDK 17+ gefunden. Bitte ein JDK installieren ^(z.B. Temurin 21^) oder JAVA_HOME setzen.
    pause
    exit /b 1
)
ma
:menu
echo.
echo ================= The Abyss - NeoForge 26.3 =================
echo  JDK fuer Gradle: %JAVA_HOME%
echo.
echo  1  Bauen (Mod-JAR nach build\libs)
echo  2  Client starten (Minecraft mit der Mod + JEI)
echo  3  Server starten (Dedicated Server)
echo  4  Automatischer Test (Client, Screenshots nach run-devtest\screenshots)
echo  5  Test-Server mit /abysstest
echo  6  Schneller Compile-Check (ohne Gradle)
echo  0  Beenden
echo.
set /p choice="Auswahl: "

if "%choice%"=="1" call gradlew.bat build & goto :done
if "%choice%"=="2" call gradlew.bat runClient & goto :done
if "%choice%"=="3" goto :server
if "%choice%"=="4" call gradlew.bat runDevTestClient & goto :done
if "%choice%"=="5" goto :testserver
if "%choice%"=="6" call gradlew.bat writeCompileClasspath & python -I tools\compile_check.py & goto :done
if "%choice%"=="0" exit /b 0
goto :menu

:server
call :eula run
call gradlew.bat runServer
goto :done

:testserver
call :eula run-devtest-server
echo Im Server-Fenster "abysstest all" eingeben, "stop" zum Beenden.
call gradlew.bat runDevTestServer
goto :done

:eula
if exist "%~1\eula.txt" findstr /c:"eula=true" "%~1\eula.txt" >nul && exit /b 0
echo.
echo Der Server benoetigt die Zustimmung zur Minecraft EULA: https://aka.ms/MinecraftEULA
set /p accept="EULA akzeptieren? (j/n): "
if /i "%accept%"=="j" (
    if not exist "%~1" mkdir "%~1"
    echo eula=true> "%~1\eula.txt"
)
exit /b 0

:done
echo.
pause
goto :menu
