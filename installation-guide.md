# Qubership Testing Platform ENVIRONMENTS Service Installation Guide

## This guide describes how to install Qubership Testing Platform ENVIRONMENTS Service

### Requirements
1. Postgres database is installed
   2. Use link to download and install [PostgreSQL 9.6.x version](https://www.postgresql.org/download/linux/)
2. Java 1.8 is installed
3. General System Requirements:

| Service          | vCPU | RAM | HDD   |
|------------------|------|-----|-------|
| Postgres         | 1    | 1   | 2     |
| ATP Environments | 1    | 1   | 500mb |

## Installation Notes
### Preparing Database

1. Create new user

Connect any client to postgres database and execute script (change "envconf"  as your username and "envconf" as your password in example of script)

`create user envconf with encrypted password 'envconf';`

2. Create new database (change "envconf" as your username and "envconf" as db name)

`create database envconf owner envconf;`

3. Grant user to database (change "envconf" as your username and "envconf" as db name)

`grant all privileges on database envconf to envconf;`
`ALTER USER envconf WITH SUPERUSER;`

4. Execute script

`CREATE EXTENSION IF NOT EXISTS "uuid-ossp"`

### Installation under Helm

#### Prerequisites

1. Install k8s locally
2. Install Helm

#### How to deploy tool

1. Build snapshot (artifacts and Docker image) of [Environments Repository](https://github.com/Netcracker/qubership-testing-platform-environments) in GitHub
2. Clone repository to a place, available from your openshift/kubernetes where you need to deploy the tool to
3. Navigate to <repository-root>/deployments/charts/atp-environments folder
4. Check/change configuration parameters in the ./values.yaml file according to your services installed
5. Execute the command: `helm install atp-environments`
6. After installation is completed, check deployment health
