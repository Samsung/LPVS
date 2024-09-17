# Performance monitoring tools

---
## Introduction
There are many performance monitoring tools for differnet programming languages, including Java language. 

Some tools also may be multilanguage i.e. include support a few languages. 

These tools are designed to help analyze and optimize application performance by providing insights into memory usage, thread execution, and method execution. 

They can help identify and resolve bottlenecks, improve CPU performance, and detect memory leaks.

However even for Java language there are many performance monitoring tools, it may differ in size, functionality and way of integration in main application.

For example was taken 3 performance monitoring tools:
* [`Glowroot`](https://github.com/Samsung/LPVS/new/main/doc/docs/user-guide#glowroot)
* [`VisualVM`](https://github.com/Samsung/LPVS/new/main/doc/docs/user-guide#visualvm)
* [`JConsole`](https://github.com/Samsung/LPVS/new/main/doc/docs/user-guide#jconsole)

These tools were taken for example due their small size, easy instructions for run and belonging to Open Source software.

---
## Description and instructions for the tools 
Below given very brief instructions for using the tools -more detailed instructions may be found on sites of the applications, GitHub repositories and even by search in browser. 

### Glowroot:
is a lightweight and fast tool that provides real-time performance monitoring for Java applications. It's known for its ease of use and ability to track metrics like transaction traces, thread dumps, and JVM statistics.

[Glowroot site](https://glowroot.org)

[GitHub link](https://github.com/glowroot/glowroot) 

License: code licensed under the Apache License v2.0 ([link](https://github.com/glowroot/glowroot#Apache-2.0-1-ov-file)), documentation under CC BY 3.0.

#### Instructions:
* Download last version of the tool from last release on the GitHub page or using link on the site;
* Unzip `glowroot-x.xx.x-dist.zip`;
* Add -javaagent:path/to/glowroot.jar to your application's JVM args;
* Check in browser http://localhost:4000.

Example of run built LPVS application from directory ../LPVS/target: 
```bash
java -javaagent:path/to/glowroot.jar -jar lpvs-*.jar
```

### VisualVM:
is another built-in tool that provides a more advanced graphical interface for monitoring and managing Java applications. It offers features like profiling, thread dumps, and heap analysis.

[VisualVM site](https://visualvm.github.io)

[GitHub link](https://github.com/oracle/visualvm)

License: GPLv2 + CE ([link](https://visualvm.github.io/gplv2+ce.html))

#### Instructions:
* Download last version of the tool from last release on the GitHub page or using link on the site(https://visualvm.github.io/download.html);
* Enter in unzipped directory and run:
```bash
bin/visualvm
```
* Run LPVS application and choose in VisualVM to monitor it.

After starting monitoring possible navigate menu with different metrics.


Useful notes:
* If error of absent library appears for openjdk-xx it may be fixed by command:
```bash
sudo apt install openjdk-xx-jdk --fix-missing 
```
* There are more than 20 plugins at the moment that help to add new monitoring metrics.
 

### JConsole:
is a built-in tool that comes with the Java Virtual Machine (JVM) and is compliant with the Java Management Extensions (JMX) specification. It provides a graphical user interface for monitoring and managing your Java applications.

It is standart tool that supplied with JDK- no need to install. For using Open Source software in further under JDK we understand OpenJDK.
License: as Jconsole is part of OpenJDK it is licensed under the GNU General Public License (GPL) version 2, with a linking exception (GPLv2+CPE).

#### Instructions:
* Start command in terminal:
```bash
jconsole
```
* Run LPVS application- then start JConsole and choose LPVS as application for monitoring.

After starting monitoring possible navigate menu with different metrics.


Useful notes:
* If appears error during start: Failed to load module "canberra-gtk-module" - it may be fixed by installing absent modules:
```bash
sudo apt install libcanberra-gtk-module libcanberra-gtk3-module
```
