package vn.edu.stu.com.example.da_meetingroom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.adapter.UserAdapter;
import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.User;

public class UserManagementActivity extends AppCompatActivity {
    AppDatabase db;
    UserAdapter adapter;
    RecyclerView rvUsers;
    List<User> listUsers;
    FloatingActionButton fabAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        db = AppDatabase.getInstance(this);
        rvUsers = findViewById(R.id.rvUsers);
        fabAddUser = findViewById(R.id.fabAddUser);

        checkUserRole();
        loadUserData();

        fabAddUser.setOnClickListener(v -> showUserDialog(null));
    }

    private void checkUserRole() {
        SharedPreferences pref = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        String userRole = pref.getString("userRole", "");
        fabAddUser.setVisibility("admin".equalsIgnoreCase(userRole) ? View.VISIBLE : View.GONE);
    }

    private void loadUserData() {
        new Thread(() -> {
            listUsers = db.userDao().getAllUsers();
            runOnUiThread(() -> {
                adapter = new UserAdapter(listUsers, new UserAdapter.OnUserActionListener() {
                    @Override
                    public void onEdit(User user) {
                        if (isAdmin()) showUserDialog(user);
                    }

                    @Override
                    public void onDelete(User user) {
                        if (isAdmin()) confirmDelete(user);
                    }
                });
                rvUsers.setLayoutManager(new LinearLayoutManager(this));
                rvUsers.setAdapter(adapter);
            });
        }).start();
    }

    private boolean isAdmin() {
        SharedPreferences pref = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        return "admin".equalsIgnoreCase(pref.getString("userRole", ""));
    }

    private void showUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(user == null ? "Thêm nhân sự mới" : "Sửa thông tin");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        // 1. Ô nhập Họ tên (fullName)
        final EditText edtName = new EditText(this);
        edtName.setHint("Họ và tên");
        if (user != null) edtName.setText(user.fullName);
        layout.addView(edtName);

        // 2. Ô nhập Tên đăng nhập (username) - CỰC KỲ QUAN TRỌNG ĐỂ ĐĂNG NHẬP
        final EditText edtUsername = new EditText(this);
        edtUsername.setHint("Tên đăng nhập (Username)");
        if (user != null) edtUsername.setText(user.username);
        layout.addView(edtUsername);

        // 3. Ô nhập Mật khẩu (password) - ĐỂ USER BIẾT ĐƯỜNG ĐĂNG NHẬP
        final EditText edtPassword = new EditText(this);
        edtPassword.setHint("Mật khẩu");
        if (user == null) {
            edtPassword.setText("123"); // Mặc định là 123 cho nhanh
        } else {
            edtPassword.setText(user.password);
        }
        layout.addView(edtPassword);

        // 4. Spinner chọn Quyền
        final Spinner spnRole = new Spinner(this);
        String[] displayRoles = {"Nhân viên", "Trưởng nhóm (Leader)"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayRoles);
        spnRole.setAdapter(roleAdapter);

        // Nếu là sửa, hiển thị đúng quyền hiện tại
        if (user != null) {
            spnRole.setSelection("leader".equalsIgnoreCase(user.role) ? 1 : 0);
        }
        layout.addView(spnRole);

        builder.setView(layout);

        builder.setPositiveButton("LƯU VÀO HỆ THỐNG", (d, w) -> {
            String name = edtName.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // CHUYỂN ĐỔI: Nếu chọn "Trưởng nhóm" thì lưu là "leader", còn lại là "user"
            String roleToSave = (spnRole.getSelectedItemPosition() == 1) ? "leader" : "user";

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ Tên, Username và Pass!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                if (user == null) {
                    // KIỂM TRA TRÙNG: Nếu username đã có rồi thì báo lỗi
                    if (db.userDao().getUserByUsername(username) != null) {
                        runOnUiThread(() -> Toast.makeText(this, "Username này đã có người dùng!", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // THÊM MỚI VÀO DATABASE
                    // Constructor: username, password, fullName, email, phone, role
                    User newUser = new User(username, password, name, username, "", roleToSave);
                    db.userDao().insert(newUser);

                    runOnUiThread(() -> Toast.makeText(this, "Đã tạo tài khoản cho " + name, Toast.LENGTH_SHORT).show());
                } else {
                    // CẬP NHẬT TÀI KHOẢN CŨ
                    user.fullName = name;
                    user.username = username;
                    user.password = password;
                    user.role = roleToSave;
                    db.userDao().update(user);

                    runOnUiThread(() -> Toast.makeText(this, "Đã cập nhật thành công!", Toast.LENGTH_SHORT).show());
                }
                // Load lại danh sách sau khi xong
                runOnUiThread(this::loadUserData);
            }).start();
        });
        builder.setNegativeButton("HỦY", null);
        builder.show();
    }

    private void confirmDelete(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Xóa tài khoản " + user.username + "?")
                .setPositiveButton("XÓA", (d, w) -> {
                    new Thread(() -> {
                        db.userDao().delete(user);
                        runOnUiThread(this::loadUserData);
                    }).start();
                }).setNegativeButton("HỦY", null).show();
    }
}