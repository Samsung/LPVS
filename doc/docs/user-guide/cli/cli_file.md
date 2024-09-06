# Single scan of local files

One-time scan on a specific local files from the command line.

---

You can perform a one-time scan on a specific local file or folder using the single scan mode.
LPVS should be compiled from the source code. Please refer to the [detailed guideline](../../user-guide/service/build-and-run.md).

Follow the next steps:

* Make sure that the ScanOSS scanner is installed. If not, please follow 
  the [installation guide](../../user-guide/service/scanner.md).

* Fill in all required properties in the profile file. Please refer to
  the [profile configuration guide](../config/options.md).

* Build and install LPVS, navigate to the target directory. For more details,
  please refer to the [build instructions](../service/build-and-run.md).

```bash
mvn clean install
cd target/
```

* Choose a specific local file or folder which is a target for the scan and run the command with flag `--local.path`:

```bash
java -jar lpvs-*.jar --local.path=</path/to/file/or/folder>
```

Example:

```bash
-jar lpvs-*.jar --local.path=test.java
```

!!! note

    By default, the above commands require a [pre-configured MySQL database](../../user-guide/service/database.md). 
    Use the "singlescan" profile to skip setting up a pre-configured MySQL database:

    ```bash
    java -jar -Dspring.profiles.active=singlescan lpvs-*.jar --local.path=</path/to/file/or/folder>
    ```

* Optionally, generate an HTML report and save it in a specified folder using flag `--build.html.report`.
  Replace `path/to/your/folder` with the full path to the folder where you want to save the HTML report,
  and `your_report_filename.html` with the desired filename for the report.

```bash
java -jar -Dspring.profiles.active=singlescan lpvs-*.jar --local.path=</path/to/file/or/folder> --build.html.report=<your_report_filename.html>
```

!!! warning

    Ensure that the specified folder exists before generating the HTML report.

Examples of the command:

```bash
java -jar -Dspring.profiles.active=singlescan lpvs-*.jar --local.path=test.c
java -jar -Dspring.profiles.active=singlescan lpvs-*.jar --local.path=test_folder --build.html.report=test/report.html
```
