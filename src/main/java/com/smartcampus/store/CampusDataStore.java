package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory runtime data store for the Smart Campus context.
 * Utilizes ConcurrentHashMap to ensure thread-safety during concurrent operations.
 */
public class CampusDataStore {

    // Eagerly initialized singleton instance
    private static final CampusDataStore instance = new CampusDataStore();

    // Data maps for in-memory storage
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readingsHistory = new ConcurrentHashMap<>();

    private CampusDataStore() {
        // Private constructor for singleton pattern
    }

    /**
     * Retrieve the singleton instance of the data store.
     */
    public static CampusDataStore getInstance() {
        return instance;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getReadingsHistory() {
        return readingsHistory;
    }

    // Return a list of all rooms
    public java.util.List<Room> getAllRooms() {
        return new java.util.ArrayList<>(rooms.values());
    }

    // Add a new room to the map
    public void addRoom(Room room) {
        if (room != null && room.getId() != null) {
            rooms.put(room.getId(), room);
        }
    }

    // Find a specific room by id
    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    // Check if a room exists
    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    // Check if room exists and has no sensors
    public boolean canDeleteRoom(String roomId) {
        Room room = getRoomById(roomId);
        if (room == null) {
            return false;
        }
        // Iterate through all sensors to see if any belong to this room
        for (Sensor sensor : sensors.values()) {
            if (roomId.equals(sensor.getRoomId())) {
                return false;
            }
        }
        return true;
    }

    // Remove the room from the data store
    public void deleteRoom(String roomId) {
        if (!canDeleteRoom(roomId)) {
            throw new com.smartcampus.model.errors.RoomNotEmptyException("Cannot delete room: It contains active sensors or does not exist.");
        }
        rooms.remove(roomId);
    }

    // Add a new sensor to the map, validating its room first
    public void addSensor(Sensor sensor) {
        if (!roomExists(sensor.getRoomId())) {
            throw new com.smartcampus.model.errors.LinkedResourceNotFoundException("Cannot add sensor: Room with ID " + sensor.getRoomId() + " does not exist.");
        }
        if (sensor != null && sensor.getId() != null) {
            sensors.put(sensor.getId(), sensor);
            Room room = getRoomById(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().add(sensor.getId());
            }
        }
    }

    // Add a reading into the history and update the parent sensor's current value side-effect
    public void addReading(String sensorId, SensorReading reading) {
        // Initialize list and add the reading
        readingsHistory.putIfAbsent(sensorId, new java.util.concurrent.CopyOnWriteArrayList<>());
        readingsHistory.get(sensorId).add(reading);
        
        // Find the sensor and dynamically update its current value
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }
}
