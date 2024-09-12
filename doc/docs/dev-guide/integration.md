# Integration guide

Use of LPVS JAR in your project

---

## Authenticating with a personal access token

You can authenticate to GitHub Packages with Apache Maven by editing your 
`~/.m2/settings.xml` file to include your personal access token.

!!! info

    Create a token with minimally sufficient rights:

    - Fine-grained tokens **(recommended)**  

        Only select repositories -> Permissions -> Repository permissions -> Metadata -> Read-only
    
    - Tokens (classic)

        Select scopes -> read:packages

Create a new `~/.m2/settings.xml` file if one doesn't exist.

Example `settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
         <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/samsung/lpvs</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>USERNAME</username>
      <password>TOKEN</password>
    </server>
  </servers>
</settings>
```

!!! note

    If your `settings.xml` file is not located in `~/.m2/settings.xml`, then you need 
    to add the `-s path/to/file/settings.xml` option to `mvn` command

---

## Installing a package

Edit the `<dependencies>` element of the `pom.xml` file located in your project directory.

```xml
<dependencies>
    <dependency>
        <groupId>com.lpvs</groupId>
        <artifactId>lpvs</artifactId>
        <version>x.y.z</version>
    </dependency>
</dependencies>
```
