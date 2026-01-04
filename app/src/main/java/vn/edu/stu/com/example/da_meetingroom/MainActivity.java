package vn.edu.stu.com.example.da_meetingroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.adapter.MeetingAdapter;
import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.Meeting;
import vn.edu.stu.com.example.da_meetingroom.model.User;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton btnAddMeeting;
    Button btnManageRooms;
    MaterialButton btnManageUsers, btnStats;
    RecyclerView rvMeetings;
    MeetingAdapter adapter;
    List<Meeting> meetingList = new ArrayList<>();
    AppDatabase db;
    String currentRole;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getInstance(this);

        // --- BƯỚC 1: ĐỌC SESSION CẨN THẬN ---
        SharedPreferences pref = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        // Kiểm tra cả 2 key phòng trường hợp LoginActivity lưu key khác
        currentRole = pref.getString("userRole", pref.getString("role", "user"));
        currentUserId = pref.getInt("userId", -1);

        initViews();
        setupRecyclerView();

        // --- BƯỚC 2: PHÂN QUYỀN ---
        applyPermissions(currentRole);

        // Sự kiện click
        btnManageRooms.setOnClickListener(v -> startActivity(new Intent(this, RoomManagementActivity.class)));
        btnManageUsers.setOnClickListener(v -> startActivity(new Intent(this, UserManagementActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        btnAddMeeting.setOnClickListener(v -> startActivity(new Intent(this, AddMeetingActivity.class)));

        seedUserData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnAddMeeting = findViewById(R.id.btnAddMeeting);
        btnManageRooms = findViewById(R.id.btnManageRooms);
        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnStats = findViewById(R.id.btnStats);
        rvMeetings = findViewById(R.id.rvMeetings);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void applyPermissions(String role) {
        if (role == null || role.isEmpty()) {
            role = "user";
        }

        // Chuẩn hóa role
        String r = role.toLowerCase().trim();

        // LOG VÀ TOAST ĐỂ DEBUG (Bạn hãy nhìn cái này khi chạy app)
        Log.d("PERMISSION_CHECK", "Role đang đọc được: [" + r + "]");
        // Toast.makeText(this, "Quyền hiện tại: " + r, Toast.LENGTH_SHORT).show();

        boolean isAdmin = r.equals("admin");
        boolean isLeader = r.equals("leader");

        // Hiển thị nút + cho Admin hoặc Leader
        if (isAdmin || isLeader) {
            btnAddMeeting.setVisibility(View.VISIBLE);
            btnAddMeeting.bringToFront();
        } else {
            btnAddMeeting.setVisibility(View.GONE);
        }

        // Các nút quản lý chỉ dành cho Admin
        btnManageRooms.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        btnManageUsers.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        btnStats.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupRecyclerView() {
        adapter = new MeetingAdapter(this, meetingList, currentRole);
        rvMeetings.setLayoutManager(new LinearLayoutManager(this));
        rvMeetings.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Khi quay lại màn hình, kiểm tra lại quyền và load lại lịch
        SharedPreferences pref = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        currentRole = pref.getString("userRole", pref.getString("role", "user"));
        applyPermissions(currentRole);
        loadMeetings();
    }

    private void loadMeetings() {
        new Thread(() -> {
            List<Meeting> list;
            if ("admin".equalsIgnoreCase(currentRole)) {
                list = db.meetingDao().getAllMeetings();
            } else {
                list = db.meetingDao().getMeetingsForUser(currentUserId);
            }

            runOnUiThread(() -> {
                if (adapter != null) {
                    adapter.updateList(list);
                }
            });
        }).start();
    }

    private void performSearch(String query) {
        new Thread(() -> {
            List<Meeting> searchResults;
            String searchQuery = "%" + query + "%";

            if ("admin".equalsIgnoreCase(currentRole)) {
                searchResults = db.meetingDao().searchMeetings(searchQuery);
            } else {
                searchResults = db.meetingDao().searchMeetingsForUser(searchQuery, currentUserId);
            }

            runOnUiThread(() -> {
                if (adapter != null) adapter.updateList(searchResults);
            });
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Tìm tên cuộc họp...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    performSearch(query);
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String newText) {
                    performSearch(newText);
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void logout() {
        getSharedPreferences("USER_SESSION", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void seedUserData() {
        new Thread(() -> {
            // Đảm bảo có admin
            if (db.userDao().getUserByUsername("admin") == null) {
                db.userDao().insert(new User("admin", "123", "admin"));
            }

            // Đảm bảo có leader
            User existingLeader = db.userDao().getUserByUsername("leader");
            if (existingLeader == null) {
                db.userDao().insert(new User("leader", "123", "leader"));
            } else if (!existingLeader.role.equals("leader")) {
                // Nếu đã có user leader nhưng role sai thì cập nhật lại
                existingLeader.role = "leader";
                db.userDao().update(existingLeader);
            }

            // Tạo nhân viên mẫu
            for (int i = 1; i <= 3; i++) {
                String username = "nhanvien_" + i;
                if (db.userDao().getUserByUsername(username) == null) {
                    db.userDao().insert(new User(username, "123", "user"));
                }
            }
        }).start();
    }
}