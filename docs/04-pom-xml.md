# 04 - Deep Dive into pom.xml

The **Project Object Model (`pom.xml`)** is the core configuration unit of any Apache Maven project. It is an XML file located at the project root directory that defines the project identity, dependencies, build settings, and plugin configurations.

---

## Minimum Valid `pom.xml` Example

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Project Coordinates -->
    <groupId>com.example</groupId>
    <artifactId>simple-maven-project</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!-- Properties -->
    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

> **Modern Best Practice Note**: In Java 9+, using `<maven.compiler.release>17</maven.compiler.release>` is preferred over setting `<maven.compiler.source>` and `<maven.compiler.target>` separately. The `--release` flag automatically configures the source level, target bytecode version, and the matching Java platform bootstrap classpath simultaneously.

---

## Key Elements of `pom.xml`

### 1. Root XML Namespace (`<project>`)
Specifies the Maven XML Schema definition version (currently `4.0.0`).

### 2. Maven GAV Coordinates
Maven uniquely identifies every project and library using **GAV** (GroupId, ArtifactId, Version):

* **`<groupId>`**: Specifies the organization, domain, or package structure owning the project (e.g., `com.mycompany.app`).
* **`<artifactId>`**: The unique name of the specific project module/library (e.g., `user-service`). This forms the base name of the generated artifact (e.g., `user-service-1.0.0.jar`).
* **`<version>`**: Specifies the project build version. Versions ending with `-SNAPSHOT` represent active development builds.

### 3. Packaging (`<packaging>`)
Defines the final build output archive format. Defaults to `jar` if omitted.
* Common options: `jar`, `war`, `pom` (for multi-module parent POMs), `ear`.

### 4. Properties (`<properties>`)
Allows defining custom key-value variables used across the `pom.xml` file:
```xml
<properties>
    <java.version>17</java.version>
    <junit.version>5.10.0</junit.version>
</properties>
```
Referenced elsewhere using property expansion notation: `${junit.version}`.

### 5. Dependencies (`<dependencies>`)
Declares third-party software packages required by the project:
```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 6. Build Configuration (`<build>`)
Defines compiler plugins, source directory overrides, resources filtering, and plugin configurations:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <release>17</release>
            </configuration>
        </plugin>
    </plugins>
</build>
```

---

## Summary Key Takeaways

- **GAV** (`groupId`, `artifactId`, `version`) forms the unique key for every Maven library worldwide.
- Properties prevent version duplication across dependency declarations.
- Snapshots (`-SNAPSHOT`) signify non-final, fluid development releases.
