package vn.edu.stu.com.example.da_meetingroom.model;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.RoomDatabase;

import java.io.Serializable;

import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;

@Entity(tableName = "rooms") // Tên bảng là "rooms"
public class Room implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int maPhong;

    public String tenPhong;
    public int sucChua;
    public String trangThai;

    public Room() {}

    @Ignore
    public Room(String tenPhong, int sucChua, String trangThai) {
        this.tenPhong = tenPhong;
        this.sucChua = sucChua;
        this.trangThai = trangThai;
    }
    @Override
    public String toString() {
        // Đây là những gì người dùng sẽ thấy trong ô chọn (Spinner)
        return tenPhong + " (" + sucChua + " chỗ)";
    }



}