# Qubership Testing Platform Environments Service

QSTP Environments is a microservice. It is designed to configure and store information about project environments
and to provide this information to other services during test scenarios execution.

## How to install and run the database

For local DB in Docker deployment, see the following instructions - [DB in Docker](./docker-compose/readme.md)

## How to start Backend

This project use [Lombok](https://projectlombok.org). This means that the project has a code that is generated before compilation.
To develop you need to install a plugin for your **IDE**.
* [Intellij IDEA](http://plugins.jetbrains.com/plugin/6317-lombok-plugin)

Probably you would need running Hazelcast instance. You can find information like "How to run Hazelcast locally"

### Build project: mvn clean package
1. In some case with flag -DskipTests
2. If you have not compiled q-classes:
    * check db-postgresql, migration-on-build-pg in profiles;
    * set settings for DB - jdbc.url, jdbc.user, jdbc.password in parent-db-properties (like in the step bellow)
    * and create extensions in db if not
3. Check and enable maven profiles `db-postgresql` and `migration-on-build-pg`

### Create run configuration
1. Main class: org.qubership.atp.environments.Main
2. VM options:
 -Djavax.net.ssl.keyStore=src/main/config/keystore.p12
 -Djavax.net.ssl.keyStorePassword=123456
 -Dspring.datasource.url=jdbc:postgresql://localhost:5432/dev_env?preferQueryMode=simple
 -Dspring.datasource.username=dev_env_user
 -Dspring.datasource.password=dev_env_pass
 -Dlogback.configurationFile=src/main/config/logback.xml
 -Dlog.graylog.on=false
3. Working dir (for example): C:\Projects\env-backend
4. VM options for logging define where is logback file and where log should be (console.log or graylog):
-Dlogback.configurationFile=src/main/config/logback.xml
-Dlog.graylog.on=false

### Run Main
Just run Main#main with args from step above

## How to create dump on production and restore it to local DB
[Create and restore dump]

## Local build without local PostgreSQL installation
Example for **dev04** openshift server connection. For another project use correspond server and credentials.

1. Go to [ATP Cloud]
2. Find paragraph **dev-atp-cloud** > **Base coordinates**
3. Find service **Postgres** in table with **Base coordinates**
4. Copy **External address** with port, for example **127.0.0.1:98765**
5. Go to development environment with **Environment service** opened
6. Go to **src/test/config/application-test-rest-api.properties**
7. Set parameters:
   spring.datasource.url=${pg.jdbc.Url:jdbc:postgresql://127.0.0.1:98765/dev_env}
   spring.datasource.username=${pg.jdbc.User:dev_env_user}
   spring.datasource.password=${pg.jdbc.Password:dev_env_pass}
8. Go to **parent/parent-db-properties/pom.xml**
9. Set parameters:
   <pg.jdbc.Url>jdbc:postgresql://127.0.0.1:98765/dev_env?preferQueryMode=simple</pg.jdbc.Url>
   <pg.jdbc.User>dev_env_user</pg.jdbc.User>
   <pg.jdbc.Password>dev_env_pass</pg.jdbc.Password>
10. Go to **Maven**, select lifecycle phases and click **Run Maven Build**

