#!/bin/sh
#Choose which type of DB you would like to use:
#pg - postgresql database
#Example: SET jdbc_type=pg
export jdbc_type=pg

#-Dlb.libs.path="scripts"
java -Djdbc_type=${jdbc_type} -cp "./config/:./lib/*" org.qubership.atp.environments.db.migration.Main
