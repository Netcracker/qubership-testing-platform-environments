# Qubership ATP-ENVIRONMENTS Installation Guide

## This guide describes Qubership ATP-ENVIRONMENTS installation process

### Requirements
1. Postgres database is installed 
   2. Use link to download and install 9.6 release version https://www.postgresql.org/download/linux/
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

### Installation

* Choose installation method:
  * Install on OpenShift use this article ATP Environments. Installation on OpenShift.
  * Install Standalone use this article ATP Environments. Installation and update for standalone.
