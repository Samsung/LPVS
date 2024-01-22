# Quick Start Guide & Build

## Contents
 ### [Quick Start Guide](#quick-start-guide-1)
  1. [Setting up your project to interact with _LPVS_](#1-setting-up-your-project-to-interact-with-lpvs)  
  2. [Using pre-built _LPVS_ Docker images](#2-using-pre-built-lpvs-docker-images)  

 ### [How to Build and Run _LPVS_ from Source Code](#how-to-build-and-run-lpvs-from-source-code-1)
  1. [Build Prerequisites](#1-build-prerequisites)  
  2. [Create Necessary MySQL Database and User (optional if not using a database)](#2-create-necessary-mysql-database-and-user-optional-if-not-using-a-database)  
  3. [Setting up _LPVS_ application.properties](#3-setting-up-lpvs-applicationproperties)  
  4. [Build _LPVS_ Application with Maven and Run it](#4-build-lpvs-application-with-maven-and-run-it)
    
---

## Quick Start Guide

### 1. Setting up your project to interact with _LPVS_

To enable _LPVS_ license scanning for your project, you need to set up GitHub Webhooks:

1.1 Create a personal github access token (`personal-token`):
   - Follow the instructions [here](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-fine-grained-personal-access-token) to create a personal access token with the necessary permissions.

   > [!NOTE]  
   > Pay attention that the token must be copied immediately after creation, because you will not be able to see it later!!

2.1 Configure the webhook in your GitHub repository settings:
   - Go to `Settings` -> `Webhooks`.
   - Click on `Add webhook`.
   - Fill in the `Payload URL` with: `http://<IP where LPVS is running>:7896/webhooks`.
     > If you're using ngrok, the `Payload URL` should be like `https://50be-62-205-136-206.ngrok-free.app/webhooks`.
     - Install ngrok and connect your account from [here](https://ngrok.com/docs/getting-started/#step-2-install-the-ngrok-agent) (follow steps 1 and 2).
     - Run ngrok using the command: `ngrok http 7896`.
   - Specify the content type as `application/json`.
   - Fill in the `Secret` field with the passphrase: `LPVS`.
   - Save the same passphrase in `github.secret` of the _LPVS_ backend `application.properties` or `docker-compose.yml` files.
   - Select `Let me select individual events` -> `Pull requests` (make sure only `Pull requests` is selected).
   - Set the webhook to `Active`.
   - Click `Add Webhook`.

Configuration from your project side is now complete!

Alternatively, you can use the Pull Request Single Scan API to analyze the code of a specific pull request.
Please refer to the  [API Documentation](doc/lpvs-api.yaml) for more information.

---

### 2. Using pre-built _LPVS_ Docker images

This section explains how to download and run pre-built _LPVS_ Docker images without building the _LPVS_ project.

For the Docker deployment scenario, you may need to fill in the environment variables in the `docker-compose.yml` file.

#### 2.1 Setting up _LPVS_ Docker environment variables

2.1.1 Open `docker-compose.yml` file.

2.1.2 In the `environment` part of the `lpvs` service, find `## Github data for fetching code` and fill in the github `login` and personal `token` that was generated earlier

```yaml
- github.login=<github-login>
- github.token=<personal-token>
```

2.1.3 In case you plan to use a database user other than `root` reflect this in the appropriate lines in the `## Database Configuration` part of the `lpvs` service in `environment` section:

```yaml
- spring.datasource.username=user
- spring.datasource.password=password  
```

2.1.4 Make the following changes in the `environment` section of `mysqldb` service near `MYSQL_ROOT_PASSWORD` value:

```yaml
- MYSQL_USER: user
- MYSQL_PASSWORD: password
```

If you are using only the `root` user, make the following change:

```yaml
- spring.datasource.username=root
- spring.datasource.password=rootpassword
```
```yaml 
- MYSQL_ROOT_PASSWORD: rootpassword
```

In both cases, ensure that the `MYSQL_ROOT_PASSWORD` field is filled.

2.1.5 You can also change the directory for storing MySQL data by modifying the following line:

```yaml
- ./mysql-lpvs-data:/var/lib/mysql # db storage by default it is a directory in the root of the repository with the name mysql-lpvs-data
```

#### 2.2 Running _LPVS_ and MySQL Docker images with Docker Compose

Start the _LPVS_ services using `docker-compose` (before Compose V2):

```bash
docker-compose up -d
```

Start the _LPVS_ services using `docker compose` (after Compose V2):

```bash
docker compose up -d
```

Stop the _LPVS_ services using `docker-compose` (before Compose V2):

```bash
docker-compose down -v --rmi local
```

Stop the _LPVS_ services using `docker compose` (after Compose V2):

```bash
docker compose down
```

You can now create a new pull request or update an existing one with commits. _LPVS_ will automatically start scanning and provide comments about the licenses found in the project.

---
   
## How to Build and Run _LPVS_ from Source Code

### 1. Build Prerequisites

Before building _LPVS_ from source code, ensure that you have the following prerequisites installed:

- SCANOSS Python package by following the [guidelines](https://github.com/scanoss/scanoss.py#installation). Install it using the command:
  ```bash
  pip3 install scanoss
  ```
  Make sure that the path variable is added to the environment:
  ```bash
  export PATH="$HOME/.local/bin:$PATH"
  ```

- MySQL server installed locally. Install it using the command:
  ```bash
  sudo apt install mysql-server
  ```

### 2. Create Necessary MySQL Database and User (optional if not using a database)

2.1 Start the MySQL server:
   ```bash
   sudo service mysql start
   ```

2.2 Open the MySQL command line interface:
   ```bash
   sudo mysql
   ```

2.3 Run the following commands in the MySQL command line interface to create the necessary database and user:
   ```sql
   mysql> create database lpvs;
   mysql> create user username;
   mysql> grant all on lpvs.* to username;
   mysql> alter user username identified by 'password';
   mysql> exit;
   ```

2.4 (Optional) If you have an existing dump file, import it into the newly created database using the command:
   ```bash
   mysql -u[username] -p[password] < src/main/resources/database_dump.sql
   ```

2.5 Fill in the `licenses` and `license_conflicts` tables with the information about permitted, restricted, and prohibited licenses, as well as their compatibility specifics. You can find an example database dump file in the repository at [`src/main/resources/database_dump.sql`](src/main/resources/database_dump.sql).

2.6 Update the following lines in the [`src/main/resources/application.properties`](src/main/resources/application.properties) file:
   ```properties
   spring.datasource.username=username
   spring.datasource.password=password
   ```

### 3. Setting up _LPVS_ `application.properties`

Fill in the following lines in the [`src/main/resources/application.properties`](src/main/resources/application.properties) file:

```properties
# GitHub configuration (github.token and github.secret required)
github.token=
github.login=
github.api.url=
github.secret=LPVS
```
   > [!NOTE]  
   > For personal GitHub account use  `https://api.github.com`  in field `github.api.url=`.  
   
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

Alternatively, you can provide the necessary values using the following environment variables: `LPVS_GITHUB_LOGIN`, `LPVS_GITHUB_TOKEN`, `LPVS_GITHUB_API_URL`, `LPVS_GITHUB_SECRET`, and `LPVS_LICENSE_CONFLICT`.

### 4. Build _LPVS_ Application with Maven and Run it

#### 4.1 Service mode (default)

To build _LPVS_ from source code and run it in the default service mode, follow these steps:

4.1.1 Build the _LPVS_ application using Maven:
   ```bash
   mvn clean install
   ```

4.1.2 Navigate to the target directory:
   ```bash
   cd target/
   ```

4.1.3 Run the _LPVS_ application.

   Service is run using the following command:
   ```bash
   java -jar lpvs-*.jar
   ```

   Alternatively, you can provide the necessary values associated with GitHub and license using the command line:
   ```bash
   java -jar -Dgithub.token=<my-token> -Dgithub.secret=<my-secret> lpvs-*.jar
   ```
   > [!NOTE]  
   > Use `LPVS` as the value for the `-Dgithub.secret=` parameter.

_LPVS_ is now built and running. You can create a new pull request or update an existing one with commits, and _LPVS_ will automatically start scanning and provide comments about the licenses found in the project.

#### 4.2 Single scan mode

Alternatively, you can perform a one-time scan on a specific pull request using the single scan mode. Follow these steps:

4.2.1 Begin by running the installation and navigating to the target directory, similar to the process in service mode (refer to steps 4.1.1 and 4.1.2):

   ```bash
   mvn clean install
   cd target/
   ```

4.2.2 Execute the single scan with the following command:

   ```bash
   java -jar -Dgithub.token=<my-token> lpvs-*.jar --github.pull.request=<PR URL>
   ```

4.2.3 By default, the above command requires a pre-configured MySQL database. To avoid setting up the database, use the "singlescan" profile:
   ```bash
   java -jar -Dspring.profiles.active=singlescan -Dgithub.token=<my-token> lpvs-*.jar --github.pull-request=<PR URL>
   ```

These steps streamline the process, allowing you to run a scan on a single pull request without the need for a preconfigured database.

4.2.4 Available option to generate an HTML report and save it in a specified folder. Replace `/path/to/your/folder` with the full path to the folder where you want to save the HTML report, and `your_report_filename.html` with the desired filename for the report.
   ```bash
   java -jar -Dgithub.token=<my-token> lpvs-*.jar --github.pull.request=<PR URL> --build.html.report=</path/to/your/folder/your_report_filename.html>
   ```

These steps streamline the process, allowing you to run a scan on a single pull request without the need for a preconfigured database.

#### 4.3 Use of _LPVS_ JAR `lpvs-x.y.z.jar` in your project

4.3.1 Authenticating with a personal access token

You can authenticate to GitHub Packages with Apache Maven by editing your `~/.m2/settings.xml` file to include your personal access token
> [!NOTE]  
> Create a token with minimally sufficient rights:
>  - Fine-grained tokens **(recommended)**  
      _Only select repositories -> Permissions -> Repository permissions -> Metadata -> Read-only_
>  - Tokens (classic)  
      _Select scopes -> read:packages_

Create a new `~/.m2/settings.xml` file if one doesn't exist.

Example `settings.xml`
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
         <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/samsung/lpvs</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>USERNAME</username>
      <password>TOKEN</password>
    </server>
  </servers>
</settings>
```
> [!NOTE]  
> if your `settings.xml` file is not located in `~/.m2/settings.xml`, then you need to add the `-s path/to/file/settings.xml` option to `mvn` command

4.3.2 Installing a package

Edit the `<dependencies>` element of the `pom.xml` file located in your project directory.

```
...
    <dependencies>
        <dependency>
            <groupId>com.lpvs</groupId>
            <artifactId>lpvs</artifactId>
            <version>x.y.z</version>
        </dependency>
    </dependencies>
...
```