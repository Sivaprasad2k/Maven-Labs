# 02 - Installing Apache Maven

This document outlines the step-by-step procedure to install and verify Java and Apache Maven across operating systems, cloud instances (AWS EC2), and development environments.

---

## Prerequisites: Java Development Kit (JDK)

Apache Maven requires a working Java Development Kit (JDK). Ensure Java is installed and `JAVA_HOME` is correctly set.

### Local Windows Environment Verification:
```powershell
java -version
```
Output:
```text
openjdk version "17.0.17" 2025-10-21
OpenJDK Runtime Environment Temurin-17.0.17+10 (build 17.0.17+10)
OpenJDK 64-Bit Server VM Temurin-17.0.17+10 (build 17.0.17+10, mixed mode, sharing)
```

---

## 1. Installing Maven CLI on Windows (Optional)

> **Note**: **AWS EC2** (`projects/01-aws-ec2`) is our primary cloud execution environment and **IDE** (`projects/02-ide`) is our primary IDE development environment. Installing local Maven CLI is optional but recommended if you wish to run `mvn` commands directly from local terminals.

### Step 1: Download Maven
Download the binary zip archive from `https://maven.apache.org/download.cgi` (e.g., `apache-maven-3.9.x-bin.zip`).

### Step 2: Extract Archive
Extract to `C:\Program Files\apache-maven-3.9.x` or `C:\apache-maven-3.9.x`.

### Step 3: Environment Variables Setup
1. System Variable: `MAVEN_HOME` = `C:\apache-maven-3.9.x`
2. System `PATH`: append `%MAVEN_HOME%\bin`
3. Verify System Variable `JAVA_HOME` points to your JDK directory.

---

## 2. Installing Maven on AWS EC2 (Amazon Linux 2023)

### AWS EC2 Instance Specs (`i-0077573b92a678a48`):
- OS: Amazon Linux 2023 (`kernel 6.18.41-94.142.amzn2023.x86_64`)
- SSH User: `ec2-user`
- Region: `ap-south-2`

### Package Installation Commands:
```bash
sudo dnf update -y
sudo dnf install -y java-17-amazon-corretto-devel maven
```

### Verified Terminal Outputs:

#### `java -version`:
```text
openjdk version "17.0.20" 2026-07-21 LTS
OpenJDK Runtime Environment Corretto-17.0.20.8.1 (build 17.0.20+8-LTS)
OpenJDK 64-Bit Server VM Corretto-17.0.20.8.1 (build 17.0.20+8-LTS, mixed mode, sharing)
```

#### `mvn -v`:
```text
Apache Maven 3.8.4 (Red Hat 3.8.4-3.amzn2023.0.5)
Maven home: /usr/share/maven
Java version: 17.0.20, vendor: Amazon.com Inc., runtime: /usr/lib/jvm/java-17-amazon-corretto.x86_64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.18.41-94.142.amzn2023.x86_64", arch: "amd64", family: "unix"
```

---

## 3. Maven Integration in IntelliJ IDEA

IntelliJ IDEA is the primary IDE chosen for this repository. It includes bundled Apache Maven support.

### Configuration Steps in IntelliJ IDEA:
1. Open **File** -> **Settings** (`Ctrl+Alt+S`).
2. Go to **Build, Execution, Deployment** -> **Build Tools** -> **Maven**.
3. Set **Maven home path**: `Bundled (Maven 3)` or custom local path.
4. Set **JDK for importer**: Project JDK (Java 17).
