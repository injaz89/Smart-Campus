package com.smartcampus.model;

/**
 * Represents a sensor deployed in a room.
 */
public class Sensor {
    private String id;
    private String roomId;
    private String type; // e.g., TEMPERATURE, HUMIDITY, MOTION
    private String status; // e.g., ACTIVE, INACTIVE, MAINTENANCE

    public Sensor() {
        // Default constructor required for JSON mapping
    }

    public Sensor(String id, String roomId, String type, String status) {
        this.id = id;
        this.roomId = roomId;
        this.type = type;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
