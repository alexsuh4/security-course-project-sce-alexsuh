REM this script starts the MySql SQL server daemon 
REM compatible with win9x/winNT/win2k/winXP OS
@echo off
SET SQL_PATH="D:\apps\mysql-5.1.45-win32\bin\"
echo starting server
%SQL_PATH%\mysqld