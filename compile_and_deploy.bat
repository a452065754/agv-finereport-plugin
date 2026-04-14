@echo off
cd /d C:\Users\xudongdong

del AgvDataSource.class 2>nul
del com\fr\data\AgvDataSource.class 2>nul

set "LIB=D:\FineReport_11.0\webapps\webroot\WEB-INF\lib"
set "CP=%LIB%\fine-core-11.0.jar;%LIB%\fine-datasource-11.0.jar;%LIB%\fine-accumulator-11.0.jar;%LIB%\fine-activator-11.0.jar;%LIB%\fine-cbb-11.0.jar;%LIB%\fine-decision-11.0.jar;%LIB%\fine-decision-report-11.0.jar;%LIB%\fine-report-engine-11.0.jar;%LIB%\fine-schedule-11.0.jar;%LIB%\fine-schedule-report-11.0.jar;%LIB%\fine-swift-log-adaptor-11.0.jar;%LIB%\fine-third-11.0.jar;%LIB%\fine-webui-11.0.jar"

"C:\Users\xudongdong\.jdks\temurin-24\bin\javac.exe" -source 8 -target 8 -encoding UTF8 -Xlint:-options -cp "%CP%" -d C:\Users\xudongdong AgvDataSource.java

if %ERRORLEVEL% EQU 0 (
    echo Compile SUCCESS
    echo Copying to FineReport...
    copy /Y C:\Users\xudongdong\com\fr\data\AgvDataSource.class "D:\FineReport_11.0\webapps\webroot\WEB-INF\classes\com\fr\data\"
    echo Deploy done
) else (
    echo Compile FAILED
)
