package com.shevay.oddlyspecific.server;

import com.shevay.oddlyspecific.challenge.ChallengeEngine;
import com.shevay.oddlyspecific.session.SessionManager;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {

    private final HttpServer server;
    private final int port;
    private final String host;

    public WebServer(String host, int port) throws IOException {
        this(host, port, new SessionManager(), new ChallengeEngine());
    }

    public WebServer(String host, int port, SessionManager sessionManager, ChallengeEngine challengeEngine) throws IOException {
        this.host = host;
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.port = this.server.getAddress().getPort(); // Actual bound port

        this.server.setExecutor(Executors.newFixedThreadPool(16));

        // Handlers
        StaticFileHandler staticFileHandler = new StaticFileHandler();
        ApiHandler apiHandler = new ApiHandler(sessionManager, challengeEngine);
        AdminHandler adminHandler = new AdminHandler(sessionManager);

        this.server.createContext("/", staticFileHandler);
        this.server.createContext("/api", apiHandler);
        this.server.createContext("/admin", adminHandler);
        this.server.createContext("/api/admin", adminHandler);
    }

    public void start() {
        server.start();
        System.out.println("==================================================");
        System.out.println("Oddly Specific Server running at http://" + host + ":" + port);
        System.out.println("==================================================");
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
        System.out.println("Oddly Specific Server stopped.");
    }

    public int getPort() {
        return port;
    }
}
