
![License Pre-Validation Service (_LPVS_)](lpvslogo.png)
[![Build](https://github.com/samsung/lpvs/workflows/Build/badge.svg)](https://github.com/samsung/lpvs/actions?query=workflow%3ABuild)
[![CodeQL Analysis](https://github.com/Samsung/LPVS/workflows/CodeQL%20Analysis/badge.svg)](https://github.com/Samsung/LPVS/actions?query=workflow%3A%22CodeQL+Analysis%22)
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/6309/badge)](https://www.bestpractices.dev/projects/6309)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/Samsung/LPVS/badge)](https://api.securityscorecards.dev/projects/github.com/Samsung/LPVS)
[![Release](https://img.shields.io/github/v/release/samsung/lpvs.svg)](https://github.com/Samsung/LPVS/releases)
[![LICENSE](https://img.shields.io/github/license/samsung/lpvs.svg)](https://github.com/Samsung/LPVS/blob/main/LICENSE)
[![codecov](https://codecov.io/gh/Samsung/LPVS/graph/badge.svg?token=XTD749ITNF)](https://codecov.io/gh/Samsung/LPVS)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7127519.svg)](https://doi.org/10.5281/zenodo.7127519)
[![Project Map](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/samsunglpvs/)

## Introduction
[Open-source code](https://en.wikipedia.org/wiki/Open-source_software) (that is a software that is freely available for use, study, modification, and distribution) must meet conditions of the respective license(s) of all of its dependencies. Miscompliance may lead to legal disputes, fines, obligation to disclose intellectual property, as well as reputational damage.

In projects with numerous external dependencies, it becomes challenging to track license obligations accurately. Also, when many collaborators are involved, the risk of unintentional license violations, such as through copy-pasting code snippets, increases. Furthermore, there are nuanced situations like dependencies with dual licensing or licenses that may change due to ownership, purpose, or legislative alterations. These factors can potentially turn previously safe dependencies into unsafe ones over time.

To address these license-related risks for open-source code, we have developed the _License Pre-Validation Service (LPVS)_. This tool provides a solution to mitigate potential license issues. By analyzing the project, _LPVS_ identifies its components and their respective licenses at every commit. Then it generates a list of potential issue cases, and communicates them to the developers as comments on GitHub. _LPVS_ offers a comprehensive description of possible license violations, including the details on the location of risky code and an overview of the specific license-related issues.

With _LPVS_, we aim at assisting developers and project teams with ensuring license compliance for their open-source code. By providing insights into the potential license violations and their implications, _LPVS_ enables proactive management of license-related risks throughout the development process.

We believe that _LPVS_ will be an invaluable tool for maintaining the integrity of open-source projects and safeguarding against license infringements.

## Features

- License Scanners:

    _LPVS_ integrates with the [SCANOSS](https://www.scanoss.com) license scanner, allowing for comprehensive license analysis of the project's components. SCANOSS helps to identify the licenses associated with the codebase, ensuring the compliance with open-source license requirements. By leveraging SCANOSS, _LPVS_ provides accurate and up-to-date information on the licenses used in the project.

- GitHub Review System Integration:

    _LPVS_ seamlessly integrates with the GitHub review system, enhancing the collaboration and code review process. _LPVS_ automatically generates comments on GitHub, highlighting potential license violations or issues within the codebase. This integration streamlines the review process, making it easier for the developers and collaborators to identify and address license-related concerns directly within the GitHub environment.

- Comprehensive Issue Description:

    _LPVS_ provides a detailed and comprehensive description of possible license violations within the project. This includes specific information on the location of potentially risky code and an overview of the license-related issues at hand. By offering this comprehensive insight, _LPVS_ enables the developers to have a clear understanding of license-related risks within their codebase and to take appropriate steps to mitigate them.

- Continuous Monitoring:

    _LPVS_ facilitates continuous monitoring of license-related risks throughout the development process. By analyzing each commit, _LPVS_ ensures that any changes or additions to the codebase are assessed for potential license violations. This ongoing monitoring allows developers to proactively manage license compliance and address any issues that arise in a timely manner.

- Risk Mitigation:

    _LPVS_ aims at mitigating license-related risks by providing early detection and identification of potential violations. By alerting developers about potential issues and by providing the necessary information to understand and address them, _LPVS_ empowers teams to take proactive steps to ensure compliance with open-source licenses. This helps mitigate the risk of legal disputes, financial liabilities, and reputational damage associated with license violations.

With these features, _LPVS_ assists developers to manage license compliance for their open-source projects effectively. By integration with license scanning tools, supporting the GitHub review system, and providing comprehensive issue descriptions, _LPVS_ offers a robust solution for identifying and addressing license-related risks in the software development lifecycle.

---

## Quick Start Guide & Build

- [_LPVS_ quick start guide](doc/quick-start-guide-and-build.md#quick-start-guide-1)
- [How to Build and Run _LPVS_ from Source Code](doc/quick-start-guide-and-build.md#how-to-build-and-run-lpvs-from-source-code-1)

---

## Frontend Source Code (React)

The frontend of the _LPVS_ project is implemented using React. The corresponding code can be found in the `frontend` folder. For detailed information about the frontend, please refer to the [Frontend README](frontend/README.md).

---

## License

The _LPVS_ source code is distributed under the [MIT](https://opensource.org/licenses/MIT) open source license.

---

## Contributing

You are welcome to contribute to the _LPVS_ project. Contributing is a great way to practice social coding on GitHub, learn new technologies, and enhance your public portfolio. If you would like to contribute, please follow the guidelines below:

- [How to Contribute Code](.github/CONTRIBUTING.md)
- [How to Report a Security Vulnerability](.github/SECURITY.md)
- [Code Review Requirements](doc/code-review-requirements.md)

Thank you for your interest in contributing to _LPVS_! Your contributions are highly appreciated.
