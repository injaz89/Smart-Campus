package com.smartcampus.resource;

import com.smartcampus.model.SensorReading;
import com.smartcampus.store.CampusDataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Sub-Resource Locator for nested history management of Sensor Readings.
 * Deliberately lacks a class-level @Path annotation.
 */
public class SensorReadingResource {

    private final String sensorId;
    private final CampusDataStore dataStore = CampusDataStore.getInstance();

    // Accepts the parent sensor context through the constructor
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // Handles GET for all readings associated with the given sensorId
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> history = dataStore.getReadingsHistory().get(this.sensorId);
        
        // If no readings exist yet, return an empty array instead of null
        if (history == null) {
            history = Collections.emptyList();
        }
        
        return Response.ok(history).build();
    }

    // Handles POST to add a new reading to this specific sensor history
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading newReading, @Context UriInfo uriInfo) {
        // Validation for MAINTENANCE mode
        com.smartcampus.model.Sensor sensor = dataStore.getSensors().get(this.sensorId);
        if (sensor != null && "MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new com.smartcampus.model.errors.SensorUnavailableException("Sensor is in MAINTENANCE mode and cannot accept new readings.");
        }

        // Enforce the parent context relationship
        newReading.setSensorId(this.sensorId);
        
        // Generate an ID if one isn't supplied
        if (newReading.getId() == null || newReading.getId().isEmpty()) {
            newReading.setId(UUID.randomUUID().toString());
        }
        
        // Add the reading to the datastore
        dataStore.addReading(this.sensorId, newReading);
        
        // Build the robust URI for the newly created reading
        URI location = uriInfo.getAbsolutePathBuilder().path(newReading.getId()).build();
        
        return Response.created(location).entity(newReading).build();
    }
}
