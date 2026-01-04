package vn.edu.stu.com.example.da_meetingroom;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.User;

public class ProfileActivity extends AppCompatActivity {
    ImageView imgAvatar; // Thêm ImageView cho Avatar
    TextView tvHeaderName, tvEmployeeCode, tvRole, tvEmail;
    Button btnLogout, btnChangePassword, btnEditEmail;
    AppDatabase db;
    int userId;

    // 1. Khai báo bộ chọn ảnh từ thư viện
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        saveAvatarToDatabase(selectedImageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = AppDatabase.getInstance(this);
        userId = getSharedPreferences("USER_SESSION", MODE_PRIVATE).getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "Lỗi phiên đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadUserProfile();

        // 2. Sự kiện click vào ảnh để đổi Avatar
        imgAvatar.setOnClickListener(v -> checkPermissionAndPickImage());

        btnLogout.setOnClickListener(v -> performLogout());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnEditEmail.setOnClickListener(v -> showEditEmailDialog());
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.imgAvatar); // Ánh xạ ImageView
        tvHeaderName = findViewById(R.id.tvProfileHeaderName);
        tvEmployeeCode = findViewById(R.id.tvProfileEmployeeCode);
        tvRole = findViewById(R.id.tvProfileRole);
        tvEmail = findViewById(R.id.tvProfileEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnEditEmail = findViewById(R.id.btnEditEmail);
    }

    private void loadUserProfile() {
        new Thread(() -> {
            User user = db.userDao().getUserById(userId);
            if (user != null) {
                runOnUiThread(() -> {
                    tvHeaderName.setText(user.username.toUpperCase());
                    tvEmployeeCode.setText(user.username);
                    tvRole.setText(user.role.equals("admin") ? "Quản trị viên" : "Nhân viên");

                    String emailStr = (user.getEmail() != null && !user.getEmail().isEmpty())
                            ? user.getEmail() : "Chưa cập nhật";
                    tvEmail.setText(emailStr);

                    // 3. Load ảnh đại diện nếu có
                    if (user.getAvatarUri() != null) {
                        Glide.with(this)
                                .load(Uri.parse(user.getAvatarUri()))
                                .circleCrop() // Bo tròn ảnh
                                .placeholder(android.R.drawable.ic_menu_report_image)
                                .into(imgAvatar);
                    }
                });
            }
        }).start();
    }

    // 4. Kiểm tra quyền và mở thư viện ảnh
    private void checkPermissionAndPickImage() {
        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissions(new String[]{permission}, 101);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    // 5. Lưu đường dẫn ảnh vào Database
    private void saveAvatarToDatabase(Uri imageUri) {
        // Xin quyền truy cập URI lâu dài
        try {
            getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            User user = db.userDao().getUserById(userId);
            if (user != null) {
                user.setAvatarUri(imageUri.toString());
                db.userDao().update(user);
                runOnUiThread(() -> {
                    Glide.with(this).load(imageUri).circleCrop().into(imgAvatar);
                    Toast.makeText(this, "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");
        View v = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(v);

        EditText edtOld = v.findViewById(R.id.edtOldPassword);
        EditText edtNew = v.findViewById(R.id.edtNewPassword);
        EditText edtConfirm = v.findViewById(R.id.edtConfirmPassword);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String oldP = edtOld.getText().toString().trim();
            String newP = edtNew.getText().toString().trim();
            String confP = edtConfirm.getText().toString().trim();

            if (newP.isEmpty() || !newP.equals(confP)) {
                Toast.makeText(this, "Mật khẩu mới không khớp hoặc trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = db.userDao().getUserById(userId);
                if (user != null && user.password.equals(oldP)) {
                    user.password = newP;
                    db.userDao().update(user);
                    runOnUiThread(() -> Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Mật khẩu cũ không chính xác!", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });
        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    private void showEditEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập nhật Email");

        final EditText input = new EditText(this);
        input.setHint("Nhập địa chỉ email mới...");

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 20, 50, 0);
        input.setLayoutParams(lp);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String mail = input.getText().toString().trim();
            if (mail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = db.userDao().getUserById(userId);
                if (user != null) {
                    user.setEmail(mail);
                    db.userDao().update(user);
                    runOnUiThread(() -> {
                        tvEmail.setText(mail);
                        Toast.makeText(this, "Đã cập nhật Email!", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    private void performLogout() {
        getSharedPreferences("USER_SESSION", MODE_PRIVATE).edit().clear().apply();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
    }
}