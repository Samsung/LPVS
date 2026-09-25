# Run service using pre-built Docker image

How to download and run pre-built LPVS Docker images without building the LPVS project.

---

## Setting up LPVS Docker environment variables

For the Docker deployment scenario, you need to provide database credentials and
GitHub settings before starting the containers.

* Copy `.env.example` to `.env` in the same directory as `docker-compose.yml`, and
fill in strong, unique values for `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD` and `LPVS_GITHUB_SECRET`:

    ```bash
    cp .env.example .env
    ```

    `docker compose` refuses to start if these are left empty, so there is no
    insecure default to forget to change. `MYSQL_USER` defaults to `lpvs`, a
    dedicated database account scoped to the `lpvs` schema only — the application
    does not need and does not use the MySQL `root` account.

!!! warning

    Never commit `.env` to version control, and do not publish MySQL's port
    (3306) to the host unless you have a specific need for external access —
    the default `docker-compose.yml` keeps it internal to the `lpvs` network.

* Open `docker-compose.yml` file.

* In the `environment` part of the `lpvs` service, find `## Github data for fetching code`
and fill in the GitHub `login` and personal `token` that was generated [earlier](webhook.md#create-a-personal-github-access-token).

    ```yaml
    - github.login=<github-login>
    - github.token=<personal-token>
    ```

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
