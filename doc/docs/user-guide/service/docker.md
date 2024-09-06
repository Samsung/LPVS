# Run service using pre-built Docker image

How to download and run pre-built LPVS Docker images without building the LPVS project.

---

## Setting up LPVS Docker environment variables

For the Docker deployment scenario, you may need to fill in the environment variables 
in the `docker-compose.yml` file.

* Open `docker-compose.yml` file.

* In the `environment` part of the `lpvs` service, find `## Github data for fetching code` 
and fill in the GitHub `login` and personal `token` that was generated [earlier](webhook.md#create-a-personal-github-access-token).

    ```yaml
    - github.login=<github-login>
    - github.token=<personal-token>
    ```

* In case you plan to use a database user other than `root` reflect this in the appropriate 
lines in the `## Database Configuration` part of the `lpvs` service in `environment` section.

    ```yaml
    - spring.datasource.username=user
    - spring.datasource.password=password  
    ```

* Make the following changes in the `environment` section of `mysqldb` service near 
`MYSQL_ROOT_PASSWORD` value:

    ```yaml
    - MYSQL_USER: user
    - MYSQL_PASSWORD: password
    ```

!!! note

    If you are using only the `root` user, make the following change:

    ```yaml
    - spring.datasource.username=root
    - spring.datasource.password=rootpassword
    ...
    - MYSQL_ROOT_PASSWORD: rootpassword
    ```

!!! warning

    In both cases, ensure that the `MYSQL_ROOT_PASSWORD` field is filled.

* You can also change the directory for storing MySQL data by modifying the following line:

    ```yaml
    - ./mysql-lpvs-data:/var/lib/mysql
    ```

!!! info

      Database storage by default is a directory in the root of the repository with 
      the name `mysql-lpvs-data`.

---

## Running and stopping LPVS and MySQL Docker images

Start the LPVS services using `docker compose`:

```bash
docker compose up -d
```

Stop the LPVS services using `docker compose`:

```bash
docker compose down
```

You can now create a new pull request or update an existing one with commits. 
If webhook was configured correctly, LPVS will automatically start scanning and 
provide comments about the licenses and conflicts found in the project.
