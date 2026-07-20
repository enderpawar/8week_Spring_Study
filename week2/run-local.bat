@echo off
setlocal

rem Ignore deployment settings inherited from the terminal or IDE.
set "SPRING_PROFILES_ACTIVE="
set "SPRING_DATASOURCE_URL="
set "SPRING_DATASOURCE_USERNAME="
set "SPRING_DATASOURCE_PASSWORD="

rem Force this project to use the local MySQL study database.
set "DB_URL=jdbc:mysql://localhost:3306/study_room?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
set "DB_USERNAME=root"
set "DB_PASSWORD="

call "%~dp0gradlew.bat" bootRun --console=plain
set "EXIT_CODE=%ERRORLEVEL%"

endlocal & exit /b %EXIT_CODE%
