@echo off
setlocal EnableDelayedExpansion
title The Abyss - NeoForge und Fabric 26.3
cd /d "%~dp0"

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" goto :menu
for /d %%J in ("%USERPROFILE%\.gradle\jdks\*25*") do (
    for /d %%K in ("%%~J" "%%~J\*") do if exist "%%~K\bin\java.exe" set "JAVA_HOME=%%~K"
)
if not defined JAVA_HOME (
    for /d %%J in ("%USERPROFILE%\.jdks\*25*" "%USERPROFILE%\.jdks\*23*" "%USERPROFILE%\.jdks\*21*") do (
        if exist "%%~J\bin\java.exe" set "JAVA_HOME=%%~J"
    )
)
if not defined JAVA_HOME (
    echo Kein JDK 21+ gefunden. Bitte ein JDK installieren ^(z.B. Temurin 25^) oder JAVA_HOME setzen.
    pause
    exit /b 1
)

:menu
cls
set "cur="
for /f "tokens=2 delims==" %%V in ('findstr /b "mod_version" gradle.properties') do set "cur=%%V"
echo ==========================================================
echo     The Abyss !cur!  (Minecraft 26.3, NeoForge und Fabric)
echo ==========================================================
echo  JDK: %JAVA_HOME%
echo.
echo   1  Minecraft starten - NeoForge
echo   2  Minecraft starten - Fabric
echo.
echo   3  .jar bauen - NeoForge
echo   4  .jar bauen - Fabric
echo   5  .jar bauen - NeoForge und Fabric
echo.
echo   6  Automatischer Test (NeoForge Client, Screenshots nach run-devtest\screenshots)
echo   7  Test-Server mit /abysstest (NeoForge)
echo   8  Schneller Compile-Check (alle Module, ohne Gradle)
echo.
echo   0  Beenden
echo.
set "choice="
set /p "choice=Auswahl: "
if "%choice%"=="1" set "loader=neoforge" & goto run
if "%choice%"=="2" set "loader=fabric" & goto run
if "%choice%"=="3" set "loaders=neoforge" & goto build
if "%choice%"=="4" set "loaders=fabric" & goto build
if "%choice%"=="5" set "loaders=neoforge fabric" & goto build
if "%choice%"=="6" call "%~dp0gradlew.bat" :neoforge:runDevTestClient & goto done
if "%choice%"=="7" goto testserver
if "%choice%"=="8" goto check
if "%choice%"=="0" exit /b 0
goto menu

:run
echo.
echo Kompiliere den aktuellen Stand und starte Minecraft mit !loader! ...
if "!loader!"=="neoforge" (echo Spielordner: run) else (echo Spielordner: run-fabric)
call "%~dp0gradlew.bat" :!loader!:runClient
goto done

:build
echo.
set "ver="
set /p "ver=Versionsnummer [Enter = !cur!]: "
if not defined ver set "ver=!cur!"
echo(!ver!| findstr /r /x "[0-9][0-9A-Za-z.+-]*" >nul
if errorlevel 1 (
    echo Ungueltige Version "!ver!" - erlaubt sind Zahlen, Buchstaben, Punkt, Plus und Minus, z.B. 1.1.0-beta
    pause
    goto menu
)
powershell -NoProfile -Command "$p='gradle.properties'; (Get-Content -Raw $p) -replace '(?m)^mod_version=.*$', 'mod_version=!ver!' | Set-Content -NoNewline -Encoding ascii $p"
set "tasks="
for %%L in (!loaders!) do set "tasks=!tasks! :%%L:build"
echo.
echo Baue The Abyss !ver! fuer: !loaders! ...
call "%~dp0gradlew.bat" !tasks! -x test -Pmod_version=!ver!
if errorlevel 1 (
    echo.
    echo BUILD FEHLGESCHLAGEN - siehe Ausgabe oben.
    pause
    goto menu
)
if not exist "dist" mkdir "dist"
echo.
for %%L in (!loaders!) do (
    if "%%L"=="neoforge" (set "jar=neoforge\build\libs\theabyss-!ver!.jar") else (set "jar=fabric\build\libs\theabyss-fabric-!ver!.jar")
    if exist "!jar!" (
        copy /y "!jar!" "dist\" >nul
        echo Fertig: dist\!jar:*libs\=!
    ) else (
        echo !jar! wurde nicht gefunden.
    )
)
echo.
echo NeoForge: in den mods Ordner einer NeoForge 26.3 Installation kopieren, zusammen mit GeckoLib.
echo Fabric:   in den mods Ordner einer Fabric 26.3 Installation kopieren, zusammen mit Fabric API und GeckoLib.
start "" explorer "dist"
pause
goto menu

:testserver
call :eula run-devtest-server
echo Im Server-Fenster "abysstest all" eingeben, "stop" zum Beenden.
call "%~dp0gradlew.bat" :neoforge:runDevTestServer
goto done

:check
call "%~dp0gradlew.bat" :common:writeCompileClasspath :neoforge:writeCompileClasspath :fabric:writeCompileClasspath -q
for %%M in (common neoforge fabric) do (
    echo.
    echo === %%M ===
    python -I tools\port_check.py %%M --summary
)
goto done

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
goto menu
