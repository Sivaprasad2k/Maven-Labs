# EC2 System Monitor

A lightweight, zero-framework Java 17 HTTP monitoring service running directly on an AWS EC2 Linux instance. It exposes structured metrics about the JVM, Java process, operating system, CPU, memory, and filesystem via a REST-style JSON HTTP API.

---

## 1. Project Purpose
The purpose of `ec2-system-monitor` is to provide a real-time, low-overhead system monitoring endpoint on AWS EC2 without deploying heavy enterprise frameworks.

## 2. Why This Project Exists
This project serves as a hands-on learning experiment to understand the direct relationship between Java, the JVM, the operating system, process management, file storage, HTTP networking, and cloud Linux environments.

## 3. Why Java's Built-in HTTP Server is Used Instead of Spring Boot
Instead of pulling in Spring Boot (which abstracts web server creation, auto-configuration, and dependency injection), this service directly uses `com.sun.net.httpserver.HttpServer`. This demonstrates what happens under the hood before abstractions like Spring Web or Tomcat are introduced.

---

## 4. Architecture

A layered monolithic architecture separates HTTP transport, business service, configuration, and domain models:

```text
HTTP Client (curl / browser)
        │
        ▼
MonitorHttpServer  ◄──  AppConfig (PORT = 8080 / env)
  (HttpServer, ThreadPool)
        │
        ├──► HttpResponseUtil (Jackson JSON Serialization, Status Codes)
        │
        ▼
SystemMonitorService
        │
        ├──► JVM APIs (Runtime.getRuntime())
        ├──► OS APIs (ManagementFactory.getOperatingSystemMXBean())
        ├──► Process APIs (ProcessHandle.current())
        └──► Filesystem APIs (File / Java NIO)
```

- **`Application`**: Entry point and lifecycle management (JVM shutdown hooks).
- **`AppConfig`**: Manages environment configuration (`PORT`).
- **`MonitorHttpServer`**: Configures `com.sun.net.httpserver.HttpServer`, registers context routes, and manages thread pool executors.
- **`HttpResponseUtil`**: Centralized HTTP headers, JSON serialization with Jackson, and error response formatting.
- **`SystemMonitorService`**: Core metric collection service using Java standard APIs.
- **Models**: `HealthResponse`, `SystemInfo`, `ResourceMetrics`, `DiskInfo`.

---

## 5. Project Structure

```text
ec2-system-monitor/
├── README.md
├── pom.xml
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── shevay/
    │               └── monitor/
    │                   ├── Application.java
    │                   ├── config/
    │                   │   └── AppConfig.java
    │                   ├── model/
    │                   │   ├── DiskInfo.java
    │                   │   ├── HealthResponse.java
    │                   │   ├── ResourceMetrics.java
    │                   │   └── SystemInfo.java
    │                   ├── server/
    │                   │   ├── HttpResponseUtil.java
    │                   │   └── MonitorHttpServer.java
    │                   └── service/
    │                       └── SystemMonitorService.java
    └── test/
        └── java/
            └── com/
                └── shevay/
                    └── monitor/
                        ├── config/
                        │   └── AppConfigTest.java
                        ├── server/
                        │   └── MonitorHttpServerTest.java
                        └── service/
                            └── SystemMonitorServiceTest.java
```

---

## 6. Maven Configuration & Dependencies
- **`groupId`**: `com.shevay`
- **`artifactId`**: `ec2-system-monitor`
- **`version`**: `1.0-SNAPSHOT`
- **`packaging`**: `jar`

### Dependencies:
- `com.fasterxml.jackson.core:jackson-databind:2.15.2`: High-performance JSON serialization.
- `org.junit.jupiter:junit-jupiter:5.10.0`: Unit testing framework (`test` scope).

## 7. Java 17 Configuration
Built explicitly for Java 17 using `<maven.compiler.release>17</maven.compiler.release>` and `maven-compiler-plugin:3.11.0`.

---

## 8. System Information Sources & Metrics

### 9. JVM Memory vs. System Memory
- **JVM Memory**: Reported via `Runtime.getRuntime()`. Reflects heap memory allocated to this Java process (`usedMemoryBytes`, `committedMemoryBytes`, `maxMemoryBytes`).
- **System Memory**: Total OS RAM. Kept distinct to avoid confusion between JVM heap limits and total EC2 instance memory.

### 10. Process Uptime vs. EC2 Instance Uptime
- **Process Uptime**: Obtained via `ProcessHandle.current().info().startInstant()`. Reports how long *this Java application process* has been running.
- **EC2 Instance Uptime**: Total uptime of the EC2 Linux virtual machine since boot.

---

## 11. API Documentation

### 1. `GET /api/health`
- **Purpose**: Liveness check endpoint.
- **Status Code**: `200 OK`
- **Example Response**:
```json
{
  "status": "UP",
  "timestamp": "2026-08-28T01:50:42.123456Z",
  "service": "ec2-system-monitor"
}
```

### 2. `GET /api/system`
- **Purpose**: Static system and runtime metadata.
- **Status Code**: `200 OK`
- **Example Response**:
```json
{
  "hostname": "ip-172-31-39-135",
  "osName": "Linux",
  "osVersion": "6.18.41-94.142.amzn2023.x86_64",
  "architecture": "amd64",
  "javaVersion": "17.0.20",
  "availableProcessors": 2
}
```

### 3. `GET /api/metrics`
- **Purpose**: Dynamic CPU, JVM heap memory, process PID, and uptime metrics.
- **Status Code**: `200 OK`
- **Example Response**:
```json
{
  "systemCpuLoad": 0.05,
  "systemLoadAverage": 0.12,
  "jvmUsedMemoryBytes": 14680064,
  "jvmCommittedMemoryBytes": 33554432,
  "jvmMaxMemoryBytes": 536870912,
  "pid": 14205,
  "processUptimeSeconds": 3600
}
```

### 4. `GET /api/disk`
- **Purpose**: Storage disk space utilization for application partition.
- **Status Code**: `200 OK`
- **Example Response**:
```json
{
  "path": "/home/ec2-user/Maven/Maven-Labs/projects/02-aws-ec2/ec2-system-monitor/.",
  "totalBytes": 16106127360,
  "freeBytes": 10737418240,
  "usableBytes": 10737418240,
  "usedBytes": 5368709120
}
```

---

## 12. Running & Testing

### Running Locally / on EC2:
```bash
# Build executable shaded JAR
mvn clean package

# Run with default port 8080
java -jar target/ec2-system-monitor-1.0-SNAPSHOT.jar

# Run with custom port 9090
PORT=9090 java -jar target/ec2-system-monitor-1.0-SNAPSHOT.jar
```

### `curl` Verification Commands:
```bash
curl -i http://localhost:8080/api/health
curl -i http://localhost:8080/api/system
curl -i http://localhost:8080/api/metrics
curl -i http://localhost:8080/api/disk
curl -i http://localhost:8080/api/unknown-path   # Returns HTTP 404
```

---

## 13. Concurrency & Error Handling
- **Bounded Executor**: Uses `Executors.newFixedThreadPool(10)` to prevent thread starvation under concurrent requests.
- **Graceful Shutdown**: Registers a JVM shutdown hook (`Runtime.getRuntime().addShutdownHook`) that stops the HTTP server and shuts down executor threads cleanly.
- **Error Safety**: Exceptions are logged server-side; clients receive clean JSON error payloads (`HTTP 404`, `HTTP 405`, `HTTP 500`) without stack trace leaks.

---

## 14. Testing Suite
Includes 15 automated unit tests (`AppConfigTest`, `SystemMonitorServiceTest`, `MonitorHttpServerTest`) verifying environment configuration, Java metrics collection, route handling, HTTP status codes, and JSON response formatting.

---

## 15. Conceptual Mapping to Spring Boot

| Feature in `ec2-system-monitor` | Equivalent in Spring Boot |
| :--- | :--- |
| `com.sun.net.httpserver.HttpServer` | Embedded Tomcat / Jetty (`spring-boot-starter-web`) |
| `AppConfig` | `@ConfigurationProperties` / `application.properties` |
| `HttpResponseUtil` + Jackson | `@RestController` + Spring MVC MessageConverters |
| `SystemMonitorService` | `@Service` Spring Bean |
| `SystemMonitorServiceTest` | `@WebMvcTest` / `@SpringBootTest` |
| `/api/health` & `/api/metrics` | Spring Boot Actuator (`/actuator/health`, `/actuator/metrics`) |
