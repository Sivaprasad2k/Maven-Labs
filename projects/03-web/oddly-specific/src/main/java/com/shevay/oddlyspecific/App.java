package com.shevay.oddlyspecific;

import com.shevay.oddlyspecific.config.AppConfig;
import com.shevay.oddlyspecific.server.WebServer;

public class App {
    public static void main(String[] args) {
        int port = AppConfig.getPort();
        String host = AppConfig.DEFAULT_HOST;

        try {
            WebServer webServer = new WebServer(host, port);
            webServer.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down Oddly Specific server...");
                webServer.stop(1);
            }));
        } catch (Exception e) {
            System.err.println("Fatal error starting Oddly Specific server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
