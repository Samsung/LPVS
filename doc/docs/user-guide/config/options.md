# Configuration options

Description of the meaningful properties and its possible values.

---

## Properties in the application profile files

There are three profiles available in the LPVS application:

- `application.properties`: default profile which contains all main properties of the application
- `application-singlescan.properties`: profile for single scan mode
- `application-web.properties`: profile for connecting the frontend application to the backend

---

### `application.properties` profile

- `debug`: This setting determines whether the application runs in debug mode. If set to false, 
the application will run in normal execution mode. Default: `false`.

- `server.port`: This setting specifies the port number used by the application. Default: `7896`.

- `scanner`: This setting specifies the name of the scanning tool being used. Currently supporting 
[ScanOSS](https://github.com/scanoss). Default: `scanoss`.

- `license_conflict`: This setting specifies where the license conflict information comes from. 
It can be either `db` (from the manually filled table in database, refer to the 
[Database guide](database.md#table-lpvs_license_conflicts)) or `scanner` (if supported by scanner).
Default: `db`.

- `license_source`: This setting specifies the URL of the external [OSORI](https://osori-db.github.io/en/) 
license database. Default: `https://olis.or.kr:8082`.

- `spring.profiles.active`: This setting specifies which Spring profile is active.

- `github.*`: These settings include various configurations related to GitHub such as login, token, API URL.
Fill in your own values for these settings. Please refer to the [Personal token creation guide](../service/webhook.md#create-a-personal-github-access-token)
for more details.

- `github.secret`: This setting specifies the secret string used for configuring webhooks. Please refer to the
[Webhook configuration guide](../service/webhook.md#configure-the-webhook-in-your-github-repository-settings) 
for more details. Default: `LPVS`.

- `lpvs.*`: These settings include various configurations specific to the LPVS application like core pool size, 
number of scan attempts, and version.

- `spring.jpa.properties.hibernate.default_schema`: This setting specifies the default schema name that Hibernate 
should use. Default: `lpvs`.

- `spring.datasource.*`: These settings specify the data source configurations including URL, username, password, etc.
Fill in your own values for these settings.

---

### `application-singlescan.properties` profile

- `spring.sql.init.data-locations`: This setting specifies the location of the SQL script files that will 
be executed when initializing the database. By default, it is set to `classpath*:database_dump.sql`, indicating 
that the default dump file named `database_dump.sql` should be found in any package within the classpath.

- `spring.datasource.username`: This setting specifies the username for accessing the datasource. By default, 
it is left blank, indicating that no username is required for authentication.

- `spring.datasource.password`: This setting specifies the password for accessing the datasource. Again, 
it is left blank here, indicating that no password is needed for authentication.

---

### `application-web.properties` profile

These properties configure OAuth2 clients for different providers such as Google, Naver, and Kakao. 
For each provider, client ID, client secret, redirect URI, authorization grant type, scope, and other 
relevant details should be specified. Additionally, there are frontend configuration options for 
specifying the main page URL and allowed origins for CORS. Overall, these properties enable 
integration with multiple authentication providers and provide flexibility in handling user authentication.

!!! warning 

    All properties in `application-web.properties` profile must be filled in case of connecting some 
    frontend application. If you don't need to connect any frontend application, you can ignore this file.

!!! info

    Sample frontend application is available at [LPVS repository](https://github.com/Samsung/LPVS/tree/main/frontend)
    and can be used as a reference.

---

## Command line options

All missing properties from application profiles can be specified via command line options. 
For example:

```bash
java -jar -Dgithub.token=<my-token> -Dgithub.secret=<my-secret> lpvs-*.jar
```

But there are several options that are not supported by this method and only must be set for the one-time scans
in command line mode.

The following command line options are available:

- `--build.html.report`: This setting specifies the path to the HTML report file which will be generated after the scan.
If it is not specified, no HTML report will be generated and result of the scan will be displayed in the console.

- `--github.pull.request`: This setting specifies the pull request URL which should be scanned by the LPVS application.

- `--local.path`: This setting specifies the path to the local file or folder which should be scanned by the LPVS application.

!!! warning

    Options `--github.pull.request` and `--local.path` can't be use simultaneously. If both options are specified,
    LPVS application will throw an exception and exit with error code.

---

## Environment variables

Alternatively, you can provide the necessary values for several properties using the following environment variables: 

- `LPVS_GITHUB_LOGIN`: Equivalent to the property `github.login`.
- `LPVS_GITHUB_TOKEN`: Equivalent to the property `github.token`.
- `LPVS_GITHUB_API_URL`: Equivalent to the property `github.api.url`.
- `LPVS_GITHUB_SECRET`: Equivalent to the property `github.secret`.
- `LPVS_LICENSE_CONFLICT`: Equivalent to the property `license_conflict`.
