@echo off
set APP=base24framework

echo Building %APP% Message Adapter ...

IF EXIST adapter\postilion GOTO full_package

REM -- files are sitting inside the adapter subdir, not further down
javac -classpath "%POSTILIONDIR%\realtime\java\classes";..\..\build\classes;"%POSTILIONDIR%\testharness\java\classes";"%POSTILIONDIR%\%APP%\java\classes" .\adapter\*.java

IF (%1) == (COMPILEONLY) goto END

echo Copying %APP% Message Adapter to %APP% classpath
xcopy /S /Y /I ".\adapter\*.class" "%POSTILIONDIR%\%APP%\java\classes\postilion\realtime\%APP%\adapter\"
GOTO update_classpath


:full_package
javac -classpath "%POSTILIONDIR%\realtime\java\classes";"%POSTILIONDIR%\testharness\java\classes";"%POSTILIONDIR%\%APP%\java\classes" .\adapter\postilion\realtime\%APP%\adapter\*.java

IF (%1) == (COMPILEONLY) goto END

echo Copying %APP% Message Adapter to %APP% classpath
xcopy /S /Y /I ".\adapter\postilion\realtime\%APP%\adapter\*.class" "%POSTILIONDIR%\%APP%\java\classes\postilion\realtime\%APP%\adapter"


:update_classpath
REM echo Making changes to TestHarness .classpath file so that it can find the Message Adapter
REM echo. >>"%POSTILIONDIR%\testharness\java\.classpath.win32"
REM echo #include ..\..\%APP%\java\.classpath >>"%POSTILIONDIR%\testharness\java\.classpath.win32"

echo Making changes to TestHarness .classpath file so that it can find the Message Adapter
echo #include ..\..\realtime\java\.classpath>"%POSTILIONDIR%\testharness\java\.classpath.win32"
echo .\classes>>"%POSTILIONDIR%\testharness\java\.classpath.win32"
echo lib\antlr.jar>>"%POSTILIONDIR%\testharness\java\.classpath.win32"
echo. >>"%POSTILIONDIR%\testharness\java\.classpath.win32"
echo #include ..\..\%APP%\java\.classpath>>"%POSTILIONDIR%\testharness\java\.classpath.win32"
REM echo #include ..\..\base24framework\java\.classpath>>"%POSTILIONDIR%\testharness\java\.classpath.win32"
:END
