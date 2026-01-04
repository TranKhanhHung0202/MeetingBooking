package vn.edu.stu.com.example.da_meetingroom.dao;

import androidx.room.*;
import java.util.List;
import vn.edu.stu.com.example.da_meetingroom.model.User;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM users WHERE role != 'admin'")
    List<User> getAllNormalUsers();

    @Query("SELECT * FROM users WHERE username = :user AND password = :pass LIMIT 1")
    User login(String user, String pass);

    // THÊM HÀM NÀY: Đếm tổng số user
    @Query("SELECT COUNT(*) FROM users")
    int getTotalUsers();

    // THÊM HÀM NÀY: Lấy tên những người tham gia một cuộc họp cụ thể
    @Query("SELECT users.fullName FROM users " +
            "INNER JOIN attendees ON users.id = attendees.userId " +
            "WHERE attendees.meetingId = :meetingId")
    List<String> getAttendeeNamesByMeetingId(int meetingId);

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}