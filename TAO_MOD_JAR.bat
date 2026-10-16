@echo off
setlocal EnableExtensions
cd /d "%~dp0"
title Hytur Tu Tien - Fabric 1.20.1 - Build JAR

echo ===========================================
echo  HYTUR TU TIEN - TAO FILE MOD .JAR

echo ===========================================
echo.
where java >nul 2>&1
if errorlevel 1 (
  if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" goto java_ready
  )
  echo [LOI] Khong tim thay Java tren Windows.
  echo Cai JDK 17, sau do chay lai file nay.
  goto failed
)
:java_ready
java -version 2>&1
echo.
if not exist "_gradle\gradle-8.8\bin\gradle.bat" (
  echo [1/3] Tai Gradle 8.8 (can mang Internet) ...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue'; $url='https://services.gradle.org/distributions/gradle-8.8-bin.zip'; Invoke-WebRequest -Uri $url -OutFile 'gradle-8.8-bin.zip'; Expand-Archive -Force -Path 'gradle-8.8-bin.zip' -DestinationPath '_gradle'; Remove-Item 'gradle-8.8-bin.zip'"
  if errorlevel 1 (
    echo [LOI] Khong tai duoc Gradle. Kiem tra Internet va thu lai.
    goto failed
  )
)
echo [2/3] Dang bien dich Fabric 1.20.1 (lan dau co the mat nhieu phut) ...
call "_gradle\gradle-8.8\bin\gradle.bat" --no-daemon clean remapJar
if errorlevel 1 (
  echo [LOI] Build khong thanh cong. Chup man hinh loi gui ChatGPT de sua.
  goto failed
)
if not exist "build\libs\hytur-tu-tien-2.0.1.jar" (
  echo [LOI] Khong tim thay file JAR o build\libs\.
  goto failed
)
echo [3/3] Sao chep file JAR ra thu muc hien tai ...
copy /Y "build\libs\hytur-tu-tien-2.0.1.jar" "hytur-tu-tien-2.0.1.jar" >nul
if errorlevel 1 goto failed
echo.
echo ===========================================
echo    THANH CONG!
echo  hytur-tu-tien-2.0.1.jar
echo  Hay chep file vao HyturCraft\mods

echo ===========================================
explorer "%CD%"
pause
exit /b 0
:failed
echo.
echo Neu bi loi, chup anh phan thong bao mau do/LOI va gui lai.
pause
exit /b 1
