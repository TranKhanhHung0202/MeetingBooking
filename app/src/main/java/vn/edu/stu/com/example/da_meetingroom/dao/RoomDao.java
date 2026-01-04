package vn.edu.stu.com.example.da_meetingroom.dao;

import androidx.room.*;
import java.util.List;
import vn.edu.stu.com.example.da_meetingroom.model.Room;
import vn.edu.stu.com.example.da_meetingroom.model.RoomStatistic;

@Dao
public interface RoomDao {
    @Query("SELECT * FROM rooms")
    List<Room> getAllRooms();

    // THÊM ĐOẠN NÀY:
    @Query("SELECT rooms.tenPhong as roomName, COUNT(meetings.id) as meetingCount " +
            "FROM rooms " +
            "LEFT JOIN meetings ON rooms.maPhong = meetings.roomId " +
            "GROUP BY rooms.maPhong")
    List<RoomStatistic> getRoomStatistics();

    @Query("SELECT * FROM rooms WHERE trangThai = 'Đang trống'")
    List<Room> getRoomsAvailable();
    @Insert
    void insert(Room room);

    @Update
    void update(Room room);

    @Delete
    void delete(Room room);
}