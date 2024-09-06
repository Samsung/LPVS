# Build and run service from the source code

Basic steps to build and run LPVS.

---

To build and run the LPVS application you have to follow the next steps:

* Fill in the properties file according to the selected profile with your own data. Refer to the [guideline](../config/options.md#properties-in-the-application-profile-files).

!!! note

    Alternatively, you can provide the necessary values using the [environment variables](../config/options.md#environment-variables),
    or using [command line options](../config/options.md#command-line-options).

* Build the LPVS application using [Maven](https://maven.apache.org/).

```bash
mvn clean install
```

* Navigate to the target directory.

```bash
cd target/
```

* Run the LPVS service using the following command:

```bash
java -jar lpvs-*.jar
```

LPVS is now built and running. You can create a new pull request or update an existing one with commits, 
and LPVS will automatically start scanning and provide comments about the licenses found in the project.

!!! warning

    Make sure that you configured GitHub webhooks according to the [guideline](webhook.md).
