package vn.edu.stu.com.example.da_meetingroom.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendees")
public class Attendee {
    @PrimaryKey(autoGenerate = true) // Phải có ID riêng tự tăng
    public int id;

    public int meetingId; // Cái này KHÔNG ĐƯỢC là PrimaryKey
    public int userId;

    public Attendee(int meetingId, int userId) {
        this.meetingId = meetingId;
        this.userId = userId;
    }
}
