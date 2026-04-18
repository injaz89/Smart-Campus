package com.smartcampus.resource;

import com.smartcampus.model.Sensor;
import com.smartcampus.store.CampusDataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Maps all requests for /api/v1/sensors to this class
@Path("/sensors")
public class SensorResource {

    // Access the singleton data store
    private final CampusDataStore dataStore = CampusDataStore.getInstance();

    // Handles POST requests to create new sensors
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor newSensor, @Context UriInfo uriInfo) {
        // Generate an ID if not provided
        if (newSensor.getId() == null || newSensor.getId().isEmpty()) {
            newSensor.setId(UUID.randomUUID().toString());
        }
        
        try {
            // Attempt to add the sensor to validate the room relationship
            dataStore.addSensor(newSensor);
            
            // Build the URI for the newly created sensor dynamically
            URI location = uriInfo.getAbsolutePathBuilder().path(newSensor.getId()).build();
            
            // Return 201 Created with Location header and the generated entity
            return Response.created(location).entity(newSensor).build();
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request if validation (e.g., room existence) fails
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"" + e.getMessage() + "\"}")
                           .build();
        }
    }

    // Handles GET requests for specific sensor details
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("id") String id) {
        // Fetch the sensor directly from the datastore map
        Sensor sensor = dataStore.getSensors().get(id);
        
        // Check if the sensor exists
        if (sensor == null) {
            // Return 404 Not Found if missing
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Return 200 OK with the sensor specifics
        return Response.ok(sensor).build();
    }

    // Handles GET requests to retrieve all sensors with optional type filtering
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        // Get all sensors as a list
        List<Sensor> allSensors = new java.util.ArrayList<>(dataStore.getSensors().values());
        
        // If type parameter is present, return a filtered list
        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filteredSensors = allSensors.stream()
                .filter(sensor -> type.equalsIgnoreCase(sensor.getType()))
                .collect(Collectors.toList());
            return Response.ok(filteredSensors).build();
        }
        
        // Return the full list if no filter was provided
        return Response.ok(allSensors).build();
    }
}
