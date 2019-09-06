:: 
:: The latest copy of this can be found at: 
:: //depot_us/implementationteam/templates/autotestrun/run.cmd
::
:: This script should be saved in the root folder of a particular
:: testsuite (e.g. testharness\run.cmd or testengine\run.cmd or junit\run.cmd)
:: and helps to automate testing cycles of S1 apps. The aim of 
:: this script is to standardize testing for all node interfaces etc.
:: This template takes into account RTFW4 / RTFW5 implementations by 
:: the use of configurable parameters (see the User Defined Values). 
::
:: Note that this script will automatically copy over the
:: required output as per the US Policies & Procedures document. 
:: The developer can then submit these as part of their submission 
:: for proof of testing. 
:: 
@ECHO OFF

REM When using ECHO, the period ('.') is used to prevent confusion with the attempt 
REM to output ON or OFF rather than turn input and prompt displaying on and off. 

:: ---------------------------------------------------------------------------
:: -- User Defined Values
:: ---------------------------------------------------------------------------

SET "APP=DvcBogota"

REM Usually set to the lowercase of the above - seems like DOS cmd files don't have easy lower() fn :(
SET "APPLOWERCASE=dvcbogota"

REM Used to start /indicate what service is needed to ensure the testsuite runs successfully.
REM Possible Values: 0 (It runs as itself i.e. %APP$%); 1 (Runs through the TM service); 2 (Needs no services).
SET "SERVICE=%APP%"

REM Used to run the Version command. If it's the same as the app name, then leave unchanged.
REM Else modify it as needed i.e. perhaps it's an embedded name e.g. integrationdrivers.myapp
SET "PACKAGE=%APPLOWERCASE%"

REM Used to indicate that the app being tested makes use of another app and we want to store the version of required app as part of test evidence.
REM Values: No or name of required app in lowercase (used to get the version)
REM e.g. "visabase12" or if dependent on 2 apps then use "visabase12;fleetframework".
SET "PACKAGE_DEPENDANT=No"

REM Used to indicate that this is a office application. Values: Yes or No.
SET "OFFICEAPP=No"

REM Possible values: 'TestEngine' or 'TestHarness' or 'Junit'
SET "TESTSOFTWARE=TestHarness"

REM Possible values: 'None' or name of the class used to run entire junit testsuite
SET "JUNITMAINFILE=None"

REM TESTHARNESS_Version can have values: either '1' or '5'
SET "TESTHARNESS_Version=5"

REM RTFW_Version can have values: either '4' or '5'
SET "RTFW_Version=5"

REM NEEDPROXYORADAPTER should have values: either 'Yes' or 'No'
SET "NEEDPROXYORADAPTER=Yes"

REM ADDITIONALDBLOAD should have values: either 'No' or 
REM the name of the database e.g. 'postcard' or 'postcheck'
REM If an additional db load is required other than 
REM postilion/realtime note that the dir file structure must be
REM config\postilion and config\<name of other db>
SET "ADDITIONALDBLOAD=realtime"

REM CUSTOMSQLFILENAME should specify the full patch of a custom SQL file that needs to be run prior to your test run.
REM Possible values: 'No' or the name of the file e.g. 'mop_up.sql"
REM This excludes the pre_load and post_load SCR scripts that are found in the postcard directory
REM as that functionality is already available in the script.
SET "CUSTOMSQLFILENAME=No"

REM When this is set to No, then the Active/Active trace files will not be 
REM included in the test evidence.
SET "ACTIVE_ACTIVE_TESTS=No"

:: -- ********************************************************************* --
:: DO NOT CHANGE THINGS BELOW THIS LINE - IF YOU FIND A BUG - PLEASE REPORT IT 
:: TO YOUR TEAM LEAD SO THE FIX CAN BE INCORPORATED INTO OUR GLOBAL TEAM COPY. 
:: -- ********************************************************************* --

:: ---------------------------------------------------------------------------
:: -- Main Method
:: ---------------------------------------------------------------------------

REM Check parameters supplied to this script
IF "%1" == "--help" GOTO FuncPrintHelp


call:FuncInit
REM If in debug mode, then we need to skip over configuration setup and go straight to the testcases
IF NOT "%1" == "1"  (
	call:FuncStopServices
	call:FuncLoadTestDatabaseConfig
	call:FuncBuildProxyAdapter
	call:FuncStartServices
)

call:FuncRunTests
call:FuncGetVersionInfo
call:FuncCopyTestTraceEvidence
call:FuncPrintNotesToDeveloper

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function Init sets various parameters needed by the script
:: ---------------------------------------------------------------------------
:FuncInit
@ECHO.- Function Init start ...
SET "OUTPUTFOLDER=output"
SET "TESTOUTPUT=%OUTPUTFOLDER%\test_output"

IF "%SERVICE%" == "0" ( 
	SET "TRACEPATH=%POSTILIONDIR%\Trace\%APP%"
) ELSE IF "%SERVICE%" == "1" (
	SET "TRACEPATH=%POSTILIONDIR%\Trace\Transaction Manager"
) ELSE IF "%SERVICE%" == "2" (
	SET "TRACEPATH=NONE"
) ELSE (
	SET "TRACEPATH=%POSTILIONDIR%\Trace\%SERVICE%"
)

SET TESTENGINE_TESTS_FILENAME_CONVENTION=TestCase

REM Need a variable to store the db name (set to 'realtime' or 'postilion') based on the TESTHARNESS_Version parameter
IF "%TESTHARNESS_Version%" == "1" SET POSTILION_DATA_SOURCE=postilion
IF "%TESTHARNESS_Version%" == "5" SET POSTILION_DATA_SOURCE=realtime

IF EXIST %OUTPUTFOLDER% (
	@CALL RMDIR %OUTPUTFOLDER% /S /Q
)
MKDIR %OUTPUTFOLDER%

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function StopServices
:: ---------------------------------------------------------------------------
:FuncStopServices
@ECHO.- Function ServicesStop start ...
REM If no services are needed, skip this section
IF "%SERVICE%" == "2" goto:eof

IF "%SERVICE%" == "0" (
	net stop %APP%
) ELSE IF NOT "%SERVICE%" == "1" (
	net stop %SERVICE%
)
@CALL net stop "Transaction Manager"
@CALL net stop "Certificate Manager"
IF EXIST "%TRACEPATH%" (
	@CALL RMDIR "%TRACEPATH%" /S /Q
)
goto:eof

:: ---------------------------------------------------------------------------
:: -- Function StartServices
:: ---------------------------------------------------------------------------
:FuncStartServices
REM If no services are needed, skip this section
IF "%SERVICE%" == "2" goto:eof

@ECHO.- Function ServicesStart start ...

net start "Certificate Manager"

net start "Transaction Manager"

IF "%SERVICE%" == "0" (
	net start %APP%
) ELSE IF NOT "%SERVICE%" == "1" (
	net start %SERVICE%
)

@ECHO.Sleeping for a few secs to give the services time to start up
@ECHO.
ping localhost -n 10 > nul

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function BuildProxyAdapter
:: ---------------------------------------------------------------------------
:FuncBuildProxyAdapter
IF "No" == "%NEEDPROXYORADAPTER%" goto:eof
@ECHO.- Function BuildProxyAdapter start ...

IF "5" == "%RTFW_Version%" @CALL build_adapter.cmd
IF "4" == "%RTFW_Version%" @CALL build_proxy.cmd

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function LoadTestDatabaseConfig
:: ---------------------------------------------------------------------------
:FuncLoadTestDatabaseConfig
@ECHO.- Function LoadTestDatabaseConfig start ...

REM Exec custom db prep requirements needed for tests to work
IF NOT "No" == "%CUSTOMSQLFILENAME%" (
	@ECHO.- Function LoadTestDatabaseConfig running custom SQL
	@CALL osql -d postcard -E -i %CUSTOMSQLFILENAME%
)

IF "TestEngine" == "%TESTSOFTWARE%" call:FuncLoadTestEngineDatabaseConfig
IF "Junit" == "%TESTSOFTWARE%" call:FuncLoadJunitDatabaseConfig
IF "TestHarness" == "%TESTSOFTWARE%" call:FuncLoadTestHarnessDatabaseConfig

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function LoadTestHarnessDatabaseConfig
:: ---------------------------------------------------------------------------
:FuncLoadTestHarnessDatabaseConfig
@ECHO.- Function LoadTestHarnessDatabaseConfig start ...

FOR /d %%f in (config*) DO SET TMP_CONFIG_DIR_NAME=%%f
IF EXIST %TMP_CONFIG_DIR_NAME%\base (
	@ECHO.Found a base subdir in the config dir
	SET "CONFIG_DIR_NAME=%TMP_CONFIG_DIR_NAME%\base"
) ELSE (
	SET "CONFIG_DIR_NAME=%TMP_CONFIG_DIR_NAME%"
)

IF "%ADDITIONALDBLOAD%" == "No" GOTO lbl_test_db_config_std_load

:lbl_testharness_db_config_additional_db
IF "%TESTHARNESS_Version%" == "5" GOTO lbl_testharness_db_config_additional_db_testharness5

:lbl_testharness_db_config_additional_db_testharness1
@ECHO.Running: 'TestHarness 1 LoadDbConfig for datasource: %POSTILION_DATA_SOURCE%_%ADDITIONALDBLOAD%'
IF EXIST %CONFIG_DIR_NAME%\postcard\preload.scr (
	@ECHO.Found a pre load script
	@CALL osql -E -i %CONFIG_DIR_NAME%\postcard\preload.scr
)
@CALL loaddbconfig %CONFIG_DIR_NAME%\%ADDITIONALDBLOAD% -ds %POSTILION_DATA_SOURCE%_%ADDITIONALDBLOAD%
IF EXIST %CONFIG_DIR_NAME%\postcard\postload.scr (
	@ECHO.Found a post load script
	@CALL osql -E -i %CONFIG_DIR_NAME%\postcard\postload.scr
)

@ECHO.Running: 'Testharness 1 LoadDbConfig for datasource: %POSTILION_DATA_SOURCE%'
@CALL loaddbconfig %CONFIG_DIR_NAME%\%POSTILION_DATA_SOURCE% -ds %POSTILION_DATA_SOURCE%

goto:eof

:lbl_testharness_db_config_additional_db_testharness5
IF EXIST %CONFIG_DIR_NAME%\postcard\preload.scr (
	@ECHO.Found a pre load script
	@CALL osql -E -i %CONFIG_DIR_NAME%\postcard\preload.scr
)

@ECHO.Running: 'TestHarness 5 LoadDbConfig for datasource: %ADDITIONALDBLOAD%'
@CALL loaddbconfig %ADDITIONALDBLOAD% %CONFIG_DIR_NAME%\%ADDITIONALDBLOAD%

IF EXIST %CONFIG_DIR_NAME%\postcard\postload.scr (
	@ECHO.Found a post load script
	@CALL osql -E -i %CONFIG_DIR_NAME%\postcard\postload.scr
)

@ECHO.Running: 'TestHarness 5 LoadDbConfig for datasource: %POSTILION_DATA_SOURCE%'
@CALL loaddbconfig %POSTILION_DATA_SOURCE% %CONFIG_DIR_NAME%\%POSTILION_DATA_SOURCE%

goto:eof

:lbl_test_db_config_std_load
@ECHO.Running: LoadDbConfig from folder '%CONFIG_DIR_NAME%' into datasource: '%POSTILION_DATA_SOURCE%'
IF "%TESTHARNESS_Version%" == "5" @CALL loaddbconfig %POSTILION_DATA_SOURCE% %CONFIG_DIR_NAME%
IF "%TESTHARNESS_Version%" == "1" @CALL loaddbconfig %CONFIG_DIR_NAME% -ds %POSTILION_DATA_SOURCE%

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function LoadJunitDatabaseConfig
:: ---------------------------------------------------------------------------
:FuncLoadJunitDatabaseConfig
@ECHO.- Function LoadJunitDatabaseConfig start ...
:lbl_junit_db_config
REM Cater for this scenario when necessary...
goto:eof

:: ---------------------------------------------------------------------------
:: -- Function LoadTestEngineDatabaseConfig
:: ---------------------------------------------------------------------------
:FuncLoadTestEngineDatabaseConfig
@ECHO.- Function LoadTestEngineDatabaseConfig start ...
REM There should only be one setup file, we're doing a loop anyway just to look up the exact filename.
FOR %%f in (*TestSetup.py) DO SET TESTENGINE_SETUP_FILENAME=%%f
@ECHO.Running: '%TESTENGINE_SETUP_FILENAME%'
@CALL %TESTENGINE_SETUP_FILENAME%

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function RunTests
:: ---------------------------------------------------------------------------
:FuncRunTests
@ECHO.Running %TESTSOFTWARE% test cases. Output is saved to the folder '%CD%\%OUTPUTFOLDER%\'.
IF "TestHarness" == "%TESTSOFTWARE%" call:FuncRunTestHarnessTests
IF "TestEngine" == "%TESTSOFTWARE%" call:FuncRunTestEngineTests
IF "Junit" == "%TESTSOFTWARE%" call:FuncRunJunitTests

goto:eof 

ping 127.0.0.1 -n 10 > nul


:: ---------------------------------------------------------------------------
:: -- Function RunTestHarnessTests
:: ---------------------------------------------------------------------------
:FuncRunTestHarnessTests
REM Need a variable to store the name of the test case folder (e.g. testcases or cases)
IF NOT EXIST *cases (
	SET "TESTCASES_DIR_NAME=."
) ELSE (
	FOR /d %%f in (*cases) DO SET TESTCASES_DIR_NAME=%%f
)

@ECHO.Running %TESTSOFTWARE% testsuite '%TESTCASES_DIR_NAME%'
@CALL COPY NUL %TESTOUTPUT%.txt /Y
START "tailing output for %TESTOUTPUT%.txt" gtail -f %TESTOUTPUT%.txt
@ECHO.#include ..\..\%APPLOWERCASE%\java\.classpath >>"%POSTILIONDIR%\testharness\java\.classpath.win32"
@CALL runtestsuite --register-scribes "postilion.testharness.log.testdoc.Scribe,postilion.testharness.log.stdout.Scribe" %TESTCASES_DIR_NAME% >> %TESTOUTPUT%.txt
:: Close testsuite´s window
@CALL taskkill /F /FI "WINDOWTITLE eq tailing output for*"
goto:eof

:: ---------------------------------------------------------------------------
:: -- Function RunJunitTests
:: ---------------------------------------------------------------------------
:FuncRunJunitTests
@CALL build_junit.cmd REM need to build the junit class files
@ECHO.Calling %TESTSOFTWARE% testsuite 'postilion.%APPLOWERCASE%.%JUNITMAINFILE%'
START "tailing output for %TESTOUTPUT%.txt" gtail -f %TESTOUTPUT%.txt
@CALL postjava -cp %WINDIR%\java\classes;%POSTILIONDIR%\core\java\lib\junit.jar postilion.%APPLOWERCASE%.%JUNITMAINFILE% > %TESTOUTPUT%.txt
@TYPE %TESTOUTPUT%.txt

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function RunTestEngineTests
:: ---------------------------------------------------------------------------
:FuncRunTestEngineTests
IF NOT EXIST *%TESTENGINE_TESTS_FILENAME_CONVENTION%*.py goto:eof
FOR /f %%a IN ('dir *%TESTENGINE_TESTS_FILENAME_CONVENTION%*.py /b /o:-n') DO (
	@CALL ClearStoreAndForward.py
	@ECHO.Running %TESTSOFTWARE% file %%a and saving output to %TESTOUTPUT%_%%~na.txt
	@CALL COPY NUL %TESTOUTPUT%_%%~na.txt /Y
	START "tailing output for %%a" gtail -f %TESTOUTPUT%_%%~na.txt
	@CALL %%a >> %TESTOUTPUT%_%%~na.txt
)
@ECHO.Looking for errors in %OUTPUTFOLDER%\*%TESTENGINE_TESTS_FILENAME_CONVENTION%*.txt
FOR %%f IN (%OUTPUTFOLDER%\*%TESTENGINE_TESTS_FILENAME_CONVENTION%*.txt) DO (
	FINDSTR /B /C:"SUMMARY: 0 test cases failed" %%f > NUL
	IF errorlevel=1 (
		@ECHO.
		@ECHO.****** 
		@ECHO.TestEngine Cases Failed
		@ECHO.****** 
		@ECHO.
		goto:eof
	)
)
@ECHO.TestEngine Test Cases Completed Successfully 
goto:eof

:: ---------------------------------------------------------------------------
:: -- Function CopyFilesInDirectory takes 2 parameters:
:: -- %~1 = Directory containing the html files to be copied
:: -- %~2 = Directory to which the files should be copied
:: ---------------------------------------------------------------------------
:FuncCopyFilesInDirectory
IF NOT "%~1" == "None" (
	@ECHO.Copying files from %~1 into folder %~2
	for /f "delims=|" %%a in ('dir /b /ad "%~1"') do (
		REM @ECHO.- Copying file %%a
		copy "%~1\%%a\*.html" %~2\ /Y 
	)

	@CALL DEL "%~2\Hardware Security*.html"

	IF "%ACTIVE_ACTIVE_TESTS%" == "No" (
		@CALL DEL "%~2\*Active active*.html"
	)
)
goto:eof

:: ---------------------------------------------------------------------------
:: -- Function CopyTestTraceEvidence
:: ---------------------------------------------------------------------------
:FuncCopyTestTraceEvidence

REM Note have to use double quotes around parameters containing spaces i.e. trace path
call:FuncCopyFilesInDirectory "%TRACEPATH%",%OUTPUTFOLDER%

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function GetVersionInfo
:: ---------------------------------------------------------------------------
:FuncGetVersionInfo
@ECHO.- Function GetVersionInfo start ...

@ECHO.Retrieving relevant application versions ... > %OUTPUTFOLDER%\version.txt
IF "%OFFICEAPP%" == "Yes" (
	@ECHO.Retrieving Office version ... >> %OUTPUTFOLDER%\version.txt
	@CALL jview postilion.office.Version >> %OUTPUTFOLDER%\version.txt
) ELSE IF "%RTFW_Version%" == "4" (
	@CALL postjava postilion.%PACKAGE%.Version >> %OUTPUTFOLDER%\version.txt
	IF "%SERVICE%" == "1" (	REM i.e. we only get the RTFW version if it runs through TM)
		@ECHO.Retrieving Realtime v4x version ... >> %OUTPUTFOLDER%\version.txt
		REM For standalone apps, we'll print out the RTFW version
		REM since they most prob won't have trace file functionality
		REM (i.e. trace files always show the RTFW version with all patches)
		@CALL postjava postilion.core.Version >> %OUTPUTFOLDER%\version.txt
	)
) ELSE (
	@CALL postjava_%APPLOWERCASE%.exe postilion.realtime.%PACKAGE%.Version >> %OUTPUTFOLDER%\version.txt
	IF "%SERVICE%" == "1" (
		@ECHO.Retrieving Realtime v5x version ... >> %OUTPUTFOLDER%\version.txt
		@CALL postjava.exe postilion.realtime.Version >> %OUTPUTFOLDER%\version.txt
	)
)

IF NOT "%PACKAGE_DEPENDANT%" == "No" (
	FOR %%a IN (%PACKAGE_DEPENDANT%) DO (
		@ECHO.Retrieving %%a version ... >> %OUTPUTFOLDER%\version.txt
		IF "%RTFW_Version%" == "4" (
			@CALL postjava postilion.%%a.Version >> %OUTPUTFOLDER%\version.txt
		) ELSE (
			@CALL postjava_%%a.exe postilion.realtime.%%a.Version >> %OUTPUTFOLDER%\version.txt
		)
	)
)

IF "%ADDITIONALDBLOAD%" == "postcard" (
	@ECHO.Retrieving PostCard version ... >> %OUTPUTFOLDER%\version.txt
	IF "%RTFW_Version%" == "4" (
		@CALL postjava postilion.postcard.Version >> %OUTPUTFOLDER%\version.txt
	) ELSE (
		@CALL postjava_postcard postilion.postcard.Version >> %OUTPUTFOLDER%\version.txt
	)
)

@ECHO.Retrieving SQL version ... >> %OUTPUTFOLDER%\version.txt
@CALL osql -E -Q"SELECT @@VERSION" >> %OUTPUTFOLDER%\version.txt

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function PrintNotesToDeveloper
:: ---------------------------------------------------------------------------
:FuncPrintNotesToDeveloper

@ECHO.
@ECHO.------------------- WARNING: ---------------------------
@ECHO.LOOK AT TEST RESULTS OUTPUT in the output folder and 
@ECHO.ONLY if there are NO errors should you then submit the WHOLE of 
@ECHO.the output folder (which includes traces AND testrun output proof) 
@ECHO.with your p4 submission. Note that doing a p4 DIFF on the output 
@ECHO.files should show at least date differences and any testcase 
@ECHO.changes to prove that the relevant code changes made are okay.

goto:eof

:: ---------------------------------------------------------------------------
:: -- Function PrintHelp
:: ---------------------------------------------------------------------------
:FuncPrintHelp
@ECHO.run.cmd [debug mode for screen output]
@ECHO.where
@ECHO.	[debug screen output] can be set to 1 to indicate that
@ECHO.	the testrun output should be shown on the screen
@ECHO.	and not redirected to the output file for p4 submission. "
goto:lbl_end

:lbl_end
ENDLOCAL
@ECHO ON
