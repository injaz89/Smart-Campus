package com.smartcampus.resource;

import com.smartcampus.model.Room;
import com.smartcampus.store.CampusDataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("/rooms")
public class RoomResource {

    // Access the singleton data store
    private final CampusDataStore dataStore = CampusDataStore.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        // Retrieve all rooms from the store
        List<Room> rooms = dataStore.getAllRooms();
        // Return JSON list of rooms
        return Response.ok(rooms).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room newRoom, @Context UriInfo uriInfo) {
        // Generate an ID if not provided
        if (newRoom.getId() == null || newRoom.getId().isEmpty()) {
            newRoom.setId(UUID.randomUUID().toString());
        }
        
        // Save the room to the data store
        dataStore.addRoom(newRoom);
        
        // Build the URI for the newly created room dynamically
        URI location = uriInfo.getAbsolutePathBuilder().path(newRoom.getId()).build();
        
        // Return 201 Created with Location header and the created entity
        return Response.created(location).entity(newRoom).build();
    }

    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        // Fetch the room by ID
        Room room = dataStore.getRoomById(roomId);
        
        // Check if the room exists
        if (room == null) {
            // Return 404 Not Found if missing
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        // Return the room details
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        // Attempt to delete. Throws RoomNotEmptyException if it has active sensors or doesn't exist
        // The exception will be automatically mapped to a 409 Conflict.
        dataStore.deleteRoom(roomId);
        
        // Return 204 No Content for successful deletion
        return Response.noContent().build();
    }
}
