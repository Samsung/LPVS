
![License Pre-Validation Service (LPVS)](lpvslogo.png)
[![Build](https://github.com/samsung/lpvs/workflows/Build/badge.svg)](https://github.com/samsung/lpvs/actions?query=workflow%3ABuild)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7127523.svg)](https://doi.org/10.5281/zenodo.7127523)
[![CodeQL Analysis](https://github.com/Samsung/LPVS/workflows/CodeQL%20Analysis/badge.svg)](https://github.com/Samsung/LPVS/actions?query=workflow%3A%22CodeQL+Analysis%22)
[![CII Best Practices](https://bestpractices.coreinfrastructure.org/projects/6309/badge)](https://bestpractices.coreinfrastructure.org/projects/6309)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/Samsung/LPVS/badge)](https://api.securityscorecards.dev/projects/github.com/Samsung/LPVS)
[![RepoSize](https://img.shields.io/github/repo-size/samsung/lpvs.svg)](https://github.com/Samsung/LPVS)
[![Release](https://img.shields.io/github/v/release/samsung/lpvs.svg)](https://github.com/Samsung/LPVS/releases)
[![LICENSE](https://img.shields.io/github/license/samsung/lpvs.svg)](https://github.com/Samsung/LPVS/blob/main/LICENSE)

## Introduction
OpenSource code [refers](https://en.wikipedia.org/wiki/Open-source_software) to software available for use, study, change, and distribution by anyone and for any purpose provided that the corresponding license conditions are met. License violation may end up with litigations, damage compensation, obligation to disclose intellectual property as well as reputational losses. 

In a project with many external dependencies it might be really difficult to trace license obligations. Also if many collaborators are involved, a risk of non-intentional license violation (such as via Copy-Paste) grows. There are even more tricky nuances such as double-licensed dependencies or license change (because of owner, purpose, legislation change) that may make a previously safe dependency to become an unsafe one over time.

_License Pre-Validation Service (LPVS)_ helps to mitigate license-related risks for OpenSource code. The tool analyzes the project, identifies its components and their respective licenses at every commit. Then it returns the list of potential issue cases as GitHub comments. _LPVS_ provides the comprehensive description of possible license violations, including  risky code location and license issue overview.

## Features

- Available license scanners: [SCANOSS](https://www.scanoss.com)
- _LPVS_ supports GitHub review system

---

## Quick start

### 1. Setting up your project to interact with _LPVS_ 

_LPVS_ license scan shall be enabled on your project via GitHub Webhooks:

1. [Creating a personal access token (`github.token`)](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-fine-grained-personal-access-token)

2. Configure webhook in your GitHub repository settings:
    - go to `Settings` -> `Webhooks`
    - press `Add webhook`
    - fill in `Payload URL` with: `http://<IP where LPVS is running>:7896/webhooks`  
        > In case of using the *ngrok*, the `Payload URL` should look like this `https://50be-62-205-136-206.ngrok-free.app/webhooks`  
            - Install [*ngrok*](https://dashboard.ngrok.com/get-started) (step 1 and 2)  
            - Run *ngrok* using the following command: `ngrok http 7896`
    - specify content type: `application/json`
    - fill in `Secret` field with the passphrase: `LPVS`
    - the same passphrase must be saved in `github.secret` of LPVS backend `application.properties` or `docker-compose.yml` files 
    - select `Let me select individual events` -> `Pull requests` (make sure that only `Pull requests` is selected)
    - make it `Active`
    - press `Add Webhook`

Configuration from your project side is finished!

---

### 2. Using pre-built _LPVS_ Docker images

This section provides how to download and run pre-built LPVS docker images without building the _LPVS_ project.

For using docker deploy scenario, environment variables may be filled in file `docker-compose.yml`.

#### 2.1 Setting up _LPVS_ docker environment variables
In this case, values in [`docker-compose.yml`](docker-compose.yml) file overwrite values mentioned in `application.properties`.

If you plan to use other than `root` database user that reflecting in files `application.properties` or `docker-compose.yml` as:
```
 spring.datasource.username=user
 spring.datasource.password=password  
```
then you needed to add two fields in `docker-compose.yml` file in section `environment` near `MYSQL_ROOT_PASSWORD` value:
```
  -MYSQL_USER: user
  -MYSQL_PASSWORD: password
```
Otherwise, if only `root` user is used
```
 spring.datasource.username=root
 spring.datasource.password=rootpassword  
```
than only need to fill one field
```
  -MYSQL_ROOT_PASSWORD:rootpassword
```
 But in both cases `MYSQL_ROOT_PASSWORD` need to be filled.
 
 
Also directory of keeping MySQL data may be changed, line:
```
 - ./mysql-lpvs-data:/var/lib/mysql # db storage by default it is directory in root of repository with name mysql-lpvs-data
```
#### 2.2 Run `lpvs` and `mysqldb` docker images by docker-compose
Start _LPVS_ services by `docker-compose` (before Compose V2)
```bash
docker-compose up -d
```
Start _LPVS_ services by `docker compose` (after Compose V2)
```bash
docker compose up -d
```

Stop _LPVS_ services by `docker-compose` (before Compose V2)
```bash
docker-compose down -v --rmi local
```  
Stop _LPVS_ services by `docker compose` (after Compose V2)
```bash
docker compose down
```  

**Now you can create a new pull request or update it with commits. _LPVS_ will start scanning automatically, then provide comments about the licenses found in the project.**

---
   
## How to build and run _LPVS_ from source code

#### 1. Build Prerequisites
- SCANOSS Python package by following the [guideline](https://github.com/scanoss/scanoss.py#installation).
```bash
pip3 install scanoss
```
- Install MySQL server locally
```
sudo apt install mysql-server
```

#### 2. Create necessary MySQL database and user (for the case when the database is supposed to be used)
2.1 Start MySQL server
```
sudo service mysql start
```
2.2 Open Mysql command line interface
```
sudo mysql
```

Run the following commands to create necessary database and user
```
mysql> create database lpvs;
mysql> create user username;
mysql> grant all on lpvs.* to username;
mysql> alter user username identified by identified by 'password';
mysql> exit;
```

2.3 Import existing dump file to newly created databse (_optional instruction below_)
```
mysql -u[username] -p[password] < src/main/resources/database_dump.sql
```
   
2.4 Fill in `licenses` and `license_conflicts` tables with the information about permitted, restricted, and prohibited licenses (mandatory) as well as their compatibility specifics (optional). 

An example database dump file can be found in the repository at [`src/main/resources/database_dump.sql`](src/main/resources/database_dump.sql).
    
2.5 Update the lines of the [`src/main/resources/application.properties`](src/main/resources/application.properties) file:
```text
spring.datasource.username=username
spring.datasource.password=password
```

#### 3 Setting up _LPVS_ `application.properties`

Fill in the lines of the [`src/main/resources/application.properties`](src/main/resources/application.properties) file:

```text
# Fill in the properties associated with github (github.token and github.secret required).
github.token=
github.login=
github.api.url=
github.secret=LPVS
```
> Tip: For personal GitHub account use  `https://api.github.com`  in field `github.api.url=`.  
   
```text
# Used license scanner: scanoss (at the moment, only this scanner is supported)
scanner=scanoss

# Used license conflicts source:
# > option "db": take conflicts from MySQL database - 'license_conflicts' table (should be filled manually
# according to the example at 'src/main/resources/database_dump.sql')
# > option "scanner": take conflicts from the scanner response
  license_conflict=db

# DB configuration (URL, username and password) - example
...
spring.datasource.url=jdbc:mysql://localhost:3306/lpvs
spring.datasource.username=
spring.datasource.password=
```

Alternatively, you can supply all the necessary values associated with GitHub and license using these env variables: `LPVS_GITHUB_LOGIN`, `LPVS_GITHUB_TOKEN`, `LPVS_GITHUB_API_URL`, `LPVS_GITHUB_SECRET` and `LPVS_LICENSE_CONFLICT`.

#### 4 Build _LPVS_ application with Maven, then run it:
```bash
mvn clean install
cd target/
java -jar lpvs-*.jar
```

When running the application you will also be able to use command line to input all the same values associated with github and license on the fly, like so:
```bash
java -jar -Dgithub.token=<`my-token`> -Dgithub.secret=<`my-secret`> lpvs-*.jar
```
> Tip: for parameter `-Dgithub.secret=`  is recommended to use `LPVS` as `my-secret`.

**Now you can create a new pull request or update it with commits. _LPVS_ will start scanning automatically, then provide comments about the licenses found in the project.**

---

## License

The _LPVS_ source code is distributed under the [MIT](https://opensource.org/licenses/MIT) open source license.

---

## Contributing

You are welcome to contribute to _LPVS_ project. 
Contributing is also a great way to practice social coding at Github, study new technologies and enrich your public portfolio.  
[How to contribute code](.github/CONTRIBUTING.md)  
[How to report a security vulnerability](.github/SECURITY.md)  
