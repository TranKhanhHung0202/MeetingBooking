package vn.edu.stu.com.example.da_meetingroom.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import vn.edu.stu.com.example.da_meetingroom.model.Attendee;

@Dao
public interface AttendeeDao {
    @Insert
    void insert(Attendee attendee);

    @Query("DELETE FROM attendees WHERE meetingId = :mId")
    void deleteByMeetingId(int mId);

    @Query("SELECT userId FROM attendees WHERE meetingId = :mId")
    List<Integer> getUserIdsByMeetingId(int mId);
}
// Đảm bảo KHÔNG có thêm dấu } nào ở đây nữa