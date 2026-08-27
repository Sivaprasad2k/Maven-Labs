# 02 - AWS EC2 Linux Maven Guide

This guide documents the creation, execution, and troubleshooting of Maven projects on our **AWS EC2 Linux** instance using exact environment configurations and real terminal outputs.

---

## 1. AWS EC2 Instance Details

| Specification | Value |
| :--- | :--- |
| **Instance ID** | `i-0077573b92a678a48` |
| **AWS Region** | `ap-south-2` (Hyderabad) |
| **Public DNS** | `ec2-16-113-88-194.ap-south-2.compute.amazonas.com` |
| **Instance Hostname** | `ip-172-31-39-135` |
| **Operating System** | Amazon Linux 2023 (`6.18.41-94.142.amzn2023.x86_64`) |
| **SSH User** | `ec2-user` |
| **SSH Key Pair** | `java.pem` (Local path: `/e/AWS/java.pem`) |

---

## 2. Connecting to EC2 via SSH Client

From local Git Bash terminal (`/e/AWS` directory):

```bash
# 1. Set key file permissions (read-only by owner)
chmod 400 "java.pem"

# 2. Connect to the EC2 instance via SSH
ssh -i "java.pem" ec2-user@ec2-16-113-88-194.ap-south-2.compute.amazonas.com
```

---

## 3. Verified Java & Maven Installations on EC2

### Java Version Output (`java -version`):
```text
openjdk version "17.0.20" 2026-07-21 LTS
OpenJDK Runtime Environment Corretto-17.0.20.8.1 (build 17.0.20+8-LTS)
OpenJDK 64-Bit Server VM Corretto-17.0.20.8.1 (build 17.0.20+8-LTS, mixed mode, sharing)
```

### Maven Version Output (`mvn -v`):
```text
Apache Maven 3.8.4 (Red Hat 3.8.4-3.amzn2023.0.5)
Maven home: /usr/share/maven
Java version: 17.0.20, vendor: Amazon.com Inc., runtime: /usr/lib/jvm/java-17-amazon-corretto.x86_64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.18.41-94.142.amzn2023.x86_64", arch: "amd64", family: "unix"
```

---

## 4. Existing Project Context: `shevay-app`

The existing Maven application directory structure on EC2:

```text
[ec2-user@ip-172-31-39-135 shevay-app]$ ls -l
total 4
-rw-r--r--. 1 ec2-user ec2-user 643 Aug 27 18:19 pom.xml
drwxr-xr-x. 4 ec2-user ec2-user  30 Aug 27 18:19 src
```

### Project Directory Structure:
```text
shevay-app/
├── pom.xml      (643 bytes)
└── src/
    ├── main/
    │   └── java/
    └── test/
        └── java/
```

---

## 5. Real Issue Encountered & Root Cause Analysis

### Error Traceback:
```text
[ERROR] The goal you specified requires a project to execute 
[ERROR] but there is no POM in this directory (/home/ec2-user). 
[ERROR] Please verify you invoked Maven from the correct directory.
```

### What Happened & Why:
The command `mvn compile` (or `mvn package`) was executed while in the user home directory (`/home/ec2-user`), outside of `/home/ec2-user/shevay-app`.

Because build lifecycle goals (`compile`, `test`, `package`) require a project context, Maven searched for `pom.xml` in the working directory. Finding none, it threw the error.

### Solution:
Navigate into the directory containing `pom.xml` prior to executing build goals:

```bash
cd ~/shevay-app
# or
cd ~/Maven/shevay-app

# Verify pom.xml exists
ls -l pom.xml

# Execute Maven package
mvn package
```
