# Single scan of the pull request

One-time scan on a specific pull request from the command line.

---

You can perform a one-time scan on a specific pull request using the single scan mode. 
LPVS should be compiled from the source code. Please refer to the [detailed guideline](../../user-guide/service/source_code.md).

Follow the next steps:

* Install LPVS and navigate to the target directory.

```bash
mvn clean install
cd target/
```

* Choose a specific pull request from GitHub which is a target for the scan and run the command with flag `--github.pull.request`:

```bash
java -jar -Dgithub.token=<my-token> lpvs-*.jar --github.pull.request=<PR URL>
```

Example: 

```bash
-jar -Dgithub.token=your_personal_token lpvs-*.jar --github.pull.request=https://github.com/Samsung/LPVS/pull/594
```

!!! note

    By default, the above commands require a pre-configured MySQL database. Use the "singlescan" profile to skip 
    setting up a pre-configured MySQL database:

    ```bash
    java -jar -Dspring.profiles.active=singlescan -Dgithub.token=<my-token> lpvs-*.jar --github.pull.request=<PR URL>
    ```

* Optionally, generate an HTML report and save it in a specified folder using flag `--build.html.report`. 
Replace `path/to/your/folder` with the full path to the folder where you want to save the HTML report, 
and `your_report_filename.html` with the desired filename for the report.

```bash
java -jar -Dspring.profiles.active=singlescan -Dgithub.token=<my-token> lpvs-*.jar --github.pull.request=<PR URL> --build.html.report=</path/to/your/folder/your_report_filename.html>
```

!!! warning

    Ensure that the specified folder exists before generating the HTML report.

Examples of the command:

```bash
java -jar -Dspring.profiles.active=singlescan lpvs-*.jar --github.pull.request=https://github.com/Samsung/LPVS/pull/2
java -jar -Dspring.profiles.active=singlescan lpvs-*.jar --github.pull.request=https://github.com/Samsung/LPVS/pull/2 --build.html.report=report.html
```
