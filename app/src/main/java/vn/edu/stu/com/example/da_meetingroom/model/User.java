package vn.edu.stu.com.example.da_meetingroom.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "users")
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String password;
    public String fullName;
    public String email;
    public String phone;
    public String role;
    public String avatarUri;

    @Ignore
    public boolean isChecked = false;

    // 1. Constructor mặc định (Bắt buộc cho Room)
    public User() {}

    // 2. Constructor đầy đủ 6 tham số (Dùng cho Đăng ký/Đăng nhập)
    @Ignore
    public User(String username, String password, String fullName, String email, String phone, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // 3. Constructor 4 tham số (Dùng cho UserManagementActivity)
    @Ignore
    public User(String fullName, String email, String phone, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.username = email; // Gán mặc định để tránh null
        this.password = "123456";
    }

    // 4. Constructor 3 tham số (Dành cho việc tạo nhanh tài khoản admin/user)
    @Ignore
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = username;
        this.email = username + "@example.com";
        this.phone = "0000000000";
    }

    // --- Getter và Setter đầy đủ để các Activity khác gọi không bị lỗi ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatarUri() { return avatarUri; }
    public void setAvatarUri(String avatarUri) { this.avatarUri = avatarUri; }
}