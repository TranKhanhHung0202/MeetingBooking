package vn.edu.stu.com.example.da_meetingroom.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import vn.edu.stu.com.example.da_meetingroom.model.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

import vn.edu.stu.com.example.da_meetingroom.dao.AttendeeDao;
import vn.edu.stu.com.example.da_meetingroom.dao.MeetingDao;
import vn.edu.stu.com.example.da_meetingroom.dao.MeetingRoomDao;
import vn.edu.stu.com.example.da_meetingroom.dao.RoomDao;
import vn.edu.stu.com.example.da_meetingroom.dao.UserDao;
import vn.edu.stu.com.example.da_meetingroom.model.*;

@Database(entities =
        {
                User.class,
                MeetingRoom.class,
                Meeting.class,
                Attendee.class,
                Room.class},
        version = 8,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract MeetingRoomDao meetingRoomDao();
    public abstract MeetingDao meetingDao();
    public abstract AttendeeDao attendeeDao();
    public abstract RoomDao roomDao();

    private static volatile AppDatabase instance; // Thêm volatile để đảm bảo an toàn đa luồng

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    // Thay vì viết Room.databaseBuilder, hãy viết đầy đủ như dưới đây:
                    instance = androidx.room.Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class, "meeting_room_db")
                            .fallbackToDestructiveMigration()
                            .addCallback(roomCallback)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }

    // Tự động tạo tài khoản khi chạy ứng dụng lần đầu
    // Tìm đến phần roomCallback trong AppDatabase.java và sửa lại như sau:
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Executors.newSingleThreadExecutor().execute(() -> {
                UserDao dao = instance.userDao();

                // TRUYỀN ĐỦ 6 THAM SỐ: username, password, fullName, email, phone, role
                dao.insert(new User("admin", "123", "Quản trị viên", "admin@gmail.com", "0123456789", "admin"));
                dao.insert(new User("leader1", "123", "leader", "leader1@gmail.com", "0987654321", "leader"));
                dao.insert(new User("user1", "123", "Nhân viên 1", "user1@gmail.com", "0111222333", "user"));
            });
        }
    };
}