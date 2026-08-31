# Maven Dependency Management

Maven automates external library management using GroupId, ArtifactId, and Version (GAV) coordinates.

## Dependency Scopes
- **compile**: Default scope. Available on classpath for compilation, testing, and runtime.
- **test**: Available only for test compilation and execution (e.g., JUnit Jupiter).
- **provided**: Expected to be provided by JDK or container at runtime (e.g., Servlet API).
- **runtime**: Required for execution but not compilation.

## Transitive Dependency Resolution
Maven builds a dependency tree resolving transitive dependencies automatically. Conflict resolution follows nearest-definition rules.
