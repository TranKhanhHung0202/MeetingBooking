package vn.edu.stu.com.example.da_meetingroom;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;
import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.RoomStatistic; // Đổi sang Model mới

public class StatisticsActivity extends AppCompatActivity {
    TextView tvTotalMeetings, tvTotalRooms, tvTotalUsers, tvDetails;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống kê báo cáo");
        }

        db = AppDatabase.getInstance(this);
        tvTotalMeetings = findViewById(R.id.tvTotalMeetings);
        tvTotalRooms = findViewById(R.id.tvTotalRooms);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvDetails = findViewById(R.id.tvDetails);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // 1. Lấy các con số tổng quát
            int mCount = db.meetingDao().getTotalMeetings();
            // Đảm bảo dùng đúng roomDao và hàm lấy danh sách phòng
            int rCount = db.roomDao().getAllRooms().size();
            int uCount = db.userDao().getAllNormalUsers().size();

            // 2. Lấy thống kê chi tiết từng phòng (Dùng LEFT JOIN từ RoomDao)
            // Đây là phần quan trọng nhất để hiện phòng có 0 cuộc họp
            List<RoomStatistic> stats = db.roomDao().getRoomStatistics();

            StringBuilder sb = new StringBuilder();
            sb.append("CHI TIẾT THEO PHÒNG:\n\n");

            for (RoomStatistic item : stats) {
                sb.append("• ").append(item.roomName) // Tên phòng
                        .append(": ").append(item.meetingCount) // Số cuộc họp (sẽ có 0 nếu là phòng mới)
                        .append(" cuộc họp\n");
            }

            runOnUiThread(() -> {
                tvTotalMeetings.setText("Tổng cuộc họp: " + mCount);
                tvTotalRooms.setText("Tổng số phòng: " + rCount);
                tvTotalUsers.setText("Tổng nhân viên: " + uCount);
                tvDetails.setText(sb.toString());
            });
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}