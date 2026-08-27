# 03 - Maven Standard Project Structure

Apache Maven enforces a standardized directory layout across all projects. By adhering to this structure, Maven plugins know automatically where to look for source files, configuration resources, and test suites without requiring manual path configurations.

---

## Standard Directory Layout

```text
my-app/
├── pom.xml                        # Project Object Model configuration file
├── src/
│   ├── main/
│   │   ├── java/                  # Application Java source code (.java)
│   │   │   └── com/example/app/
│   │   │       └── App.java
│   │   ├── resources/             # Application resources (properties, XML, SQL scripts)
│   │   └── webapp/                # Web application sources (HTML, JSP, web.xml) - for WARs
│   │
│   └── test/
│       ├── java/                  # Unit and integration test source code (.java)
│       │   └── com/example/app/
│       │       └── AppTest.java
│       └── resources/             # Test resources and test configuration files
│
└── target/                        # Build output directory (generated automatically, ignored in Git)
    ├── classes/                   # Compiled application bytecode (.class)
    ├── test-classes/              # Compiled test bytecode (.class)
    ├── surefire-reports/          # Test execution XML & TXT reports
    └── my-app-1.0.0.jar           # Final packaged artifact
```

---

## Detailed Directory Breakdown

### 1. `pom.xml`
Located at the root of the project. Contains project metadata, dependencies, build configurations, and plugin definitions.

### 2. `src/main/java/`
Houses all production Java code. Packages match the directory path (e.g., `com.example.app` package lives under `src/main/java/com/example/app/`).

### 3. `src/main/resources/`
Contains non-Java assets required at runtime (e.g., `application.properties`, log configuration `logback.xml`, database migration scripts). Files in this directory are copied directly to the root of the compiled classpath (`target/classes`).

### 4. `src/test/java/`
Contains test suites written using frameworks like JUnit or TestNG. These classes are compiled during the test execution phase but are **not** bundled into the final production package (JAR/WAR).

### 5. `src/test/resources/`
Contains assets used strictly during test runs (e.g., mock dataset files, test properties). Copied to `target/test-classes`.

### 6. `target/`
The build output folder. Every file generated during the Maven build lifecycle (`mvn compile`, `mvn test`, `mvn package`) is placed inside `target/`. 

> **Important**: The `target/` directory should **never** be committed to version control. It is cleaned and regenerated via `mvn clean`.

---

## Why Standard Structure Matters

1. **Zero Configuration**: Maven plugins automatically read from `src/main/java` and output to `target/`.
2. **Instant Developer Familiarity**: Any Java developer opening a Maven project immediately understands where source files and resources reside.
3. **Tool & IDE Interoperability**: IntelliJ IDEA, Eclipse, VS Code, and CI/CD pipelines natively understand and process the standard layout.
