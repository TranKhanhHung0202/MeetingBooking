package vn.edu.stu.com.example.da_meetingroom.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.model.Meeting;
import vn.edu.stu.com.example.da_meetingroom.model.StatItem;


@Dao
public interface MeetingDao {

    // Admin dùng cái này
    @Query("SELECT * FROM meetings")
    List<Meeting> getAllMeetings();


    @Insert
    long insert(Meeting meeting);

    @Update
    void update(Meeting meeting);

    @Delete
    void delete(Meeting meeting);

    @Query("SELECT * FROM meetings WHERE id = :id")
    Meeting getMeetingById(int id);
    @Query("SELECT meetings.* FROM meetings " +
            "INNER JOIN attendees ON meetings.id = attendees.meetingId " +
            "WHERE attendees.userId = :userId AND meetings.title LIKE :query")
    List<Meeting> searchMeetingsForUser(String query, int userId);

    @Query("SELECT meetings.* FROM meetings " +
            "INNER JOIN attendees ON meetings.id = attendees.meetingId " +
            "WHERE attendees.userId = :userId")
    List<Meeting> getMeetingsForUser(int userId);

    @Query("SELECT * FROM meetings WHERE title LIKE '%' || :query || '%' ORDER BY meetingDate DESC")
    List<Meeting> searchMeetings(String query);

    // --- PHẦN SỬA LỖI LỘN XỘN GIỮA ID VÀ NAME ---

    // 1. Kiểm tra trùng lịch khi THÊM MỚI
    // Sửa cột so sánh thành 'roomId' và tham số là 'roomId' (int)
    @Query("SELECT COUNT(*) FROM meetings WHERE roomId = :roomId " +
            "AND meetingDate = :date " +
            "AND (:start < endTime AND :end > startTime)")
    int checkRoomAvailability(int roomId, String date, String start, String end);

    // 2. Kiểm tra trùng lịch khi CHỈNH SỬA
    // Sửa cột so sánh thành 'roomId', tham số 'rId' (int) và loại trừ chính nó bằng 'currentId'
    @Query("SELECT COUNT(*) FROM meetings WHERE roomId = :rId " +
            "AND meetingDate = :d " +
            "AND id != :currentId " +
            "AND (:s < endTime AND :e > startTime)") // Dùng công thức tối ưu hơn cho dễ đọc
    int checkRoomAvailabilityForEdit(int rId, String d, String s, String e, int currentId);
    @Query("SELECT COUNT(*) FROM meetings")
    int getTotalMeetings();

    @Query("SELECT COUNT(*) FROM meeting_rooms")
    int getTotalRooms();

    @Query("SELECT COUNT(*) FROM users")
    int getTotalUsers();

    // Thống kê số cuộc họp theo từng phòng (trả về danh sách đối tượng tùy biến)
    @Query("SELECT roomName as label, COUNT(*) as value FROM meetings GROUP BY roomId")
    List<StatItem> getMeetingsPerRoom();

}