package vn.edu.stu.com.example.da_meetingroom;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.User;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText edtUsernameVerify, edtNewPassword, edtConfirmNewPassword;
    Button btnResetPassword;
    LinearLayout layoutReset;
    AppDatabase db;
    User targetUser; // Lưu thông tin người dùng tìm được

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = AppDatabase.getInstance(this);

        // Ánh xạ View
        edtUsernameVerify = findViewById(R.id.edtUsernameVerify);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmNewPassword = findViewById(R.id.edtConfirmNewPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        layoutReset = findViewById(R.id.layoutReset);

        btnResetPassword.setOnClickListener(v -> handleResetProcess());
    }

    private void handleResetProcess() {
        // Nếu khu vực nhập pass mới đang ẩn -> Đang ở Bước 1: Xác minh tài khoản
        if (layoutReset.getVisibility() == View.GONE) {
            verifyUsername();
        }
        // Nếu khu vực nhập pass mới đang hiện -> Đang ở Bước 2: Đổi mật khẩu
        else {
            updateNewPassword();
        }
    }

    private void verifyUsername() {
        String username = edtUsernameVerify.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            targetUser = db.userDao().getUserByUsername(username);
            runOnUiThread(() -> {
                if (targetUser != null) {
                    // Tìm thấy User -> Hiện form đổi mật khẩu
                    layoutReset.setVisibility(View.VISIBLE);
                    edtUsernameVerify.setEnabled(false); // Khóa không cho sửa username nữa
                    btnResetPassword.setText("ĐỔI MẬT KHẨU");
                    Toast.makeText(this, "Xác minh thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Tài khoản không tồn tại trên hệ thống!", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateNewPassword() {
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmNewPassword.getText().toString().trim();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng không để trống mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            targetUser.password = newPass; // Gán mật khẩu mới
            db.userDao().update(targetUser); // Cập nhật vào DB
            runOnUiThread(() -> {
                Toast.makeText(this, "Đổi mật khẩu thành công! Hãy đăng nhập lại.", Toast.LENGTH_LONG).show();
                finish(); // Đóng màn hình, quay về Login
            });
        }).start();
    }
}