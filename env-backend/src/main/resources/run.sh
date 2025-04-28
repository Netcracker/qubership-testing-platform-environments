#!/usr/bin/env sh

#Choose which type of DB you would like to use:
#pg - postgresql database
#Example: jdbc_type=pg
jdbc_type=pg

if [ "${ATP_INTERNAL_GATEWAY_ENABLED:-false}" = "true" ]; then
  echo "Internal gateway integration is enabled."
  FEIGN_ATP_CATALOGUE_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_HEALTHCHECK_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_EI_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
  FEIGN_ATP_USERS_NAME=${FEIGN_ATP_INTERNAL_GATEWAY_NAME}
else
  echo "Internal gateway integration is disabled."
  FEIGN_ATP_CATALOGUE_ROUTE=
  FEIGN_ATP_HEALTHCHECK_ROUTE=
  FEIGN_ATP_EI_ROUTE=
  FEIGN_ATP_USERS_ROUTE=
fi

JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.devtools.add-properties=false"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.config.location=${SPRING_CONFIG_LOCATION:-./config/application.properties}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.cloud.bootstrap.location=${SPRING_CONFIG_LOCATION:-./config/bootstrap.properties}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dlog.graylog.on=${GRAYLOG_ON:-false}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dlog.graylog.host=${GRAYLOG_HOST}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dlog.graylog.port=${GRAYLOG_PORT}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Datp.crypto.enabled=${ATP_CRYPTO_ENABLED:-true}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.datasource.url=jdbc:postgresql://${PG_DB_ADDR:?}:${PG_DB_PORT:?}/${ENVIRONMENT_DB:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.datasource.username=${ENVIRONMENT_DB_USER:?Must provide ENVIRONMENT_DB_USER}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dspring.datasource.password=${ENVIRONMENT_DB_PASSWORD:?Must provide ENVIRONMENT_DB_PASSWORD}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.database=${EI_GRIDFS_DB:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.host=${EI_GRIDFS_DB_ADDR:-$GRIDFS_DB_ADDR}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.port=${EI_GRIDFS_DB_PORT:-$GRIDFS_DB_PORT}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.user=${EI_GRIDFS_USER:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -Dei.gridfs.password=${EI_GRIDFS_PASSWORD:?}"
JAVA_OPTIONS="${JAVA_OPTIONS} -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${HEAP_DUMP_PATH:-./dumps}"
JAVA_OPTIONS="${JAVA_OPTIONS} -verbose:gc -Xlog:gc*:file=${GC_LOGS_PATH:-gc.log}:time,uptime,level,tags"

/usr/bin/java --add-opens java.base/java.lang=ALL-UNNAMED -XX:MaxRAM=${MAX_RAM:-2048m} ${JAVA_OPTIONS} -cp "./config/:./lib/*:./q-classes/*" org.qubership.atp.environments.Main
