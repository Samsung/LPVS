
![License Pre-Validation Service (LPVS)](lpvslogo.png)
[![Build](https://github.com/samsung/lpvs/workflows/Build/badge.svg)](https://github.com/samsung/lpvs/actions?query=workflow%3ABuild)

## Introduction
OpenSource code [refers](https://en.wikipedia.org/wiki/Open-source_software) to software available for use, study, change, and distribution by anyone and for any purpose provided that the corresponding license conditions are met. License violation may end up with litigations, damage compensation, obligation to disclose intellectual property as well as reputational losses. 

In a project with many external dependencies it might be really difficult to trace license obligations. Also if many collaborators are involved, a risk of non-intentional license violation (such as via Copy-Paste) grows. There are even more tricky nuances such as double-licensed dependencies or license change (because of owner, purpose, legislation change) that may make a previously safe dependency to become an unsafe one over time.

License Pre-Validation Service (LPVS) helps to mitigate license-related risks for OpenSource code. The tool analyzes the project, identifies its components and their respective licenses at every commit. Then it returns the list of potential issue cases as GitHub comments. LPVS provides the comprehensive description of possible license violations, including  risky code location and license issue overview.

## Features

- available license scanners: [SCANOSS](https://www.scanoss.com)
- LPVS supports GitHub review system

## LPVS GitHub Integration

LPVS license scan shall be enabled on a project via GitHub Hooks:

1. In `src/main/resources/application.properties` specify the account to be used for posting scan results as a review message. The following fields should be filled: `github.token`.

2. Add the user specified in `github.token` as a collaborator to your GitHub project.

3. Configure webhook in your GitHub repository settings:
- go to `Settings` -> `Hooks`
- press `Add webhook`
- fill in Payload URL with: `http://<IP where LPVS is running>:7896/webhooks`
- specify content type: `application/json`
- fill in `Secret` field with the passphrase: `LPVS`
- select `Let me select individual events` -> `Pull requests` (make sure that only `Pull requests` is selected)
- make it `Active`
- press `Add Webhook`
    
Create a new pull request and update it with commits. 
LPVS will start scanning automatically, then provide comments about the licenses found in the project. 

## LPVS Backend Configuration

1. Install SCANOSS Python package by following the [guideline](https://github.com/scanoss/scanoss.py#installation).

2. Fill in the lines of the `src/main/resources/application.properties` file:
    ```text
   # Used license scanner
    scanner=scanoss
   # Used license conflicts source (take from 'licenses.json' ("json") 
   # or from scanner response("scanner"))
    license_conflict=json
    ```

3. Fill in `src/main/resources/licenses.json` file with the information about permitted, restricted, and prohibited licenses as well as their compatibility specifics. An example of the `licenses.json` file can be found in the repository.
   
4. Build LPVS application with Maven, then run it:
    ```bash
    mvn clean install
    cd target/
    java -jar lpvs-1.0.0.jar
    ```

   Or alternatively build and run the Docker container with LPVS:
   ```bash
    docker build -t lpvs .
    docker run -p 7896:7896 --name lpvs lpvs:latest
    ```
    For additional information about using Docker and tips, please check file [Docker_Usage](.github/Docker_Usage.md).
    
5. Install [ngrok](https://dashboard.ngrok.com/get-started) (step 1 and 2) and run it with the following command:
    ```bash
    ./ngrok http 7896 
    ```    
At this point LPVS is ready for work.   

## License

The LPVS source code is distributed under the [MIT](https://opensource.org/licenses/MIT) open source license.

## Contributing

You are welcome to contribute to LPVS project. 
Contributing is also a great way to practice social coding at Github, study new technologies and enrich your public portfolio.
