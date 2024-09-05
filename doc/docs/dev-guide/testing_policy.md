# Testing policy

How to check verify correct work of LPVS.

---

## Introduction

Testing is a very important part of LPVS project and our team values it highly.

**When adding or changing functionality, the main requirement is to include new tests as part of your contribution.**

!!! note

    If your contribution does not have the required tests, please mark this in the PR and our team will support 
    to develop them.

The LPVS team strives to maintain test coverage of **at least 70%**. We ask you to help us keep this minimum. 
Additional tests are very welcome.

The LPVS team strongly recommends adhering to the [Test-driven development (TDD)](https://en.wikipedia.org/wiki/Test-driven_development) as a software development 
process.

---

## How to start Test Suite (Local)

To build and testing all packages:

```bash
mvn -B package -Pcoverage --file pom.xml
```

To start testing all packages:

```bash
mvn test
```

---

## Automated Run Test Suite (Remote)

Code testing occurs remotely using an [Actions -> Workflow Test-suite](https://github.com/Samsung/LPVS/actions/workflows/test-suite.yml) 
during each `push` or `pull_request`.

!!! info

    Find more information on [GitHub Actions documentation](https://docs.github.com/en/actions) 
