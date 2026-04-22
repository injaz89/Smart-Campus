package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class to bootstrap the embedded Grizzly HTTP Server.
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and providers
        final ResourceConfig rc = new ResourceConfig().packages("com.smartcampus");

        // Create and start a new instance of grizzly http server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            
            System.out.println("\n---------------------------------------------------------");
            System.out.println("[INFO] Bootstrapping Smart Campus API...");
            System.out.println("[INFO] Initializing JAX-RS ResourceConfig...");
            System.out.println("[INFO] Deploying endpoints on Grizzly HTTP Server...");
            System.out.println("\n[SUCCESS] Smart Campus API is now ONLINE!");
            System.out.println("---------------------------------------------------------");
            System.out.println("Base Web URI:    " + BASE_URI);
            System.out.println("\nAvailable Endpoint Groups:");
            System.out.println("  -> [GET]               /api/v1/         (Discovery)");
            System.out.println("  -> [GET|POST|DELETE]   /api/v1/rooms    (Room Management)");
            System.out.println("  -> [GET|POST]          /api/v1/sensors  (Sensor Context)");
            System.out.println("  -> [GET|POST]          .../readings     (Telemetry History)");
            System.out.println("\n** Application is actively listening on port 8080 **");
            System.out.println("Stop the continuous NetBeans process (or hit Ctrl-C) to terminate.");
            System.out.println("---------------------------------------------------------\n");

            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}