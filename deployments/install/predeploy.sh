#!/usr/bin/env sh

if [ ! -f ./atp-common-scripts/openshift/common.sh ]; then
  echo "ERROR: Cannot locate ./atp-common-scripts/openshift/common.sh"
  exit 1
fi

. ./atp-common-scripts/openshift/common.sh

_ns="${NAMESPACE}"

ENVIRONMENT_DB="$(env_default "${ENVIRONMENT_DB}" "atp-envconf" "${_ns}")"
ENVIRONMENT_DB_USER="$(env_default "${ENVIRONMENT_DB_USER}" "atp-envconf" "${_ns}")"
ENVIRONMENT_DB_PASSWORD="$(env_default "${ENVIRONMENT_DB_PASSWORD}" "atp-envconf" "${_ns}")"
EI_GRIDFS_DB="$(env_default "${EI_GRIDFS_DB}" "atp-ei-gridfs" "${_ns}")"
EI_GRIDFS_USER="$(env_default "${EI_GRIDFS_USER}" "atp-ei-gridfs" "${_ns}")"
EI_GRIDFS_PASSWORD="$(env_default "${EI_GRIDFS_PASSWORD}" "atp-ei-gridfs" "${_ns}")"

echo "***** Initializing databases ******"
init_pg "${PG_DB_ADDR}" "${ENVIRONMENT_DB}" "${ENVIRONMENT_DB_USER}" "${ENVIRONMENT_DB_PASSWORD}" "${PG_DB_PORT}" "${pg_user}" "${pg_pass}"
#init_mongo "${EI_GRIDFS_DB_ADDR:-$GRIDFS_DB_ADDR}" "${EI_GRIDFS_DB}" "${EI_GRIDFS_USER}" "${EI_GRIDFS_PASSWORD}" "${EI_GRIDFS_DB_PORT:-$GRIDFS_DB_PORT}" "${ei_gridfs_user:-$gridfs_user}" "${ei_gridfs_pass:-$gridfs_pass}"

echo "***** Setting up encryption *****"
encrypt "${ENCRYPT}" "${SERVICE_NAME}"
