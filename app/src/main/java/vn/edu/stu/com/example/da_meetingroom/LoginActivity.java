package vn.edu.stu.com.example.da_meetingroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.User;

public class LoginActivity extends AppCompatActivity {
    EditText edtUser, edtPass;
    Button btnLogin;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Ánh xạ các thành phần UI
        CardView loginCard = findViewById(R.id.loginCard);
        LinearLayout headerSection = findViewById(R.id.headerSection);

        // 2. Load hiệu ứng
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1200);

        headerSection.startAnimation(fadeIn);
        loginCard.startAnimation(slideUp);

        db = AppDatabase.getInstance(this);
        edtUser = findViewById(R.id.edtUsername);
        edtPass = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String usernameInput = edtUser.getText().toString().trim();
            String passwordInput = edtPass.getText().toString().trim();

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ!", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                User user = db.userDao().login(usernameInput, passwordInput);

                runOnUiThread(() -> {
                    if (user != null) {
                        // --- PHẦN CẬP NHẬT: LƯU SESSION ---
                        SharedPreferences pref = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();

                        // Lưu đầy đủ các thông tin cần thiết
                        editor.putInt("userId", user.id);
                        editor.putString("username", user.username);
                        editor.putString("role", user.role);      // Key cũ nếu bạn đang dùng
                        editor.putString("userRole", user.role);  // Key MỚI để UserManagementActivity nhận diện Admin

                        editor.apply();
                        // ----------------------------------

                        Toast.makeText(this, "Chào mừng " + user.username, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Tài khoản hoặc mật khẩu sai!", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        TextView tvForgot = findViewById(R.id.tvForgotPassword);
        tvForgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
}