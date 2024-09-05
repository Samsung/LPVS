![Logo for light mode](img/lpvs-light.png)

# License Pre-Validation Service (LPVS)

---
## Introduction
[Open-source code](https://en.wikipedia.org/wiki/Open-source_software) (that is a software that is freely available for use, study, modification, and distribution) must meet conditions of the respective license(s) of all of its dependencies. Miscompliance may lead to legal disputes, fines, obligation to disclose intellectual property, as well as reputational damage.

In projects with numerous external dependencies, it becomes challenging to track license obligations accurately. Also, when many collaborators are involved, the risk of unintentional license violations, such as through copy-pasting code snippets, increases. Furthermore, there are nuanced situations like dependencies with dual licensing or licenses that may change due to ownership, purpose, or legislative alterations. These factors can potentially turn previously safe dependencies into unsafe ones over time.

To address these license-related risks for open-source code, we have developed the _License Pre-Validation Service (LPVS)_. This tool provides a solution to mitigate potential license issues. By analyzing the project, _LPVS_ identifies its components and their respective licenses at every commit. Then it generates a list of potential issue cases, and communicates them to the developers as comments on GitHub. _LPVS_ offers a comprehensive description of possible license violations, including the details on the location of risky code and an overview of the specific license-related issues.

With _LPVS_, we aim at assisting developers and project teams with ensuring license compliance for their open-source code. By providing insights into the potential license violations and their implications, _LPVS_ enables proactive management of license-related risks throughout the development process.

We believe that _LPVS_ will be an invaluable tool for maintaining the integrity of open-source projects and safeguarding against license infringements.

---
## Features

- **License Scanners**:

  _LPVS_ integrates with the [SCANOSS](https://www.scanoss.com) license scanner, allowing for comprehensive license analysis of the project's components. SCANOSS helps to identify the licenses associated with the codebase, ensuring the compliance with open-source license requirements. By leveraging SCANOSS, _LPVS_ provides accurate and up-to-date information on the licenses used in the project.

- **GitHub Review System Integration**:

  _LPVS_ seamlessly integrates with the GitHub review system, enhancing the collaboration and code review process. _LPVS_ automatically generates comments on GitHub, highlighting potential license violations or issues within the codebase. This integration streamlines the review process, making it easier for the developers and collaborators to identify and address license-related concerns directly within the GitHub environment.

- **Continuous Monitoring**:

  _LPVS_ facilitates continuous monitoring of license-related risks throughout the development process. By analyzing each commit, _LPVS_ ensures that any changes or additions to the codebase are assessed for potential license violations. This ongoing monitoring allows developers to proactively manage license compliance and address any issues that arise in a timely manner.

- **Risk Mitigation**:

  _LPVS_ aims at mitigating license-related risks by providing early detection and identification of potential violations. By alerting developers about potential issues and by providing the necessary information to understand and address them, _LPVS_ empowers teams to take proactive steps to ensure compliance with open-source licenses. This helps mitigate the risk of legal disputes, financial liabilities, and reputational damage associated with license violations.

With these features, _LPVS_ assists developers to manage license compliance for their open-source projects effectively. By integration with license scanning tools, supporting the GitHub review system, and providing comprehensive issue descriptions, _LPVS_ offers a robust solution for identifying and addressing license-related risks in the software development lifecycle.
