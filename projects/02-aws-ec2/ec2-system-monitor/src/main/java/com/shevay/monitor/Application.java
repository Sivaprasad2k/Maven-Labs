package com.shevay.monitor;

import com.shevay.monitor.config.AppConfig;
import com.shevay.monitor.server.MonitorHttpServer;
import com.shevay.monitor.service.SystemMonitorService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        SystemMonitorService monitorService = new SystemMonitorService();
        MonitorHttpServer server = new MonitorHttpServer(config, monitorService);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown signal received. Stopping EC2 System Monitor...");
            server.stop();
        }));

        try {
            server.start();
            LOGGER.info("Application is running. Press Ctrl+C to terminate.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start EC2 System Monitor HTTP Server", e);
            System.exit(1);
        }
    }
}
