@echo off

setlocal

:: Determine if Java is in path
where java > nul
if %ERRORLEVEL% == 0 goto :has_java

echo Java not found in PATH
goto :eof

:has_java

:: Compile project
call mvn compile

:: Get major Java version
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
)
set JAVAVER=%JAVAVER:"=%

for /f "delims=. tokens=1-3" %%v in ("%JAVAVER%") do (
	set JAVAVER_MAJOR=%%v
)

:: Determine which command to use to generate the JNI headers based on the JDK version
if [%JAVAVER_MAJOR%] == [1] (
	:: Running Java 1.8 or below
	set JNIGEN_COMMAND=javah -jni
) else if %JAVAVER_MAJOR% geq 9 (
	:: Running Java 9 or later
	set JNIGEN_COMMAND=javac -h
)

:: Generate JNI headers for XInputNatives and XInputNatives14 under src/native/windows/include
%JNIGEN_COMMAND% -classpath target\classes\ -d src\native\windows\include com.github.strikerx3.jxinput.natives.XInputNatives
if %ERRORLEVEL% neq 0 goto :jni_failed
%JNIGEN_COMMAND% -classpath target\classes\ -d src\native\windows\include com.github.strikerx3.jxinput.natives.XInputNatives14
if %ERRORLEVEL% neq 0 goto :jni_failed

echo JNI headers generated
goto :eof

:jni_failed
echo Could not generate JNI headers. See errors above.

endlocal
