# 02 - IntelliJ IDEA Maven Integration Guide

This guide documents creating and managing Apache Maven projects inside **IntelliJ IDEA**, detailing how IDE features map directly to underlying Maven CLI lifecycle phases and goals.

---

## 1. Creating a New Maven Project in IntelliJ

1. Open IntelliJ IDEA -> Click **New Project**.
2. Select **Maven** from the left-hand menu.
3. Configure project settings:
   - **Name**: `simple-maven-project`
   - **Location**: `e:\Maven\projects\02-ide\simple-maven-project`
   - **JDK**: Select **17** (e.g., Temurin 17 or Oracle OpenJDK 17).
   - **GroupId**: `com.mycompany.app`
   - **ArtifactId**: `simple-maven-project`
   - **Version**: `1.0-SNAPSHOT`
4. Click **Create**. IntelliJ automatically generates `pom.xml` and the standard `src/main/java`, `src/test/java` directory structure.

---

## 2. Understanding IntelliJ Maven Tool Window

IntelliJ IDEA features a dedicated **Maven Tool Window** (located on the right sidebar or accessed via `View -> Tool Windows -> Maven`).

### Tool Window Components:
* **Lifecycle**: Expand to view phases (`clean`, `validate`, `compile`, `test`, `package`, `verify`, `install`, `deploy`). Double-clicking a phase executes the corresponding Maven build phase.
* **Plugins**: Lists configured build plugins and their executable goals (e.g., `compiler:compile`, `surefire:test`).
* **Dependencies**: Displays a visual tree of direct and transitive project dependencies.
* **Reload All Maven Projects** (Icon with circular arrows): Re-imports `pom.xml` dependencies whenever `pom.xml` is modified.

---

## 3. IDE Actions vs. Maven CLI Commands

It is vital to understand what IntelliJ does behind the scenes when running IDE actions vs. native Maven commands:

| IDE Action / Feature | What IntelliJ Does Underneath | Equivalent Maven CLI Command |
| :--- | :--- | :--- |
| **Build -> Build Project** (`Ctrl+F9`) | IntelliJ compiles modified classes using its internal compiler (Javac) into IDE output folders. | *No direct CLI equivalent (IDE fast incremental compilation)* |
| **Run 'App.main()'** | Compiles main class and launches Java JVM process with dependencies on classpath. | `mvn compile exec:java -Dexec.mainClass="com.example.App"` |
| **Run Unit Tests via Green Arrow** | Launches JUnit test runner against test class. | `mvn test -Dtest=AppTest` |
| **Maven Tool Window -> Double Click `package`** | Spawns Maven process executing full default lifecycle up to `package`. | `mvn package` |
| **Maven Tool Window -> Double Click `clean`** | Spawns Maven process executing `clean` lifecycle. | `mvn clean` |

---

## 4. Configuring Custom Maven Settings in IntelliJ

1. Open **Settings** (`Ctrl+Alt+S`) -> **Build, Execution, Deployment** -> **Build Tools** -> **Maven**.
2. **Maven home path**: Default is `Bundled (Maven 3)`. Can be changed to custom local Maven installation directory (e.g., `C:\apache-maven-3.9.5`).
3. **User settings file**: Check `Override` if using a custom `~/.m2/settings.xml`.
4. **Local repository**: Displays detected path to `~/.m2/repository`.

---

## Key Takeaway

IntelliJ IDEA automates dependency resolution, classpath setup, and goal execution. However, every action in the IDE maps directly to standard Maven project structures (`pom.xml`, `src/`) and Maven lifecycle phases.
