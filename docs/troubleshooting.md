# Maven Troubleshooting & Error Reference

This guide documents common errors encountered when working with Apache Maven across local CLI, AWS EC2 Linux, and IDE environments, complete with root cause analyses and step-by-step solutions.

---

## 1. `The goal you specified requires a project to execute but there is no POM in this directory`

### Error Output Example:
```text
[ERROR] Goal require a project to execute but there is no POM in this directory (/home/ec2-user). 
[ERROR] Please verify you invoked Maven from the correct directory.
```

### Cause:
You executed a goal that relies on project context (such as `mvn compile`, `mvn test`, or `mvn package`) from a working directory that does not contain a `pom.xml` file.

### Solution:
1. Verify your current working directory:
   ```bash
   pwd    # Linux / macOS
   cd     # Windows PowerShell / CMD
   ```
2. Navigate into the directory containing `pom.xml`:
   ```bash
   cd ~/Maven/shevay-app
   ```
3. Re-run your Maven command:
   ```bash
   mvn package
   ```

> **Note**: Standard standalone goals like `mvn archetype:generate` or `mvn -version` can be executed outside a project directory, but lifecycle goals (`compile`, `test`, `package`) require a local `pom.xml`.

---

## 2. `mvn : The term 'mvn' is not recognized as the name of a cmdlet`

### Cause:
The Maven binary directory (`bin`) is not included in your operating system's system `PATH` environment variable.

### Solution (Windows):
1. Locate your Maven installation path (e.g., `C:\Program Files\apache-maven-3.9.5`).
2. Set Environment Variable `MAVEN_HOME` = `C:\Program Files\apache-maven-3.9.5`.
3. Append `%MAVEN_HOME%\bin` to system `PATH`.
4. Restart your terminal session and verify with `mvn -version`.

---

## 3. `Fatal error compiling: invalid target release: 17`

### Cause:
The `pom.xml` requires Java 17 target compatibility (`<maven.compiler.target>17</maven.compiler.target>`), but the JDK being used by Maven is older (e.g., JDK 8 or JDK 11).

### Solution:
1. Check the active JDK version used by Maven:
   ```bash
   mvn -version
   ```
2. Ensure `JAVA_HOME` points to a valid Java 17+ installation directory.
3. Update `pom.xml` properties if compiling for an earlier Java version:
   ```xml
   <properties>
       <maven.compiler.source>11</maven.compiler.source>
       <maven.compiler.target>11</maven.compiler.target>
   </properties>
   ```

---

## 4. `Could not resolve dependencies for project` / Repository Connection Failures

### Cause:
Maven cannot download a required library due to network connectivity, missing proxy configurations, or incorrect GAV coordinates.

### Solution:
1. Run build with debug output to pinpoint network failures:
   ```bash
   mvn clean package -e -X
   ```
2. Force Maven to re-check and download missing dependencies from remote repositories:
   ```bash
   mvn clean package -U
   ```
3. Check `~/.m2/settings.xml` for incorrect mirror or proxy settings.

---

## Summary Diagnostic Checklist

- [ ] Am I inside the directory containing `pom.xml`?
- [ ] Does `mvn -version` display the expected Java version?
- [ ] Are all dependency GroupID, ArtifactID, and Version tags correctly spelled?
- [ ] Have I tried forcing an artifact update with `-U`?
