{{/* Helper functions, do NOT modify */}}
{{- define "env.default" -}}
{{- $ctx := get . "ctx" -}}
{{- $def := get . "def" | default $ctx.Values.SERVICE_NAME -}}
{{- $pre := get . "pre" | default (eq $ctx.Values.PAAS_PLATFORM "COMPOSE" | ternary "" $ctx.Release.Namespace) -}}
{{- get . "val" | default ((empty $pre | ternary $def (print $pre "_" (trimPrefix "atp-" $def))) | nospace | replace "-" "_") -}}
{{- end -}}

{{- define "env.factor" -}}
{{- $ctx := get . "ctx" -}}
{{- get . "def" | default (eq $ctx.Values.PAAS_PLATFORM "COMPOSE" | ternary "1" (default "3" $ctx.Values.KAFKA_REPLICATION_FACTOR)) -}}
{{- end -}}

{{- define "env.compose" }}
{{- range $key, $val := merge (include "env.lines" . | fromYaml) (include "env.secrets" . | fromYaml) }}
{{ printf "- %s=%s" $key $val }}
{{- end }}
{{- end }}

{{- define "env.cloud" }}
{{- range $key, $val := (include "env.lines" . | fromYaml) }}
{{ printf "- name: %s" $key }}
{{ printf "  value: \"%s\"" $val }}
{{- end }}
{{- $keys := (include "env.secrets" . | fromYaml | keys | uniq | sortAlpha) }}
{{- if eq (default "" .Values.ENCRYPT) "secrets" }}
{{- $keys = concat $keys (list "ATP_CRYPTO_KEY" "ATP_CRYPTO_PRIVATE_KEY") }}
{{- end }}
{{- range $keys }}
{{ printf "- name: %s" . }}
{{ printf "  valueFrom:" }}
{{ printf "    secretKeyRef:" }}
{{ printf "      name: %s-secrets" $.Values.SERVICE_NAME }}
{{ printf "      key: %s" . }}
{{- end }}
{{- end }}
{{/* Helper functions end */}}

{{/* Environment variables to be used AS IS */}}
{{- define "env.lines" }}
ATP_ARCHIVE_CRON_EXPRESSION: "{{ .Values.ATP_ARCHIVE_CRON_EXPRESSION }}"
ATP_ARCHIVE_JOB_NAME: "{{ .Values.ATP_ARCHIVE_JOB_NAME }}"
ATP_HTTP_LOGGING: "{{ .Values.ATP_HTTP_LOGGING }}"
ATP_HTTP_LOGGING_HEADERS: "{{ .Values.ATP_HTTP_LOGGING_HEADERS }}"
ATP_HTTP_LOGGING_HEADERS_IGNORE: "{{ .Values.ATP_HTTP_LOGGING_HEADERS_IGNORE }}"
ATP_HTTP_LOGGING_URI_IGNORE: "{{ .Values.ATP_HTTP_LOGGING_URI_IGNORE }}"
ATP_INTERNAL_GATEWAY_ENABLED: "{{ .Values.ATP_INTERNAL_GATEWAY_ENABLED }}"
ATP_LAST_REVISION_COUNT: "{{ .Values.ATP_LAST_REVISION_COUNT }}"
AUDIT_LOGGING_ENABLE: "{{ .Values.AUDIT_LOGGING_ENABLE }}"
AUDIT_LOGGING_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.AUDIT_LOGGING_TOPIC_NAME "def" "audit_logging_topic") }}"
CACHE_ENVIRONMENTS_BY_SYSTEM_ID_PERIOD: "{{ .Values.CACHE_ENVIRONMENTS_BY_SYSTEM_ID_PERIOD }}"
CACHE_PERIOD: "{{ .Values.CACHE_PERIOD }}"
CACHE_SYSTEMS_BY_ENVIRONMENT_ID_PERIOD: "{{ .Values.CACHE_SYSTEMS_BY_ENVIRONMENT_ID_PERIOD }}"
CACHE_VERSIONS_PERIOD: "{{ .Values.CACHE_VERSIONS_PERIOD }}"
CATALOGUE_INTEGRATION_ENABLED: "{{ .Values.CATALOGUE_INTEGRATION_ENABLED }}"
CONSUL_ENABLED: "{{ .Values.CONSUL_ENABLED }}"
CONSUL_PORT: "{{ .Values.CONSUL_PORT }}"
CONSUL_PREFIX: "{{ .Values.CONSUL_PREFIX }}"
CONSUL_TOKEN: "{{ .Values.CONSUL_TOKEN }}"
CONSUL_URL: "{{ .Values.CONSUL_URL }}"
CONTENT_SECURITY_POLICY: "{{ .Values.CONTENT_SECURITY_POLICY }}"
EI_GRIDFS_DB: "{{ include "env.default" (dict "ctx" . "val" .Values.EI_GRIDFS_DB "def" "atp-ei-gridfs") }}"
ENVIRONMENT_DB: "{{ include "env.default" (dict "ctx" . "val" .Values.ENVIRONMENT_DB "def" "atp-envconf") }}"
EUREKA_CLIENT_ENABLED: "{{ .Values.EUREKA_CLIENT_ENABLED }}"
FEIGN_ATP_CATALOGUE_NAME: "{{ .Values.FEIGN_ATP_CATALOGUE_NAME }}"
FEIGN_ATP_CATALOGUE_ROUTE: "{{ .Values.FEIGN_ATP_CATALOGUE_ROUTE }}"
FEIGN_ATP_CATALOGUE_URL: "{{ .Values.FEIGN_ATP_CATALOGUE_URL }}"
FEIGN_ATP_EI_NAME: "{{ .Values.FEIGN_ATP_EI_NAME }}"
FEIGN_ATP_EI_ROUTE: "{{ .Values.FEIGN_ATP_EI_ROUTE }}"
FEIGN_ATP_EI_URL: "{{ .Values.FEIGN_ATP_EI_URL }}"
EI_CLEAN_JOB_ENABLED: "{{ .Values.EI_CLEAN_JOB_ENABLED }}"
EI_CLEAN_JOB_WORKDIR: "{{ .Values.EI_CLEAN_JOB_WORKDIR }}"
EI_CLEAN_SCHEDULED_JOB_PERIOD_MS: "{{ .Values.EI_CLEAN_SCHEDULED_JOB_PERIOD_MS }}"
EI_CLEAN_JOB_FILE_DELETE_AFTER_MS: "{{ .Values.EI_CLEAN_JOB_FILE_DELETE_AFTER_MS }}"
FEIGN_ATP_HEALTHCHECK_NAME: "{{ .Values.FEIGN_ATP_HEALTHCHECK_NAME }}"
FEIGN_ATP_HEALTHCHECK_ROUTE: "{{ .Values.FEIGN_ATP_HEALTHCHECK_ROUTE }}"
FEIGN_ATP_HEALTHCHECK_URL: "{{ .Values.FEIGN_ATP_HEALTHCHECK_URL }}"
FEIGN_ATP_INTERNAL_GATEWAY_NAME: "{{ .Values.FEIGN_ATP_INTERNAL_GATEWAY_NAME }}"
FEIGN_ATP_USERS_NAME: "{{ .Values.FEIGN_ATP_USERS_NAME }}"
FEIGN_ATP_USERS_ROUTE: "{{ .Values.FEIGN_ATP_USERS_ROUTE }}"
FEIGN_ATP_USERS_URL: "{{ .Values.FEIGN_ATP_USERS_URL }}"
GC_LOGS_PATH: "{{ .Values.GC_LOGS_PATH }}"
GRAYLOG_HOST: "{{ .Values.GRAYLOG_HOST }}"
GRAYLOG_ON: "{{ .Values.GRAYLOG_ON }}"
GRAYLOG_PORT: "{{ .Values.GRAYLOG_PORT }}"
ENV_EI_DB_ENABLE: "{{ .Values.ENV_EI_DB_ENABLE }}"
GRIDFS_DB_ADDR: "{{ .Values.GRIDFS_DB_ADDR }}"
GRIDFS_DB_PORT: "{{ .Values.GRIDFS_DB_PORT }}"
HAZELCAST_ADDRESS: "{{ .Values.HAZELCAST_ADDRESS }}"
HAZELCAST_CLUSTER_NAME: "{{ .Values.HAZELCAST_CLUSTER_NAME }}"
HAZELCAST_ENABLE: "{{ .Values.HAZELCAST_ENABLE }}"
HEAP_DUMP_PATH: "{{ .Values.HEAP_DUMP_PATH }}"
HIKARI_CONNECTION_TIMEOUT: "{{ .Values.HIKARI_CONNECTION_TIMEOUT }}"
HIKARI_IDLE_TIMEOUT: "{{ .Values.HIKARI_IDLE_TIMEOUT }}"
HIKARI_MAX_LIFETIME: "{{ .Values.HIKARI_MAX_LIFETIME }}"
HIKARI_MAX_POOL_SIZE: "{{ .Values.HIKARI_MAX_POOL_SIZE }}"
HIKARI_MIN_POOL_SIZE: "{{ .Values.HIKARI_MIN_POOL_SIZE }}"
HOME_LINK: "/"
JAVA_OPTIONS: "{{ if .Values.HEAPDUMP_ENABLED }}-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/diagnostic{{ end }} -Datp-environments.get.version.httpclient.maxTotal={{ .Values.GET_VERSION_HTTP_CLIENT_CONNECTION_MAX_TOTAL  }} -Datp-environments.get.version.httpclient.defaultMaxPerRoute={{ .Values.GET_VERSION_HTTP_CLIENT_CONNECTION_DEFAULT_MAX_PER_ROUTE  }} -Datp-environments.get.version.httpclient.socketTimeout={{ .Values.GET_VERSION_HTTP_CLIENT_SOCKET_TIMEOUT }} -Dcom.sun.management.jmxremote={{ .Values.JMX_ENABLE }} -Dcom.sun.management.jmxremote.port={{ .Values.JMX_PORT }} -Dcom.sun.management.jmxremote.rmi.port={{ .Values.JMX_RMI_PORT }} -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -XX:MaxRAMPercentage=70.0"
JAVERS_ENABLED: "{{ .Values.JAVERS_ENABLED }}"
KAFKA_CLIENT_ID: "{{ .Values.SERVICE_NAME }}"
KAFKA_CONNECTIONS_EVENT_PRODUCER_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_CONNECTIONS_EVENT_PRODUCER_TOPIC_NAME "def" "connections_notification_topic") }}"
KAFKA_ENABLE: "{{ .Values.KAFKA_ENABLE }}"
KAFKA_ENVIRONMENTS_EVENT_PRODUCER_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_ENVIRONMENTS_EVENT_PRODUCER_TOPIC_NAME "def" "environments_notification_topic") }}"
KAFKA_GROUP_ID: "{{ .Values.SERVICE_NAME }}"
KAFKA_MAX_REQUEST_SIZE: "{{ .Values.KAFKA_MAX_REQUEST_SIZE }}"
KAFKA_PROJECT_EVENT_CONSUMER_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_PROJECT_EVENT_CONSUMER_TOPIC_NAME "def" "catalog_notification_topic") }}"
KAFKA_SERVERS: "{{ .Values.KAFKA_SERVERS }}"
KAFKA_SERVICE_ENTITIES_TOPIC: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_SERVICE_ENTITIES_TOPIC "def" "service_entities") }}"
KAFKA_SERVICE_ENTITIES_TOPIC_PARTITIONS: "{{ .Values.KAFKA_SERVICE_ENTITIES_TOPIC_PARTITIONS }}"
KAFKA_SERVICE_ENTITIES_TOPIC_REPLICATION_FACTOR: "{{ include "env.factor" (dict "ctx" . "def" .Values.KAFKA_SERVICE_ENTITIES_TOPIC_REPLICATION_FACTOR) }}"
KAFKA_SYSTEMS_EVENT_PRODUCER_TOPIC_NAME: "{{ include "env.default" (dict "ctx" . "val" .Values.KAFKA_SYSTEMS_EVENT_PRODUCER_TOPIC_NAME "def" "systems_notification_topic") }}"
KEYCLOAK_AUTH_URL: "{{ .Values.KEYCLOAK_AUTH_URL }}"
KEYCLOAK_ENABLED: "{{ .Values.KEYCLOAK_ENABLED }}"
KEYCLOAK_REALM: "{{ .Values.KEYCLOAK_REALM }}"
LOCALE_RESOLVER: "{{ .Values.LOCALE_RESOLVER }}"
LOG_LEVEL: "{{ .Values.LOG_LEVEL }}"
MAX_RAM: "{{ .Values.MAX_RAM }}"
MICROSERVICE_NAME: "{{ .Values.SERVICE_NAME }}"
PG_DB_ADDR: "{{ .Values.PG_DB_ADDR }}"
PG_DB_PORT: "{{ .Values.PG_DB_PORT }}"
PROFILER_ENABLED: "{{ .Values.PROFILER_ENABLED }}"
PROJECT_INFO_ENDPOINT: "{{ .Values.PROJECT_INFO_ENDPOINT }}"
REGISTERED_CLIENT: ""
REMOTE_DUMP_HOST: "{{ .Values.REMOTE_DUMP_HOST }}"
REMOTE_DUMP_PORT: "{{ .Values.REMOTE_DUMP_PORT }}"
SERVICE_ENTITIES_MIGRATION_ENABLED: "{{ .Values.SERVICE_ENTITIES_MIGRATION_ENABLED }}"
SERVICE_NAME: "{{ .Values.SERVICE_NAME }}"
SERVICE_REGISTRY_URL: "{{ .Values.SERVICE_REGISTRY_URL }}"
SPRING_PROFILES: "{{ .Values.SPRING_PROFILES }}"
SWAGGER_ENABLED: "{{ .Values.SWAGGER_ENABLED }}"
VAULT_ENABLE: "{{ .Values.VAULT_ENABLE}}"
VAULT_NAMESPACE: "{{ .Values.VAULT_NAMESPACE}}"
VAULT_ROLE_ID: "{{ .Values.VAULT_ROLE_ID}}"
VAULT_URI: "{{ .Values.VAULT_URI}}"
ZIPKIN_ENABLE: "{{ .Values.ZIPKIN_ENABLE }}"
ZIPKIN_PROBABILITY: "{{ .Values.ZIPKIN_PROBABILITY }}"
ZIPKIN_URL: "{{ .Values.ZIPKIN_URL }}"
KAFKA_REPORTING_SERVERS: "{{ .Values.KAFKA_REPORTING_SERVERS }}"
AUDIT_LOGGING_TOPIC_PARTITIONS: "{{ .Values.AUDIT_LOGGING_TOPIC_PARTITIONS }}"
AUDIT_LOGGING_TOPIC_REPLICAS: "{{ include "env.factor" (dict "ctx" . "def" .Values.AUDIT_LOGGING_TOPIC_REPLICAS) }}"
{{- end }}

{{/* Sensitive data to be converted into secrets whenever possible */}}
{{- define "env.secrets" }}
ENVIRONMENT_DB_USER: "{{ include "env.default" (dict "ctx" . "val" .Values.ENVIRONMENT_DB_USER "def" "atp-envconf") }}"
ENVIRONMENT_DB_PASSWORD: "{{ include "env.default" (dict "ctx" . "val" .Values.ENVIRONMENT_DB_PASSWORD "def" "atp-envconf") }}"
EI_GRIDFS_PASSWORD: "{{ include "env.default" (dict "ctx" . "val" .Values.EI_GRIDFS_PASSWORD "def" "atp-ei-gridfs") }}"
EI_GRIDFS_USER: "{{ include "env.default" (dict "ctx" . "val" .Values.EI_GRIDFS_USER "def" "atp-ei-gridfs") }}"
KEYCLOAK_CLIENT_NAME: "{{ default "environments" .Values.KEYCLOAK_CLIENT_NAME }}"
KEYCLOAK_SECRET: "{{ default "28b4cf8a-24e6-4fa2-82df-9ef5907560c6" .Values.KEYCLOAK_SECRET }}"
VAULT_SECRET_ID: "{{ default "" .Values.VAULT_SECRET_ID }}"
{{- end }}

{{- define "env.deploy" }}
ei_gridfs_pass: "{{ .Values.ei_gridfs_pass }}"
ei_gridfs_user: "{{ .Values.ei_gridfs_user }}"
gridfs_pass: "{{ .Values.gridfs_pass }}"
gridfs_user: "{{ .Values.gridfs_user }}"
pg_pass: "{{ .Values.pg_pass }}"
pg_user: "{{ .Values.pg_user }}"
{{- end }}