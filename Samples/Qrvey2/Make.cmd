@IF NOT DEFINED DEBUG ECHO OFF

REM =================================================================================
REM Make - Script to choreograph the various installation sub-jobs.
REM
REM
REM Written by Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
REM
REM =================================================================================
SET _ALLOWED_TARGETS=All Zip Templates S3Master 
REM S3Replica - not required as these are propogated by a Lambda function
SET __APPLICATION_NAME=Installing files for ReSTFetcher Qrvey Sample
SET _OPEN_AWS_REGION_MASTER=ap-southeast-1:apse2 
SET _CLIENT_AWS_REGION_MASTER=ap-southeast-1:apse2
SET _CLIENT_PERMISSIONS=--grants full=emailaddress=Ian_MacDonald_Hogan@yahoo.com
SET _OPEN_PERMISSIONS=--acl public-read

TITLE %__APPLICATION_NAME%

COLOR 79

SETLOCAL

PUSHD "%~dp0"
SET _INSTALL_PATH=%~dp0
SET _SELF=%~0

SET _MAKE_BUCKET=
SET _DOMAIN=au-com-thinkronicity
SET _VERSION=1.0.0
SET _S3_SOURCE=open* client*
SET _S3_Payer=BucketOwner

REM =================================================================================
REM Get parameters
REM =================================================================================
:NEXT_PARAM

IF "%~1"=="" GOTO USAGE

IF /I "%~1"=="/P" (

    SET _AWS_PROFILE=--profile %~2
    SHIFT
    SHIFT
    GOTO NEXT_PARAM
)

IF /I "%~1"=="/C" (

    SET _S3_SOURCE=client*
    SHIFT
    GOTO NEXT_PARAM
)

IF /I "%~1"=="/O" (

    SET _S3_SOURCE=open*
    SHIFT
    GOTO NEXT_PARAM
)

IF /I "%~1"=="/BR" (

    SET _S3_Payer=Requester
    SHIFT
    GOTO NEXT_PARAM
)

IF /I "%~1"=="/D" (

    SET _DOMAIN=%~2
    SHIFT
    SHIFT
    GOTO NEXT_PARAM
)

IF /I "%~1"=="/B" (

    SET _MAKE_BUCKET=Y
    SHIFT
    GOTO NEXT_PARAM
)

IF /I "%~1"=="/F" (

    SET _FILTER=%~2
    SHIFT
    SHIFT
    GOTO NEXT_PARAM
)

IF /I "%~1"=="/V" (

    SET _VERSION=%~2
    SHIFT
    SHIFT
    GOTO NEXT_PARAM
)

REM =================================================================================
:DO_MAKE
REM =================================================================================

REM Replace single quote with double quote in filter expression.

IF DEFINED _FILTER SET _FILTER=%_FILTER:'="%

TITLE %__APPLICATION_NAME%

REM =================================================================================
REM Start installation
REM =================================================================================

:NEXT_TARGET

IF DEFINED _QUIT GOTO END
IF "%~1"=="" GOTO END

SET _TARGET=%~1
SHIFT

FOR %%T IN (%_ALLOWED_TARGETS%) DO IF /I "%_TARGET%"=="%%T" (GOTO %_TARGET%_INSTALL)

ECHO Unknown target - %_TARGET%
GOTO USAGE

REM =================================================================================
:ALL_INSTALL
REM =================================================================================

CALL "%_SELF%" %_ALLOWED_TARGETS:~4% 

GOTO END

REM =================================================================================
:Zip_INSTALL
REM =================================================================================

PUSHD S3Buckets\files

DEL "%_INSTALL_PATH%S3Buckets\opencode\au-com-thinkronicity-RestFetcher-QrveySample2-V%_VERSION%.zip"

7za a -r -tZIP "%_INSTALL_PATH%S3Buckets\opencode\au-com-thinkronicity-RestFetcher-QrveySample2-V%_VERSION%.zip" *.*

POPD

GOTO NEXT_TARGET

REM =================================================================================
:Templates_INSTALL
REM =================================================================================

PUSHD Stack

SET _BUCKET_SUFFIX=opencode
CALL :DECODE_REGION_PARAM "%_CLIENT_AWS_REGION_MASTER%"

SET _PERMISSIONS=%_CLIENT_PERMISSIONS%

ECHO Copying from local file system to %_BUCKET%/CloudFormation/

aws s3 cp . s3://%_BUCKET%/CloudFormation/ %_AWS_PROFILE% %_PERMISSIONS% --recursive %_FILTER%

POPD

GOTO NEXT_TARGET

REM =================================================================================
:S3Master_INSTALL
REM =================================================================================

SET _OPEN_AWS_REGIONS=%_OPEN_AWS_REGION_MASTER%
SET _CLIENT_AWS_REGIONS=%_CLIENT_AWS_REGION_MASTER%

SET _SOURCE_BUCKET_OPEN=
SET _SOURCE_BUCKET_CLIENT=

PUSHD S3Buckets

FOR /D %%F IN (%_S3_SOURCE%) DO CALL :DEPLOY_REGION_BUCKETS %%~nF 

POPD

GOTO NEXT_TARGET

REM =================================================================================
:S3Replica_INSTALL
REM =================================================================================

SET _BUCKET_SUFFIX=opencode
SET _OPEN_AWS_REGIONS=%_OPEN_AWS_REGIONS_REPLICA%
SET _CLIENT_AWS_REGIONS=%_CLIENT_AWS_REGIONS_REPLICA%

CALL :DECODE_REGION_PARAM "%_OPEN_AWS_REGION_MASTER%"

SET _BUCKET_SUFFIX=clientcode
SET _SOURCE_BUCKET_OPEN=%_BUCKET%
SET _SOURCE_REGION_OPEN=%_BUCKET_REGION%

CALL :DECODE_REGION_PARAM "%_CLIENT_AWS_REGION_MASTER%"

SET _SOURCE_BUCKET_CLIENT=%_BUCKET%
SET _SOURCE_REGION_CLIENT=%_BUCKET_REGION%

PUSHD S3Buckets

FOR /D %%F IN (%_S3_SOURCE%) DO CALL :DEPLOY_REGION_BUCKETS %%~nF 

POPD

GOTO NEXT_TARGET

REM =================================================================================
:DEPLOY_REGION_BUCKETS
REM =================================================================================

SET _BUCKET_SUFFIX=%~1
IF /I "%_BUCKET_SUFFIX:~0,4%"=="open" (

    SET _AWS_REGIONS=%_OPEN_AWS_REGIONS%
    SET _PERMISSIONS=%_OPEN_PERMISSIONS%
    SET _SOURCE_BUCKET=%_SOURCE_BUCKET_OPEN%
    SET _SOURCE_REGION=%_SOURCE_REGION_OPEN%

) ELSE (
    SET _AWS_REGIONS=%_CLIENT_AWS_REGIONS%
    SET _PERMISSIONS=%_CLIENT_PERMISSIONS%
    SET _SOURCE_BUCKET=%_SOURCE_BUCKET_CLIENT%
    SET _SOURCE_REGION=%_SOURCE_REGION_CLIENT%
) 

IF DEFINED _AWS_REGIONS FOR %%R IN (%_AWS_REGIONS%) DO CALL :DEPLOY_BUCKET %%R

GOTO :EOF

REM =================================================================================
:DEPLOY_BUCKET
REM =================================================================================

CALL :DECODE_REGION_PARAM "%~1"

IF DEFINED _MAKE_BUCKET CALL :CREATE_BUCKET "%_BUCKET%"

IF DEFINED _QUIT GOTO :EOF

PUSHD "%_BUCKET_SUFFIX%"

ECHO NOW IN "%_BUCKET_SUFFIX%"

IF DEFINED _NO_COPY GOTO DEPLOY_BUCKET_END

IF NOT DEFINED _SOURCE_BUCKET (

    ECHO Copying from local file system to %_BUCKET%

    aws s3 cp . s3://%_BUCKET%/ %_AWS_PROFILE% %_PERMISSIONS% --recursive %_FILTER%

) ELSE (

    ECHO Copying from source bucket %_SOURCE_BUCKET% to %_BUCKET%
    aws s3 cp s3://%_SOURCE_BUCKET%/ s3://%_BUCKET%/ --source-region %_SOURCE_REGION% %_AWS_PROFILE% %_PERMISSIONS% --recursive %_FILTER%
)


:DEPLOY_BUCKET_END

POPD

GOTO :EOF

REM =================================================================================
:DECODE_REGION_PARAM
REM =================================================================================

SET _BUCKET_REGION_PARAM=%~1

SET _BUCKET_REGION_PARAM=%_BUCKET_REGION_PARAM: =%

SET _BUCKET_REGION=%_BUCKET_REGION_PARAM:~0,-6%
SET _BUCKET_REGION_CODE=%_BUCKET_REGION_PARAM:~-5%

SET _BUCKET=%_DOMAIN%-%_BUCKET_SUFFIX%-%_BUCKET_REGION_CODE%

ECHO _BUCKET_REGION_PARAM="%_BUCKET_REGION_PARAM%"
ECHO _BUCKET_REGION="%_BUCKET_REGION%"
ECHO _BUCKET_REGION_CODE="%_BUCKET_REGION_CODE%"
ECHO _BUCKET="%_BUCKET%"

GOTO :EOF

REM =================================================================================
:CREATE_BUCKET
REM =================================================================================

REM See if the target bucket exists

aws s3 ls s3:// %_AWS_PROFILE%  | FINDSTR "%~1"

IF NOT ERRORLEVEL 1 GOTO CREATE_BUCKET_CHECK

GOTO CREATE_BUCKET_GO

:CREATE_BUCKET_CHECK

SET _RESPONSE=Q
CALL :GET_RESPONSE "Bucket %~1 already exists - continue with the install?" Y Q /R
GOTO :EOF

:CREATE_BUCKET_GO

aws s3 mb s3://%~1 %_AWS_PROFILE% --region %_BUCKET_REGION%

GOTO :EOF

REM =================================================================================
:USAGE
REM =================================================================================

ECHO.
ECHO Usage is %0 [/P "AWS Profile name"] [/D Domain] [/V version] [/B] [/F filter] Target[, Target ...]
ECHO.
ECHO where Target is one of:
FOR %%T IN (%_ALLOWED_TARGETS%) DO ECHO    %%T
ECHO.
ECHO and:
ECHO /P is optionally used to specify an AWS named profile
ECHO /C is optionally used to specify only client S3 files need to be copied
ECHO /O is optionally used to specify only open code S3 files need to be copied
ECHO /D is optionally used to override the default application domain %_DOMAIN%
ECHO /V is optionally used to override the default version of %_VERSION% 
REM ECHO /BR is optionally used to override the default bucket payer of BucketOwner with Requester
ECHO /B is optionally used to specify the S3 Buckets should be created
ECHO /F is optionally used to specify a filter for S3 copy operations.
ECHO.

GOTO END

REM =================================================================================
:GET_RESPONSE 
REM =================================================================================

CALL "%_INSTALL_PATH%\_Scripts\GetResponse.cmd" %*
GOTO :EOF

REM =================================================================================
:LOG_MESSAGE
REM =================================================================================

ECHO %*
IF DEFINED _Log ECHO>>"%_Log%" %*

GOTO :EOF

REM =================================================================================
:END
REM =================================================================================

PAUSE

ENDLOCAL
POPD
