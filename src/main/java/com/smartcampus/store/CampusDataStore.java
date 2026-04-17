package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

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
    private final Map<String, SensorReading> readings = new ConcurrentHashMap<>();

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

    public Map<String, SensorReading> getReadings() {
        return readings;
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
}
