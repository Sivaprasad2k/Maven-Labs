# 01 - Apache Maven Overview

## What is Apache Maven?

**Apache Maven** is a software project management and comprehension tool primarily used for Java projects. Based on the concept of a **Project Object Model (POM)**, Maven can manage a project's build, reporting, and documentation from a central piece of information (`pom.xml`).

---

## The Problems Maven Solves

Before build tools like Maven were widely adopted, Java developers faced several challenges:

1. **Manual Classpath & Library Management**: Developers had to manually download `.jar` files, place them in a `lib/` directory, and track version compatibility.
2. **Inconsistent Build Environments**: Scripts written in Bash or Ant often relied on hardcoded local paths, causing "works on my machine" failures.
3. **Lack of Standardized Project Structure**: Different developers organized source files, resources, and tests differently, increasing onboarding friction.
4. **Repetitive Build Scripting**: Every project required writing custom imperative logic to compile code, run tests, and package archives.

---

## Core Principles of Maven

### 1. Convention over Configuration
Maven prescribes standard locations for source code, test code, configuration files, and build outputs. By adhering to these conventions, developers do not need to explicitly configure basic build paths.

### 2. Declarative Build Specification
Instead of writing imperative step-by-step build scripts (e.g., shell scripts), Maven projects use a declarative XML file (`pom.xml`) that defines *what* the project depends on and *what* artifact it produces.

### 3. Reusable Build Lifecycles
Maven provides built-in lifecycles (`clean`, `default`, `site`) consisting of well-defined phases (`compile`, `test`, `package`, `install`, `deploy`).

### 4. Centralized Dependency Resolution
Maven automatically downloads declared dependencies and their transitive dependencies from local or remote repositories.

---

## Comparison: Ant vs. Maven vs. Gradle

| Feature | Apache Ant | Apache Maven | Gradle |
| :--- | :--- | :--- | :--- |
| **Approach** | Imperative (Procedural) | Declarative (Convention-based) | Declarative / Domain Specific (Groovy/Kotlin) |
| **Dependency Management** | Manual (or via Ivy plugin) | Built-in & Automatic | Built-in & Automatic |
| **Build File** | `build.xml` | `pom.xml` | `build.gradle` / `build.gradle.kts` |
| **Structure Flexibility** | High (No fixed rules) | Standardized Conventions | Flexible with strong defaults |
| **Learning Curve** | Low initial, high maintenance | Low setup, structured rules | Medium-High |

---

## Summary Key Takeaways

- Maven simplifies Java software development by automating compilation, test execution, dependency resolution, and packaging.
- The `pom.xml` file is the heart of any Maven project.
- Following Maven's standard directory layout saves configuration effort and improves team collaboration.
