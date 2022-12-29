
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
  - the same passphrase must be saved in `github.secret` of LPVS backend `application.properties` file
- select `Let me select individual events` -> `Pull requests` (make sure that only `Pull requests` is selected)
- make it `Active`
- press `Add Webhook`
    
Create a new pull request and update it with commits. 
LPVS will start scanning automatically, then provide comments about the licenses found in the project. 

## LPVS Backend Configuration

1. Install SCANOSS Python package by following the [guideline](https://github.com/scanoss/scanoss.py#installation).

2. Fill in `licenses.json` file with the information about permitted, restricted, and prohibited licenses (mandatory) as well as their compatibility specifics (optional). 
A template of the `licenses.json` file can be found in the repository at `src/main/resources/licenses.json`.

3. Fill in the lines of the `src/main/resources/application.properties` file:
    ```text
   # Fill in the properties associated with github (github.token and github.secret required).
   github.token=
   github.login=
   github.api.url=
   github.secret=LPVS

   # Used license scanner: scanoss (at the moment, only this scanner is supported)
    scanner=scanoss

   # Path to the 'licenses.json' file which contains information about permitted,
   # restricted and prohibited licenses. This file should be filled according to
   # the template which could be found at 'src/main/resources/licenses.json'
    license_filepath=

   # Used license conflicts source:
   # > option "json": take conflicts from 'licenses.json' (should be filled manually
   # according to the template at 'src/main/resources/licenses.json')
   # > option "scanner": take conflicts from the scanner response
    license_conflict=json
    ```

   Alternatively, you can supply all the necessary values associated with GitHub and license using these env variables:
   `LPVS_GITHUB_LOGIN`, `LPVS_GITHUB_TOKEN`, `LPVS_GITHUB_API_URL`, `LPVS_GITHUB_SECRET`, `LPVS_LICENSE_FILEPATH` and `LPVS_LICENSE_CONFLICT`.

4. Build LPVS application with Maven, then run it:
    ```bash
    mvn clean install
    cd target/
    java -jar lpvs-1.0.1.jar
    ```

   When running the application you will also be able to use command line to input all the same values associated with github and license on the fly, like so:
   ```bash
   java -jar -Dgithub.token=<`my-token`> -Dgithub.secret=<`my-secret`> lpvs-1.0.1.jar
   ```

   Or alternatively build and run the Docker container with LPVS:
   ```bash
    docker build -t lpvs .
    docker run -p 7896:7896 --name lpvs -e LPVS_GITHUB_TOKEN=<`github.token`> -e LPVS_GITHUB_SECRET=<`github.secret`> lpvs:latest
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
[How to contribute code](.github/CONTRIBUTING.md)  
[How to report a security vulnerability](.github/SECURITY.md)  
