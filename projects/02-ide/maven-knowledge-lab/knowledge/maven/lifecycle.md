# Apache Maven Build Lifecycles

Apache Maven operates on a sequence of build lifecycles consisting of ordered phases.

## Default Build Lifecycle
The default lifecycle handles project compilation, testing, and packaging:
1. **validate**: Validates project structure and required configuration information.
2. **compile**: Compiles main Java source code into bytecode class files.
3. **test**: Executes unit test suites using Surefire without packaging compiled code.
4. **package**: Packages compiled code into distribution formats such as JAR or WAR files.
5. **verify**: Runs checks on integration test results to ensure quality criteria are met.
6. **install**: Installs the packaged artifact into the local Maven repository (`~/.m2/repository`).
7. **deploy**: Copies final artifacts to remote distribution repositories.

## Clean and Site Lifecycles
- **clean**: Removes build output directory (`target/`).
- **site**: Generates project documentation site pages.
