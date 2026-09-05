# Oddly Specific

> A lightweight Java 17 web experiment exploring browser permissions, ephemeral session state, privacy boundaries, and controlled theatrical UX without heavy frameworks.

---

## Technology Stack

- **Backend**: Java 17, JDK `com.sun.net.httpserver.HttpServer`, Jackson Databind (2.17.0), JUnit 5 (5.10.2).
- **Frontend**: HTML5, Vanilla CSS3 (Custom Properties, Flexbox/Grid, Animations), Vanilla JavaScript (ES6+ State Machine, Geolocation API).
- **Build System**: Maven with Maven Wrapper (`./mvnw`).
- **Deployment Target**: Railway ready (zero local disk or DB dependencies, binds to `0.0.0.0:${PORT}`).

---

## Architecture & Concepts

"Oddly Specific" demonstrates key web engineering concepts:
1. **HTTP Server Bootstrap**: Direct usage of `com.sun.net.httpserver.HttpServer` with non-blocking multi-threaded request processing.
2. **Session Engine**: Thread-safe in-memory session manager (`ConcurrentHashMap`) storing ephemeral state without databases.
3. **Browser Permission & Geolocation**: Client-side `navigator.geolocation` API handling explicit user permissions, graceful denial fallbacks, and privacy isolation.
4. **Privacy Boundaries**: Strictly resolving connection IP directly from `HttpExchange` socket/headers while distinguishing between browser permission location vs network connection IP.
5. **Interactive Experience Engine**: Random challenge selector serving micro-interactions (Reaction Test, Memory Sequence, Don't Click Challenge, Moving Button, Absurd Captcha, Number Challenge).
6. **Theatrical UX & Educational Reveal**: Controlled state machine transitioning from theatrical analysis to truthful breakdown ("WHAT ACTUALLY HAPPENED?").

---

## Local Development

### Prerequisites
- JDK 17+ installed and configured in `JAVA_HOME`.

### Running Tests
```bash
./mvnw test
```

### Packaging & Executing
```bash
./mvnw package
java -jar target/oddly-specific-1.0-SNAPSHOT.jar
```

Or run directly with Maven:
```bash
./mvnw exec:java
```

Access the application in your browser at: `http://localhost:8080` (or custom `$PORT`).

---

## Deployment (Railway)

This project is fully prepared for Railway deployment out of the box:
- Binds automatically to host `0.0.0.0`.
- Listens to the `PORT` environment variable provided by Railway.
- Implements self-contained static resource loading and in-memory state.
