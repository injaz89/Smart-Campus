package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a discovery entry point for the Smart Campus API.
 * Maps to the root of the API path (/api/v1).
 */
@Path("/")
public class DiscoveryResource {

    /**
     * Handles GET requests to the root API path.
     * Returns API metadata and discovery links for available resources.
     *
     * @return A JSON response containing Version, Admin Contact, and a Map of resource links.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDiscoveryInfo() {
        
        // Create the main map to hold our JSON response data
        Map<String, Object> responseData = new HashMap<>();
        
        // Add version and admin contact details as requested
        responseData.put("version", "1.0.0");
        responseData.put("adminContact", "w2120341@my.westminster.ac.uk");
        
        // Create a nested map for all available resource endpoints
        Map<String, String> links = new HashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        links.put("readings", "/api/v1/readings");
        
        // Attach the links map to the main response map
        responseData.put("links", links);
        
        // Return the structured map wrapped in an HTTP 200 OK Response
        return Response.ok(responseData).build();
    }
}
