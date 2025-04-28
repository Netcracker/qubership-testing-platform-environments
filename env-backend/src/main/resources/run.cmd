::Choose which type of DB you would like to use:
::pg - postgresql database
::Example: SET jdbc_type=pg
set jdbc_type=pg

set q_classes_cp=q-classes/${qclass.jar.name}-${project.version}-%jdbc_type%.jar

if not defined HEAP_DUMP_PATH set HEAP_DUMP_PATH=./dumps
if not defined GC_LOGS_PATH set GC_LOGS_PATH=./logs/gc.log

set JAVA_OPTIONS=%JAVA_OPTIONS% -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%HEAP_DUMP_PATH%
set JAVA_OPTIONS=%JAVA_OPTIONS% -XX:+UseSerialGC -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps -Xloggc:%GC_LOGS_PATH%

java --add-opens java.base/java.lang=ALL-UNNAMED -Djavax.net.ssl.keyStore=".\config\keystore.p12" ^
-Djavax.net.ssl.keyStorePassword=123456 ^
%JAVA_OPTIONS% ^
-cp ".\config\;.\lib\*;%q_classes_cp%" org.qubership.atp.environments.Main

@echo off
if NOT ["%errorlevel%"]==["0"] (
    pause
    exit /b %errorlevel%
)
