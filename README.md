# Maven-Labs: Learning & Experimentation Repository

Welcome to **Maven-Labs**, an engineering notebook and practical workspace dedicated to studying, documenting, and experimenting with **Apache Maven**.

This repository is designed as a long-term resource containing detailed documentation, environment-specific guides, executable examples, and practical insights into Java build automation with Maven.

---

## 🎯 Learning Objectives & Strategy

This repository follows a structured, progressive learning sequence:

```text
Maven Concepts (docs/)
      ↓
AWS EC2 Hands-on (projects/01-aws-ec2/)
      ↓
IntelliJ IDEA Workflow (projects/02-intellij/)
      ↓
More Advanced Maven Projects (Multi-module, CI/CD, Plugins)
```

### Core Focus Areas:
* **Maven Fundamentals**: Core architecture, declarative build models, GAV coordinates (`groupId`, `artifactId`, `version`), and convention over configuration.
* **Project Structure**: Standard directory layouts (`src/main/java`, `src/test/java`, `target`) and conventions.
* **POM (`pom.xml`)**: Object model, dependencies, properties, `<maven.compiler.release>17</maven.compiler.release>`, plugins, and profiles.
* **Maven Lifecycle**: Default, clean, and site lifecycles; phases and goal bindings.
* **AWS EC2 Cloud Laboratory**: Maven on Linux, Java builds, dependency resolution, packaging executable shaded JARs, environment variable configuration, Linux process management, ports, HTTP system monitoring, and future CI/CD deployment.
* **IntelliJ IDEA IDE Laboratory**: Project creation/import, `pom.xml` editing, Maven tool window execution, dependency tree navigation, unit test execution, debugging, and IDE plugin management.

---

## 🛠️ Environment Matrix

| Environment | Method | Status | Details / Verified Configurations |
| :--- | :--- | :--- | :--- |
| **AWS EC2 Linux** | Primary Cloud Execution & Deployment | Completed | Amazon Linux 2023 (`i-0077573b92a678a48`), Amazon Corretto 17.0.20, Apache Maven 3.8.4. Executable `ec2-system-monitor` and `maven-release-lab` services active. |
| **IntelliJ IDEA** | Primary IDE Development Environment | Active | Primary IDE configured for Maven project creation, dependency management, tool window execution, testing, and debugging. |

---

## 📁 Repository Structure

```text
Maven-Labs/
├── README.md                 # Main repository index & learning roadmap
├── .gitignore                # Comprehensive Git ignore rules for Java/Maven
│
├── docs/                     # Core theoretical & practical guides
│   ├── 01-maven-overview.md     # Introduction to Maven & build automation concepts
│   ├── 02-installation.md       # Setup guides for AWS EC2 Linux, IntelliJ IDEA, and Maven CLI
│   ├── 03-project-structure.md  # Maven standard directory layout breakdown
│   ├── 04-pom-xml.md            # Deep dive into project object model (pom.xml)
│   ├── 05-maven-lifecycle.md    # Lifecycles, phases, and goal bindings
│   └── troubleshooting.md       # Real-world errors, causes, and solutions
│
└── projects/                 # Practical environment-specific laboratories
    ├── 01-aws-ec2/           # AWS EC2 Linux cloud laboratory
    │   ├── ec2-system-monitor/ # Java 17 zero-framework system monitor
    │   └── maven-release-lab/  # Maven profiles, resource filtering & release lab
    └── 02-intellij/          # IntelliJ IDEA Maven IDE integration laboratory
```

---

## 🗺️ Learning Roadmap

- [x] Install Maven on AWS EC2 (`Apache Maven 3.8.4`)
- [x] Create and build standard Maven projects on EC2
- [x] Implement lightweight Java 17 system monitoring service (`ec2-system-monitor`)
- [x] Deep dive into `pom.xml` (`<maven.compiler.release>17</maven.compiler.release>`)
- [x] Integrate IntelliJ IDEA Maven Tool Window & lifecycle execution
- [ ] Maven lifecycle deep dive & custom goal bindings
- [ ] Dependency resolution, transitive dependencies & scopes
- [ ] Maven plugin configuration (`compiler`, `surefire`, `shade`)
- [ ] Remote repositories & distribution management
- [ ] Multi-module Maven project architecture
- [ ] Automated testing & code coverage plugins
- [ ] Maven build profiles (`dev`, `prod`, `cloud`)
- [ ] Automated CI/CD pipelines with GitHub Actions
