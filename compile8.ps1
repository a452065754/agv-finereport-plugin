$jars = (Get-ChildItem 'D:\FineReport_11.0\webapps\webroot\WEB-INF\lib\*.jar').FullName -join ';'
$proc = Start-Process -FilePath 'C:\Users\xudongdong\jdk8u432-b06\bin\javac.exe' -ArgumentList '-encoding','UTF-8','-cp',$jars,'-d','C:\Users\xudongdong','C:\Users\xudongdong\AgvDataSource.java' -NoNewWindow -Wait -PassThru
Write-Host "Exit code:" $proc.ExitCode
if ($proc.ExitCode -eq 0) {
    $bytes = Get-Content 'C:\Users\xudongdong\com\fr\data\AgvDataSource.class' -Encoding Byte -TotalCount 10
    Write-Host "Class version bytes:" $bytes[6] $bytes[7]
}
