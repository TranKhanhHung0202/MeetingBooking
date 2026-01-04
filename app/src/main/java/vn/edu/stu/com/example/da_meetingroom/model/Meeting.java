package vn.edu.stu.com.example.da_meetingroom.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "meetings")
public class Meeting {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int roomId;      // ID của phòng họp
    public int leaderId;    // ID người tạo cuộc họp
    public String title;
    public String meetingDate;
    public String startTime;
    public String endTime;
    public String roomName; // Lưu tên phòng để hiển thị trực tiếp lên list mà không cần JOIN

    // Nếu bạn đã dùng bảng Attendee để lọc nhân viên, có thể giữ hoặc xóa trường này.
    // Nếu giữ lại, hãy dùng nó để lưu nhanh danh sách tên người tham gia chẳng hạn.
    public String participants;

    /**
     * CONSTRUCTOR 1: Dành cho Room Database.
     * Room sẽ sử dụng constructor rỗng này để khởi tạo đối tượng sau đó gán dữ liệu vào các trường public.
     */
    public Meeting() {
    }

    /**
     * CONSTRUCTOR 2: Dành cho lập trình viên (AddMeetingActivity).
     * Dùng để tạo nhanh một đối tượng Meeting khi thêm mới.
     * PHẢI CÓ @Ignore để Room không bị bối rối giữa 2 constructor.
     */
    @Ignore
    public Meeting(int roomId, int leaderId, String title, String meetingDate, String startTime, String endTime) {
        this.roomId = roomId;
        this.leaderId = leaderId;
        this.title = title;
        this.meetingDate = meetingDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // --- Getters và Setters (Để Code chuyên nghiệp và dễ quản lý hơn) ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getLeaderId() { return leaderId; }
    public void setLeaderId(int leaderId) { this.leaderId = leaderId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMeetingDate() { return meetingDate; }
    public void setMeetingDate(String meetingDate) { this.meetingDate = meetingDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
}