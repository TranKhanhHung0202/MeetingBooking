package vn.edu.stu.com.example.da_meetingroom;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.adapter.RoomAdapter;
import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.MeetingRoom;
import vn.edu.stu.com.example.da_meetingroom.model.Room;

public class RoomManagementActivity extends AppCompatActivity {
    AppDatabase db;
    RoomAdapter adapter;
    RecyclerView rvRooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_management);

        db = AppDatabase.getInstance(this);
        rvRooms = findViewById(R.id.rvRooms);
        loadRoomData();

        findViewById(R.id.fabAddRoom).setOnClickListener(v -> showRoomDialog(null));
    }

    private void loadRoomData() {
        new Thread(() -> {
            List<Room> list = db.roomDao().getAllRooms();
            runOnUiThread(() -> {
                adapter = new RoomAdapter(list, new RoomAdapter.OnRoomActionListener() {
                    @Override public void onEdit(Room room) { showRoomDialog(room); }
                    @Override public void onDelete(Room room) { deleteRoom(room); }
                });
                rvRooms.setLayoutManager(new LinearLayoutManager(this));
                rvRooms.setAdapter(adapter);
            });
        }).start();
    }

    private void showRoomDialog(Room room) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(room == null ? "Thêm phòng" : "Sửa phòng");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        final EditText edtName = new EditText(this);
        edtName.setHint("Tên phòng");
        if (room != null) edtName.setText(room.tenPhong);
        layout.addView(edtName);
        final EditText edtCap = new EditText(this);
        edtCap.setHint("Sức chứa");
        edtCap.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (room != null) edtCap.setText(String.valueOf(room.sucChua));
        layout.addView(edtCap);
        builder.setView(layout);
        builder.setPositiveButton("Lưu", (d, w) -> {
            String name = edtName.getText().toString();
            int cap = Integer.parseInt(edtCap.getText().toString());
            new Thread(() -> {
                if (room == null) db.roomDao().insert(new Room(name, cap, "Đang trống"));
                else {
                    room.tenPhong = name;
                    room.sucChua = cap;
                    db.roomDao().update(room);
                }
                runOnUiThread(this::loadRoomData);
            }).start();
        });
        builder.show();
    }

    private void deleteRoom(Room room) {
        new AlertDialog.Builder(this)
                .setMessage("Bạn có chắc chắn muốn xóa phòng này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    new Thread(() -> {
                        db.roomDao().delete(room);
                        runOnUiThread(this::loadRoomData);
                    }).start();
                }).show();
    }
}