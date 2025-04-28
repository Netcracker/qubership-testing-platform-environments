#!/bin/sh
#Choose which type of DB you would like to use:
#pg - postgresql database
#Example: SET jdbc_type=pg
export jdbc_type=pg

export q_classes_cp=q-classes/${qclass.jar.name}-${project.version}-${jdbc_type}.jar

export registered_client=
export home_link=/

JAVA_OPTIONS="${JAVA_OPTIONS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${HEAP_DUMP_PATH:-./dumps}"
JAVA_OPTIONS="${JAVA_OPTIONS} -XX:+UseSerialGC -verbose:gc -Xlog:gc*:file=${GC_LOGS_PATH:-gc.log}:time,uptime,level,tags"

java -XX:MaxRAM=2048m \
-Djavax.net.ssl.keyStore="./config/keystore.p12" \
-Djavax.net.ssl.keyStorePassword=123456 \
${JAVA_OPTIONS} \
-cp "./config/:./lib/*:./${q_classes_cp}" org.qubership.atp.environments.Main
