package vn.edu.stu.com.example.da_meetingroom.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "meeting_rooms") // Đảm bảo tên bảng này khớp với ý định của bạn
public class MeetingRoom implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int capacity;
    public String location;
    public String type;
    public String status;

    // CHỈ CÓ DUY NHẤT 1 Constructor này là không có @Ignore để Room sử dụng
    public MeetingRoom() {
    }

    // Tất cả các Constructor khác BẮT BUỘC phải có @Ignore
    @Ignore
    public MeetingRoom(String name, int capacity, String location, String type, String status) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
        this.type = type;
        this.status = status;
    }

    @Ignore
    public MeetingRoom(String name) {
        this.name = name;
        this.capacity = 10;
        this.location = "Tầng 1";
        this.type = "Standard";
        this.status = "Đang trống";
    }

    @NonNull
    @Override
    public String toString() {
        return name != null ? name : "";
    }
}