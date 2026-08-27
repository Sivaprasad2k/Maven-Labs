package com.shevay.monitor.server;

import com.shevay.monitor.config.AppConfig;
import com.shevay.monitor.service.SystemMonitorService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitorHttpServer {

    private static final Logger LOGGER = Logger.getLogger(MonitorHttpServer.class.getName());

    private final AppConfig config;
    private final SystemMonitorService monitorService;
    private HttpServer server;
    private ExecutorService executor;

    public MonitorHttpServer(AppConfig config, SystemMonitorService monitorService) {
        this.config = config;
        this.monitorService = monitorService;
    }

    public void start() throws IOException {
        int port = config.getPort();
        server = HttpServer.create(new InetSocketAddress(port), 0);
        executor = Executors.newFixedThreadPool(10);
        server.setExecutor(executor);

        server.createContext("/api/health", createHandler(exchange -> 
            monitorService.getHealth("ec2-system-monitor")
        ));
        server.createContext("/api/system", createHandler(exchange -> 
            monitorService.getSystemInfo()
        ));
        server.createContext("/api/metrics", createHandler(exchange -> 
            monitorService.getResourceMetrics()
        ));
        server.createContext("/api/disk", createHandler(exchange -> 
            monitorService.getDiskInfo()
        ));

        // Default handler for unregistered paths
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || path.startsWith("/api/")) {
                HttpResponseUtil.sendErrorResponse(exchange, 404, "Endpoint Not Found: " + path);
            } else {
                HttpResponseUtil.sendErrorResponse(exchange, 404, "Not Found");
            }
        });

        server.start();
        LOGGER.info("EC2 System Monitor HTTP Server started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(1); // 1 second delay
            LOGGER.info("HTTP Server stopped.");
        }
        if (executor != null) {
            executor.shutdown();
            LOGGER.info("Server executor pool shut down.");
        }
    }

    private HttpHandler createHandler(ServiceSupplier supplier) {
        return exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    HttpResponseUtil.sendErrorResponse(exchange, 405, "Method Not Allowed. Only GET is supported.");
                    return;
                }

                String path = exchange.getRequestURI().getPath();
                // Exact path matching check
                if (!isValidPath(exchange, path)) {
                    HttpResponseUtil.sendErrorResponse(exchange, 404, "Endpoint Not Found: " + path);
                    return;
                }

                Object responseModel = supplier.get(exchange);
                HttpResponseUtil.sendJsonResponse(exchange, 200, responseModel);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Internal server error processing request: " + exchange.getRequestURI(), e);
                HttpResponseUtil.sendErrorResponse(exchange, 500, "Internal Server Error");
            }
        };
    }

    private boolean isValidPath(HttpExchange exchange, String path) {
        String contextPath = exchange.getHttpContext().getPath();
        return contextPath.equals(path);
    }

    @FunctionalInterface
    private interface ServiceSupplier {
        Object get(HttpExchange exchange) throws Exception;
    }
}
