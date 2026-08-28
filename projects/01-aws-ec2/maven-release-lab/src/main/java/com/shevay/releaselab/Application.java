package com.shevay.releaselab;

import com.shevay.releaselab.config.AppConfig;
import com.shevay.releaselab.server.MonitorHttpServer;
import com.shevay.releaselab.service.ReleaseLabService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {

    private static final Logger LOGGER = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        ReleaseLabService service = new ReleaseLabService();
        MonitorHttpServer server = new MonitorHttpServer(config, service);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown signal received. Stopping Maven Release Lab...");
            server.stop();
        }));

        try {
            server.start();
            LOGGER.info("Application is running (Profile: " + config.getAppProfile() + ", Env: " + config.getAppEnvironment() + "). Press Ctrl+C to terminate.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start Maven Release Lab HTTP Server", e);
            System.exit(1);
        }
    }
}
