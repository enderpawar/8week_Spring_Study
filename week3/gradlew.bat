@echo off
rem Reuse the Gradle wrapper checked in for week1 while building the week3 project.
call "%~dp0..\week1\gradlew.bat" -p "%~dp0." %*
