# Testing policy
## Contents
1. [Introduction](#1-introduction)
2. [How to start Test Suite (Local)](#2-how-to-start-test-suite-local)  
3. [Automated Run Test Suite (Remote)](#3-automated-run-test-suite-remote)  

---

## 1. Introduction

Testing is a very important part of LPVS project and our team values it highly.

**When adding or changing functionality, the main requirement is to include new tests as part of your contribution.**
> If your contribution does not have the required tests, please mark this in the PR and our team will support to develop them.

The LPVS team strives to maintain test coverage of **at least 70%**. We ask you to help us keep this minimum. Additional tests are very welcome.

The LPVS team strongly recommends adhering to the [Test-driven development (TDD)](https://en.wikipedia.org/wiki/Test-driven_development) as a software development process.

---

## 2. How to start Test Suite (Local)

To build and testing all packages:
```
mvn -B package -Pcoverage --file pom.xml
```
To start testing all packages:
```
mvn test
```

---

## 3. Automated Run Test Suite (Remote)

Code testing occurs remotely using a [github->actions->workflow (Test-suite)](https://github.com/Samsung/LPVS/actions/workflows/test-suite.yml) during each `push` or `pull_request`.

> [More information on github->actions](https://docs.github.com/en/actions) 

---
