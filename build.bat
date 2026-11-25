@echo off
setlocal
if not exist out mkdir out

javac --release 8 -encoding UTF-8 -sourcepath src -d out src\venuebooking\Main.java
if errorlevel 1 (
    echo Kompilasi gagal.
    exit /b 1
)

if not exist out\assets\images mkdir out\assets\images
xcopy /Y /Q assets\images\*.* out\assets\images\ >nul

jar --create --file VenueBooking.jar --main-class venuebooking.Main -C out .
if errorlevel 1 (
    echo Gagal membuat VenueBooking.jar.
    exit /b 1
)

echo Build selesai: VenueBooking.jar siap dijalankan dengan:
echo   java -jar VenueBooking.jar
echo.
echo Untuk membuat ulang dist\VenueBooking\VenueBooking.exe:
echo   mkdir build_input ^&^& copy /Y VenueBooking.jar build_input\ ^>nul
echo   rmdir /S /Q dist\VenueBooking 2^>nul
echo   jpackage --type app-image --name VenueBooking --input build_input --main-jar VenueBooking.jar --main-class venuebooking.Main --dest dist --add-modules java.desktop,jdk.localedata
echo   rmdir /S /Q build_input
