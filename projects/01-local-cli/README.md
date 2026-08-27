# 01 - Local Machine Maven CLI Guide

This guide documents the setup, project creation, compilation, testing, and packaging of Maven Java applications from a Windows local machine using the command-line interface (CLI).

---

## 1. Prerequisites & Environment Check

### Java Installation Verification
Run in Windows PowerShell:
```powershell
java -version
```
Actual system output:
```text
openjdk version "17.0.17" 2025-10-21
OpenJDK Runtime Environment Temurin-17.0.17+10 (build 17.0.17+10)
OpenJDK 64-Bit Server VM Temurin-17.0.17+10 (build 17.0.17+10, mixed mode, sharing)
```

### Maven CLI Installation
Download Apache Maven zip from `https://maven.apache.org/download.cgi`, extract to `C:\apache-maven-3.9.5`, and add `C:\apache-maven-3.9.5\bin` to System `PATH`.

Verify Maven CLI installation:
```powershell
mvn -version
```
Expected output:
```text
Apache Maven 3.9.x
Maven home: C:\apache-maven-3.9.x
Java version: 17.0.17, vendor: Eclipse Adoptium
```

---

## 2. Creating a Maven Project via Local CLI

To generate a standard Java application project structure interactively or non-interactively using the Maven Archetype plugin:

```powershell
mvn archetype:generate `
  -DgroupId=com.mycompany.app `
  -DartifactId=simple-maven-project `
  -DarchetypeArtifactId=maven-archetype-quickstart `
  -DarchetypeVersion=1.4 `
  -DinteractiveMode=false
```

### Project Directory Layout Produced:
```text
simple-maven-project/
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── mycompany/
    │               └── app/
    │                   └── App.java
    └── test/
        └── java/
            └── com/
                └── mycompany/
                    └── app/
                        └── AppTest.java
```

---

## 3. Local CLI Build Workflow & Commands

Navigate into the generated project directory:
```powershell
cd simple-maven-project
```

### 1. Compile Source Code
Compiles `.java` files from `src/main/java/` to `target/classes/`:
```powershell
mvn compile
```

### 2. Execute Unit Tests
Compiles test classes and runs JUnit tests:
```powershell
mvn test
```

### 3. Package into Executable JAR
Compiles, tests, and bundles compiled bytecode into `target/simple-maven-project-1.0-SNAPSHOT.jar`:
```powershell
mvn package
```

### 4. Clean Build Artifacts
Deletes the generated `target/` directory:
```powershell
mvn clean
```

---

## 4. Git Integration Best Practices

Before committing local CLI projects to Git:
1. Ensure `.gitignore` ignores `target/`, `.idea/`, `*.class`, and `.vscode/`.
2. Commit only source files (`src/`) and `pom.xml`.
3. Never commit compiled binary `.jar` or `.class` files.
