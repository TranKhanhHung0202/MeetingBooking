package vn.edu.stu.com.example.da_meetingroom.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.model.MeetingRoom;

@Dao
public interface MeetingRoomDao {

    @Query("SELECT * FROM meeting_rooms")
    List<MeetingRoom> getAll();

    @Insert
    void insert(MeetingRoom room);

    @Update
    void update(MeetingRoom room);

    @Delete
    void delete(MeetingRoom room);

    @Query("SELECT * FROM meeting_rooms WHERE id = :id LIMIT 1")
    MeetingRoom getById(int id);
}