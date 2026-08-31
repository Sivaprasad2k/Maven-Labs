# Maven-Labs: Java Build Automation Notebook

Welcome to **Maven-Labs**, an engineering notebook and reference workspace dedicated to studying, documenting, and experimenting with **Apache Maven** and Java 17 build automation.

This repository serves as a structured resource containing theoretical documentation, environment-specific guides, executable microservices, and practical verification logs for Maven build lifecycles, dependency management, resource filtering, profiles, and shaded artifact packaging.

---

## Learning Progression

The repository follows a progressive learning sequence:

```text
Maven Core Concepts (docs/)
       ↓
AWS EC2 Projects (projects/01-aws-ec2/)
       ↓
IDE Environment Workflow (projects/02-ide/)
       ↓
Advanced Build Architectures (Multi-module, CI/CD, Distribution)
```

---

## Core Focus Areas

- **Maven Fundamentals**: Project Object Model (`pom.xml`), GAV coordinates (`groupId`, `artifactId`, `version`), declarative build specifications, and convention over configuration.
- **Directory Layout**: Standard directory structures (`src/main/java`, `src/test/java`, `src/main/resources`, `target`).
- **Compiler Configuration**: Modern JDK target specification using `<maven.compiler.release>17</maven.compiler.release>`.
- **Lifecycle & Goal Bindings**: Execution of default, clean, and site lifecycles; binding plugin goals to lifecycle phases.
- **Dependency Management**: Direct vs. transitive dependencies, dependency scope isolation (`compile` vs. `test`), and dependency tree analysis.
- **Build Profiles & Filtering**: Dynamic environment configuration (`dev`, `production`) and POM property substitution inside resources.
- **Artifact Packaging**: Differences between standard compiled bytecode JARs and executable shaded fat JARs (`maven-shade-plugin`).
- **Cloud & IDE Execution**: Building and deploying executable JAR services on AWS EC2 Linux and managing Maven projects inside IntelliJ IDEA.

---

## Environment Matrix

| Environment | Role | Status | Configuration & Verification Details |
| :--- | :--- | :--- | :--- |
| **AWS EC2 Linux** | Primary Cloud Execution & Deployment | Verified | Amazon Linux 2023 (`i-0077573b92a678a48`), Amazon Corretto 17.0.20, Apache Maven 3.8.4. `ec2-system-monitor` and `maven-release-lab` verified active. |
| **IntelliJ IDEA** | Primary IDE Development Environment | Verified | IDE integration for project creation, `pom.xml` editing, Maven tool window phase execution, dependency visualization, and debugging. |

---

## Repository Structure

```text
Maven-Labs/
├── README.md                 # Main repository index & engineering roadmap
├── .gitignore                # Git ignore rules for Java bytecode and Maven build outputs
│
├── docs/                     # Core theoretical and procedural guides
│   ├── 01-maven-overview.md  # Maven architecture & comparison with Ant/Gradle
│   ├── 02-installation.md    # Environment setup guides for AWS EC2 Linux & IntelliJ IDEA
│   ├── 03-project-structure.md # Standard Maven directory layout breakdown
│   ├── 04-pom-xml.md         # Deep dive into Project Object Model (pom.xml)
│   ├── 05-maven-lifecycle.md # Lifecycles, phases, and plugin goal bindings
│   └── troubleshooting.md    # Real-world build errors, root cause analysis & solutions
│
└── projects/                 # Practical environment-specific laboratories
    ├── 01-aws-ec2/           # AWS EC2 Linux cloud laboratory
    │   ├── ec2-system-monitor/ # Java 17 zero-framework system monitoring service
    │   └── maven-release-lab/  # Maven profiles, resource filtering & release lab
    └── 02-ide/               # IDE environment Maven integration laboratory
```

---

## Current Projects

### 1. `ec2-system-monitor` (`projects/01-aws-ec2/ec2-system-monitor`)
A zero-framework Java 17 HTTP microservice deployed on AWS EC2. Demonstrates core Java `HttpServer`, Jackson JSON serialization, JVM/OS metrics collection, thread pool management, graceful shutdown, and shaded fat-JAR packaging.

### 2. `maven-release-lab` (`projects/01-aws-ec2/maven-release-lab`)
A dedicated laboratory making Maven build-time mechanisms observable at runtime. Demonstrates build profiles (`dev` vs. `production`), resource filtering (`application.properties` property injection), dependency scopes (`compile` vs. `test`), Surefire test execution, and shaded fat-JAR artifact creation on AWS EC2.

---

## Documentation Index

- [01 - Maven Overview](docs/01-maven-overview.md)
- [02 - Installation Guide](docs/02-installation.md)
- [03 - Standard Project Structure](docs/03-project-structure.md)
- [04 - Deep Dive into pom.xml](docs/04-pom-xml.md)
- [05 - Maven Lifecycles and Phases](docs/05-maven-lifecycle.md)
- [Troubleshooting Reference](docs/troubleshooting.md)
- [AWS EC2 Laboratory Guide](projects/01-aws-ec2/README.md)
- [IDE Integration Guide](projects/02-ide/README.md)

---

## Roadmap

- [x] Install Apache Maven 3.8.4 on AWS EC2 Linux
- [x] Generate and build standard Maven archetype projects on EC2
- [x] Implement lightweight Java 17 system monitoring service (`ec2-system-monitor`)
- [x] Configure explicit Java 17 target release (`<maven.compiler.release>17</maven.compiler.release>`)
- [x] Document IntelliJ IDEA Maven Tool Window & lifecycle execution mapping
- [x] Implement Maven build profiles (`dev`, `production`) and resource filtering (`maven-release-lab`)
- [x] Verify shaded fat-JAR packaging and dependency scope isolation on AWS EC2
- [ ] Multi-module Maven project architecture
- [ ] Custom Maven plugin execution and goal bindings
- [ ] Remote distribution management and repository publishing
- [ ] CI/CD pipeline integration with GitHub Actions
