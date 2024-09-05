# Single scan of local files

One-time scan on a specific local files from the command line.

---

You can perform a one-time scan on a specific local file or folder using the single scan mode.
LPVS should be compiled from the source code. Please refer to the [detailed guideline](../../user-guide/service/source_code.md).

Follow the next steps:

* Install LPVS and navigate to the target directory.

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

    By default, the above commands require a pre-configured MySQL database. Use the "singlescan" profile to skip 
    setting up a pre-configured MySQL database:

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
