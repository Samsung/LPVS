# Security Policy

## Table of Contents
1. [Introduction](#1-introduction)
2. [Vulnerabilities](#2-vulnerabilities)  
    2.1 [Supported Versions](#21-supported-versions)  
    2.2 [Vulnerability Report](#22-vulnerability-report)  
    2.3 [Security Disclosure](#23-security-disclosure)  
3. [Security requrements](#3-security_requirements)
4. [Security Software life cycle processes](#4-security-software-life-cycle-processes)

## 1. Introduction

This document outlines the procedures for addressing vulnerabilities, the supported versions of LPVS, security requirements, and the recommended practices for developing secure code.

---

## 2. Vulnerabilities

### 2.1 Supported Versions

We provide patches to address vulnerabilities for the following versions of LPVS:

| Version     | Supported by | LPVS               | 3-rd party component                           |
| ----------- | ------------ | ------------------ | ---------------------------------------------- |
| 1.0.0       | N/A          |                    |                                                |

### 2.2 Vulnerability Report

The LPVS team takes security bugs seriously and gives them the highest priority. We appreciate your responsible disclosure of security-related information to help us address vulnerabilities.

To report security bugs, please email the LPVS Security Issue Review (SIR) team at _o.kopysov@samsung.com_ with the subject line "SECURITY". Our team will acknowledge your report and aim to provide recommendations for mitigation within 1 week. Throughout the process, we will keep you informed of the progress towards the fix and may request additional information or guidance.

### 2.3 Security Disclosure

Once a security vulnerability is reported to the LPVS team, it is treated with the highest priority. The assigned person will coordinate the patch and release process, which includes the following steps:

- Confirm the problem and identify the affected versions.
- Conduct a thorough code review to identify any similar issues.
- Prepare fixes for all supported versions. The fixes will be released as soon as possible.

When disclosing vulnerabilities, we recommend following this format:

- Provide your name and email address.
- Clearly define the scope of the vulnerability and identify potential exploiters.
- Document the steps to reproduce the vulnerability to help us validate and address it effectively.
- Describe the exploitation scenario to understand the impact and severity.

We appreciate your collaboration in making LPVS more secure.

If you have any further questions or concerns, please reach out to us.

Note: This security policy is subject to change and may be updated without notice.

---

## 3. Security requrements

```plantuml
@startuml

left to right direction
usecase "Security requirements"     #palegreen;line:black
usecase Confidentiality     as Co   #lightblue;line:black
usecase Integrity           as In   #lightblue;line:black
usecase Availability        as Av   #lightblue;line:black
usecase "Access control"    as Ac   #lightblue;line:black
usecase Identification              #lightblue;line:black
usecase Authentication              #lightblue;line:black
usecase Authorization               #lightblue;line:black
usecase Non                         #lightblue;line:black as "Non-public data 
    is kept confidential"
usecase "User privacy maintaned"    #lightblue;line:black
usecase "All data is confidential"  #lightblue;line:black
usecase "HTTPS: data in motion"     #lightblue;line:black
usecase "Authorization via GITHUB"  #lightblue;line:black
usecase Dtm                         #lightblue;line:black as "Data modification
    requires authorization"
usecase "Multiple backups"          #lightblue;line:black
usecase "Rerstore after DDoS"       #lightblue;line:black


(Security requirements) <-- (Co)    #line:black;line.bold
(Security requirements) <-- (In)    #line:black;line.bold
(Security requirements) <-- (Av)    #line:black;line.bold
(Security requirements) <-- (Ac)    #line:black;line.bold

(Ac) <-- (Identification)           #line:black
(Ac) <-- (Authentication)           #line:black
(Ac) <-- (Authorization)            #line:black
(Co) <-- (User privacy maintaned)   #line:black
(Co) <-- (Non)                      #line:black
(Co) <-- (All data is confidential) #line:black
(Co) <-- (HTTPS: data in motion)    #line:black
(In) <-- (HTTPS: data in motion)    #line:black
(In) <-- (Authorization via GITHUB) #line:black
(In) <-- (Dtm)                      #line:black
(Av) <-- (Multiple backups)         #line:black
(Av) <-- (Rerstore after DDoS)      #line:black

@enduml
```

---

## 4. Security Software life cycle processes
```plantuml
@startuml

left to right direction
usecase SSLCP           #palegreen;line:black   as  "Security Software
    life cycle processes"
usecase "Certification & Controls"      as CC       #lightblue;line:black
usecase CBPB            #lightblue;line:black   as  "CII Best 
    Practices badge"
usecase "OpenSSF Score Card"            as OSSFSC   #lightblue;line:black
usecase "Security in maintenance"       as SM       #lightblue;line:black
usecase ADPV            #lightblue;line:black   as  "Auto-detect publicy
    vulnerabilities"
usecase "Rapid update"                  as RU       #lightblue;line:black
usecase KDKDSS          #lightblue;line:black   as  "Key developers know how to
    develop secure software"
usecase "Infrastructure management"     as IM       #lightblue;line:black
usecase DTEPA           #lightblue;line:black   as  "Development & test
    environments protected
    from attack"
usecase CIATEP          #lightblue;line:black   as  "CI automated test
    environment does not have
    protected data"
usecase SIV             #lightblue;line:black   as  "Security in integration
    & verification"
usecase "Style checking tools"          as SCT      #lightblue;line:black
usecase SCWA            #lightblue;line:black   as  "Source code
    weakness analyzer"
usecase FLOSS           #lightblue;line:black
usecase "Negative Testing"              as NT       #lightblue;line:black
usecase UTC             #lightblue;line:black   as  "Unit Test
    coverage >75%"
usecase "Security in design"            as SD       #lightblue;line:black
usecase "Simple design"                 as SID      #lightblue;line:black
usecase "Memory-safe languages"         as MSL      #lightblue;line:black
usecase SDISS           #lightblue;line:black   as  "Secure disign
    includes S&S"


(SSLCP) <-- (CC)                    #line:black;line.bold
(SSLCP) <-- (SM)                    #line:black;line.bold
(SSLCP) <-- (KDKDSS)                #line:black;line.bold
(SSLCP) <-- (SIV)                   #line:black;line.bold
(SSLCP) <-- (IM)                    #line:black;line.bold
(SSLCP) <-- (SD)                    #line:black;line.bold

(CC)    <-- (CBPB)                  #line:black
(CC)    <-- (OSSFSC)                #line:black
(SM)    <-- (ADPV)                  #line:black
(SM)    <-- (RU)                    #line:black
(IM)    <-- (DTEPA)                 #line:black
(IM)    <-- (CIATEP)                #line:black
(SIV)   <-- (SCT)                   #line:black
(SIV)   <-- (SCWA)                  #line:black
(SIV)   <-- (FLOSS)                 #line:black
(SIV)   <-- (NT)                    #line:black
(SIV)   <-- (UTC)                   #line:black
(SD)    <-- (SID)                   #line:black
(SD)    <-- (MSL)                   #line:black
(SD)    <-- (SDISS)                 #line:black

@enduml
```
---