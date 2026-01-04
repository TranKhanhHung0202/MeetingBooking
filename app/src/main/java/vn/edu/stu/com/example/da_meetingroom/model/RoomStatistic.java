package vn.edu.stu.com.example.da_meetingroom.model;

public class RoomStatistic {
    public String roomName;
    public int meetingCount;

    // Constructor để Room có thể đổ dữ liệu vào
    public RoomStatistic(String roomName, int meetingCount) {
        this.roomName = roomName;
        this.meetingCount = meetingCount;
    }
}