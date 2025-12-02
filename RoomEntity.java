
package com.example.meetingbocking;

import androidx.room.Entity;

@Entity(tableName = "rooms")
public class RoomEntity {
    @com.example.meetingbocking.PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int capacity;
    public String equipments; // comma-separated

    public RoomEntity(String name, int capacity, String equipments) {
        this.name = name;
        this.capacity = capacity;
        this.equipments = equipments;
    }
}
