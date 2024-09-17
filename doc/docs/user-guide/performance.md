# Performance monitoring

Tools that can be used for performance monitoring.

---

## Introduction

There are various performance monitoring tools available for different programming languages, including Java. 

Some tools offer multi-language support, allowing users to monitor applications written in multiple languages.
These tools are designed to assist in analyzing and optimizing application performance by providing insights into 
memory utilization, thread execution, and method execution. By utilizing these tools, developers can effectively 
identify and address performance bottlenecks, enhance CPU performance, and detect potential memory leaks.

Although numerous performance monitoring tools exist for Java , they vary greatly in terms of size, functionality, 
and how seamlessly they integrate with the primary application. For instance, three such tools are Glowroot, VisualVM, 
and JConsole, which were selected based on their relatively small sizes, ease of deployment, and open-source nature:

* [Glowroot](https://github.com/Samsung/LPVS/new/main/doc/docs/user-guide#glowroot)
* [VisualVM](https://github.com/Samsung/LPVS/new/main/doc/docs/user-guide#visualvm)
* [JConsole](https://github.com/Samsung/LPVS/new/main/doc/docs/user-guide#jconsole)

---

## Description and instructions for the tools

Below given very brief instructions for using the tools -more detailed instructions may be found on sites of the applications, GitHub repositories and even by search in browser. 

### Glowroot

Glowroot is a lightweight and fast tool that provides real-time performance monitoring for Java applications. Known for 
its user-friendly interface and ability to track metrics such as transaction traces, thread dumps, and JVM statistics, 
Glowroot is a popular choice among developers.

- Glowroot [website](https://glowroot.org).
- Glowroot [repository](https://github.com/glowroot/glowroot).
- Licensed under the [Apache-2.0](https://github.com/glowroot/glowroot#Apache-2.0-1-ov-file) for code and CC-BY-3.0 for documentation.

#### How to use

* Download the latest version of Glowroot from the GitHub release page or use the download link provided on the website.
* Unzip the downloaded file `glowroot-x.xx.x-dist.zip`.
* Add the following line to your application's JVM arguments: `-javaagent:path/to/glowroot.jar`. Example: 

```bash
java -javaagent:path/to/glowroot.jar -jar lpvs-*.jar
```

* Access the Glowroot dashboard in your browser at http://localhost:4000.

### VisualVM

VisualVM is another built-in tool that provides a more advanced graphical interface for monitoring and managing Java applications. It offers features like profiling, thread dumps, and heap analysis.

- VisualVM [website](https://visualvm.github.io).
- VisualVM [repository](https://github.com/oracle/visualvm).
- Licensed under [GNU General Public License, version 2, with the Classpath Exception](https://visualvm.github.io/gplv2+ce.html).

#### How to use

* Download the latest version of VisualVM from the GitHub release page or use the download link provided on the [website](https://visualvm.github.io/download.html).
* Unzip the downloaded file `visualvm_xxx.zip`.
* Navigate to the unzipped directory and run the executable file located in the `bin/` directory.
* Start the LPVS application and select it for monitoring in VisualVM.
* After initiating monitoring, explore the different metrics available in the VisualVM menu.

!!! note

    * If you encounter an error related to missing libraries when using openjdk-xx, you can try resolving it by running 
    the following command:

    ```bash
    sudo apt install openjdk-xx-jdk --fix-missing 
    ```

    * Additionally, there are over 20 plugins currently available for VisualVM, enabling you to add new monitoring 
    metrics to further enhance your analysis.


### JConsole

JConsole is a built-in tool that accompanies the Java Virtual Machine (JVM) and adheres to the Java Management Extensions (JMX) specification.
It offers a convenient graphical user interface for monitoring and managing your Java applications. As it is a standard 
component of JDK, there is no need for separate installation. However, for utilizing Open Source software under JDK, we 
recommend considering OpenJDK.

- JConsole [website](https://docs.oracle.com/javase/8/docs/technotes/guides/management/jconsole.html).
- Licensed under the GNU General Public License, version 2, with a linking exception.

#### How to use

* Start the JConsole tool by entering the following command in the terminal:

```bash
jconsole
```

* Launch the LPVS application and select it for monitoring in JConsole.
* Once monitoring has started, you can navigate through the different menus and explore various metrics to analyze 
the performance of the application.


!!! note

    * If you encounter an error during startup stating "Failed to load module 'canberra-gtk-module'", it might be 
    resolved by installing missing modules:

    ```bash
    sudo apt install libcanberra-gtk-module libcanberra-gtk3-module
    ```
