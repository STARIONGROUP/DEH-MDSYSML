
@ECHO OFF

REM deploy.bat
REM
REM Copyright (c) 2020-2021 RHEA System S.A.
REM
REM Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
REM
REM This file is part of DEH-MDSYSML
REM
REM The DEH-MDSYSML is free software; you can redistribute it and/or
REM modify it under the terms of the GNU Lesser General Public
REM License as published by the Free Software Foundation; either
REM version 3 of the License, or (at your option) any later version.
REM
REM The DEH-MDSYSML is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
REM Lesser General Public License for more details.
REM
REM You should have received a copy of the GNU Lesser General Public License
REM along with this program; if not, write to the Free Software Foundation,
REM Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

echo ^=======================================================================================================
echo ^=                   ALMOST-AUTO-DEPLOY for the DEH-MDSYSML Adapter                                    =
echo ^=                                                                                                     =
echo ^= --First argument--                                                                                  =
echo ^= \-c ===^> sets the target to be Cameo System Modeler instead of the default target: MagicDraw \-m     =
echo ^= --Second argument--                                                                                  =
echo ^= \-p ===^> Generate DEH-MDSYSML plugin                                                                =
echo ^= \-i ===^> Install the generated DEH-MDSYSML plugin                                                   =
echo ^= \-c ===^> Run Cameo                                                                                  =
echo ^= \-m ===^> Run MagicDraw                                                                              =
echo ^=======================================================================================================

set target = "m"
if /I "%1" == "-c" set target="c"
if /I "%1" == "-m" set target="m"
if /I "%2" == "-p" goto PackPlugin
if /I "%2" == "-i" goto Install
if /I "%2" == "-c" goto RunCameo
if /I "%2" == "-m" goto RunMagicDraw

:PackPlugin
REM 6. Pack the plugin
cd ..\DEH-MDSYSML\

echo.
echo ==================================^> call mvn package DEH-MDSYSML
echo ===============================================================^>
echo.

call mvn package -Dmaven.test.skip=true
echo Exit Code = %ERRORLEVEL%
if not "%ERRORLEVEL%" == "0" GOTO ExitStatement

:Install
REM 7. Install the plugin
echo.
echo ==================================^> Install the plugin
echo ===============================================================^>
echo.

set targetPath=C:\MagicDraw\MagicDraw
if %target% == "c" set targetPath=C:\Program Files\Cameo Systems Modeler Demo
echo "%targetPath%\plugins\com.rheagroup.dehmdsysml"

SET COPYCMD=/Y && move /Y "target\DEHMDSYSMLPlugin.jar" "%targetPath%\plugins\com.rheagroup.dehmdsysml"
echo Exit Code = %ERRORLEVEL%
if not "%ERRORLEVEL%" == "0" GOTO ExitStatement

if %target% == "m" goto RunMagicDraw
if %target% == "c" goto RunCameo

:RunCameo
REM 8. Run Cameo
echo.
echo ==================================^> Run Cameo
echo ===============================================================^>
echo.
call "C:\Program Files\Cameo Systems Modeler Demo\bin\csm.exe"

GOTO ExitStatement

:RunMagicDraw
REM 8. Run MagicDraw
echo.
echo ==================================^> Run MagicDraw
echo ===============================================================^>
echo.
call "C:\MagicDraw\MagicDraw\bin\magicdraw.exe"

GOTO ExitStatement

:ExitStatement
echo deploy.bat is done