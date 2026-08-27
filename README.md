# Maven-Labs: Learning & Experimentation Repository

Welcome to **Maven-Labs**, an engineering notebook and practical workspace dedicated to studying, documenting, and experimenting with **Apache Maven**.

This repository is designed as a long-term resource containing detailed documentation, environment-specific guides, executable examples, and practical insights into Java build automation with Maven.

---

## 🎯 Learning Objectives

This repository covers key concepts and hands-on practices across Apache Maven:

* **Maven Fundamentals**: Core architecture, declarative build models, and convention over configuration.
* **Project Structure**: Standard directory layouts (`src/main`, `src/test`, `target`) and conventions.
* **POM (`pom.xml`)**: Object model, coordinates (`groupId`, `artifactId`, `version`), parent POMs, and properties.
* **Maven Lifecycle**: Default, clean, and site lifecycles; phases and bindings.
* **Build Automation & Environments**: Using Maven CLI on local machines, AWS EC2 Linux instances, and IntelliJ IDEA integration.
* **Git & GitHub Integration**: Version controlling Maven projects, ignoring build artifacts, and repository management.

---

## 🛠️ Environment Matrix

| Environment   | Method            | Status      | Details / Verified Configurations |
| ------------- | ----------------- | ----------- | --------------------------------- |
| **Local Machine** | Maven CLI         | In Progress | Windows 11, OpenJDK 17.0.17 verified. Maven CLI installation pending. |
| **AWS EC2 Linux** | Maven CLI         | Completed   | Amazon Linux 2023 (`i-0077573b92a678a48`), Amazon Corretto 17.0.20, Apache Maven 3.8.4. Project `shevay-app` active. |
| **IntelliJ IDEA** | Maven Integration | In Progress | Primary IDE configured for Maven project management & lifecycle execution. |

---

## 📁 Repository Structure

```text
Maven-Labs/
├── README.md                 # Main repository index & learning roadmap
├── .gitignore                # Comprehensive Git ignore rules for Java/Maven
│
├── docs/                     # Core theoretical & practical guides
│   ├── 01-maven-overview.md     # Introduction to Maven & build automation concepts
│   ├── 02-installation.md       # Setup guides for Windows CLI, AWS EC2 Linux, and IntelliJ IDEA
│   ├── 03-project-structure.md  # Maven standard directory layout breakdown
│   ├── 04-pom-xml.md            # Deep dive into project object model (pom.xml)
│   ├── 05-maven-lifecycle.md    # Lifecycles, phases, and goal bindings
│   └── troubleshooting.md       # Real-world errors, causes, and solutions
│
└── projects/                 # Practical environment-specific project guides
    ├── 01-local-cli/         # Hands-on guide for Windows local Maven CLI
    ├── 02-aws-ec2/           # Hands-on guide for AWS EC2 Linux (i-0077573b92a678a48) & shevay-app
    └── 03-intellij/          # Hands-on guide for IntelliJ IDEA Maven integration
```

---

## 🗺️ Learning Roadmap

- [x] Install Maven on EC2 (`Apache Maven 3.8.4`)
- [x] Create Maven project on EC2 (`shevay-app`)
- [x] Understand pom.xml
- [ ] Maven lifecycle
- [ ] Dependencies
- [ ] Dependency scopes
- [ ] Plugins
- [ ] Maven repositories
- [ ] Packaging
- [ ] Testing
- [ ] Maven profiles
- [ ] Multi-module Maven
- [ ] Maven with CI/CD
