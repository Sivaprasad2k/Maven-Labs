# 01 - AWS EC2 Linux Maven Guide & Java 17 Experiment

This guide documents the setup, project structure, compilation troubleshooting, root cause analysis, and resolution for our Maven application (`shevay-app`) running on an **AWS EC2 Linux** instance.

---

## 1. Environment

| Component | Specification / Version |
| :--- | :--- |
| **Platform** | AWS EC2 (Instance ID: `i-0077573b92a678a48`, Region: `ap-south-2`) |
| **OS / Kernel** | Amazon Linux 2023 (`6.18.41-94.142.amzn2023.x86_64`) |
| **Java JDK** | OpenJDK `17.0.20` (Amazon Corretto-17.0.20.8.1 LTS) |
| **Build Tool** | Apache Maven `3.8.4` (Red Hat 3.8.4-3.amzn2023.0.5) |
| **Version Control** | Git `2.50.1` |
| **SSH Client** | Git Bash / OpenSSH (`ssh -i "java.pem" ec2-user@ec2-16-113-88-194.ap-south-2.compute.amazonas.com`) |

---

## 2. Project Creation

The Maven project was generated directly from the AWS EC2 Linux terminal using the Maven quickstart archetype:

```bash
cd ~/Maven
mvn archetype:generate \
  -DgroupId=com.shevay \
  -DartifactId=shevay-app \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false
```

### Directory Structure Generated:
```text
simple-maven-project/   (shevay-app)
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── shevay/
    │               └── App.java
    └── test/
        └── java/
            └── com/
                └── shevay/
                    └── AppTest.java
```

---

## 3. Problem Encountered

When attempting to build the project on AWS EC2 using `mvn clean package`, the build failed with the following compiler error:

```text
[ERROR] COMPILATION ERROR : 
[INFO] -------------------------------------------------------------
[ERROR] Source option 5 is no longer supported. Use 7 or later.
[ERROR] Target option 5 is no longer supported. Use 7 or later.
[INFO] 1 error
[INFO] -------------------------------------------------------------
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
```

During build execution, Maven was observed invoking `maven-compiler-plugin:3.1`.

---

## 4. Root Cause Analysis

### JDK Version ≠ Maven Compiler Configuration

```text
Java 17 JDK Installed (Correct)
        │
        ▼
   Apache Maven
        │
        ▼
Old Compiler Plugin (3.1)
        │
        ▼
Default Source/Target = Java 5
        │
        ▼
Java 17 Compiler Rejects Java 5 Specification
        │
        ▼
BUILD FAILURE
```

1. **Java 17 JDK Was Installed Correctly**: `java -version` confirmed OpenJDK 17.0.20 was present and functioning properly.
2. **Outdated Archetype Defaults**: The quickstart archetype generated a legacy `pom.xml` without specifying compiler target properties or modern plugin versions.
3. **Legacy Compiler Plugin Defaults**: Inheriting `maven-compiler-plugin:3.1` caused Maven to default source and target bytecode levels to **Java 5 (1.5)**.
4. **JDK 17 Strictness**: Modern JDKs (Java 12+) have retired support for targeting ancient Java releases (Java 5 and 6). Passing `-source 5` to `javac` under JDK 17 causes an immediate compilation failure.

---

## 5. Solution & Updated `pom.xml`

To fix the build, `pom.xml` was updated to explicitly target Java 17 using the modern `<maven.compiler.release>17</maven.compiler.release>` tag and configuring a compatible `maven-compiler-plugin` version (`3.11.0`).

### Corrected `pom.xml`:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.shevay</groupId>
    <artifactId>shevay-app</artifactId>
    <packaging>jar</packaging>
    <version>1.0-SNAPSHOT</version>

    <name>shevay-app</name>
    <url>http://maven.apache.org</url>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 6. Build Verification

### Execution Command:
```bash
mvn clean package
```

### Verified Build Output:
```text
[INFO] Scanning for projects...
[INFO] -----------------------< com.shevay:shevay-app >------------------------
[INFO] Building shevay-app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] --- clean:3.2.0:clean (default-clean) @ shevay-app ---
[INFO] Deleting target
[INFO] --- compiler:3.11.0:compile (default-compile) @ shevay-app ---
[INFO] Compiling 1 source file with javac [debug release 17] to target/classes
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ shevay-app ---
[INFO] Compiling 1 source file with javac [debug release 17] to target/test-classes
[INFO] --- surefire:3.2.5:test (default-test) @ shevay-app ---
[INFO] Running com.shevay.AppTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- jar:3.3.0:jar (default-jar) @ shevay-app ---
[INFO] Building jar: target/shevay-app-1.0-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 7. Important Learning: JDK vs. Maven Compiler Configuration

Understanding how Maven interacts with the machine's Java installation is critical for build reproducibility:

1. **JDK Environment**: The installed JDK (`java -version`) provides the JVM runtime and `javac` compiler binary available to system scripts.
2. **Maven Build Orchestration**: Maven executes build lifecycles and delegates compilation tasks to plugin goals.
3. **Maven Compiler Plugin Role**: The `maven-compiler-plugin` determines what flags (such as `--release 17` or `-source 17 -target 17`) are passed to `javac`.
4. **Archetype Awareness**: Archetypes (especially older quickstart templates) often generate outdated build definitions. Developers must audit `pom.xml` after archetype generation.
5. **Why `<maven.compiler.release>17</maven.compiler.release>` is Best Practice**: Introduced in Java 9, the `--release` flag configures the source, target, and bootstrap classpath simultaneously, ensuring the project cannot accidentally use APIs from a newer JDK that aren't present in Java 17.
