package vn.edu.stu.com.example.da_meetingroom;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.stu.com.example.da_meetingroom.adapter.UserSelectionAdapter;
import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.Attendee;
import vn.edu.stu.com.example.da_meetingroom.model.Meeting;
import vn.edu.stu.com.example.da_meetingroom.model.Room; // Đã đổi từ MeetingRoom sang Room
import vn.edu.stu.com.example.da_meetingroom.model.User;

public class AddMeetingActivity extends AppCompatActivity {
    AppDatabase db;
    UserSelectionAdapter userAdapter;
    EditText edtTitle, edtDate, edtStartTime, edtEndTime;
    Spinner spnRooms;
    RecyclerView rvUserSelection;
    Button btnSaveMeeting;

    private int startHour = -1, startMinute = -1;
    private long selectedDateTimestamp = -1;

    private boolean isEditMode = false;
    private int editingMeetingId = -1;
    private Meeting editingMeeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meeting);

        db = AppDatabase.getInstance(this);
        initViews();

        // 1. Setup chọn ngày
        edtDate.setOnClickListener(v -> showMaterialDatePicker());

        // 2. Setup chọn giờ
        setupTimePickers();

        // 3. Load dữ liệu lên giao diện
        loadUserSelection();
        loadRoomSpinner(); // Hàm quan trọng nhất bạn đang cần

        // 4. Sự kiện lưu
        btnSaveMeeting.setOnClickListener(v -> saveMeeting());
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtDate = findViewById(R.id.edtDate);
        edtStartTime = findViewById(R.id.edtStartTime);
        edtEndTime = findViewById(R.id.edtEndTime);
        spnRooms = findViewById(R.id.spnRooms);
        rvUserSelection = findViewById(R.id.rvUserSelection);
        btnSaveMeeting = findViewById(R.id.btnSaveMeeting);

        edtDate.setFocusable(false);
        edtStartTime.setFocusable(false);
        edtEndTime.setFocusable(false);
    }

    private void loadRoomSpinner() {
        new Thread(() -> {
            // Lấy danh sách phòng có trạng thái 'Sẵn sàng'
            List<Room> rooms = db.roomDao().getRoomsAvailable();

            runOnUiThread(() -> {
                if (rooms != null && !rooms.isEmpty()) {
                    // Đổ List<Room> vào Spinner
                    ArrayAdapter<Room> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, rooms);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnRooms.setAdapter(adapter);

                    // Kiểm tra chế độ sửa
                    isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
                    if (isEditMode) {
                        editingMeetingId = getIntent().getIntExtra("MEETING_ID", -1);
                        prepareEditMode();
                    }
                } else {
                    Toast.makeText(this, "Không có phòng nào khả dụng!", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void saveMeeting() {
        String title = edtTitle.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String start = edtStartTime.getText().toString().trim();
        String end = edtEndTime.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy phòng đang chọn từ Spinner
        Room selectedRoom = (Room) spnRooms.getSelectedItem();
        if (selectedRoom == null) {
            Toast.makeText(this, "Chưa chọn phòng họp!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            // Kiểm tra trùng lịch dựa trên maPhong (ID của Room)
            int overlap = isEditMode ?
                    db.meetingDao().checkRoomAvailabilityForEdit(selectedRoom.maPhong, date, start, end, editingMeetingId) :
                    db.meetingDao().checkRoomAvailability(selectedRoom.maPhong, date, start, end);

            if (overlap > 0) {
                runOnUiThread(() -> Toast.makeText(this, "Phòng này đã có lịch vào khung giờ trên!", Toast.LENGTH_LONG).show());
                return;
            }

            int currentLeaderId = getSharedPreferences("USER_SESSION", MODE_PRIVATE).getInt("userId", -1);

            if (isEditMode) {
                editingMeeting.title = title;
                editingMeeting.meetingDate = date;
                editingMeeting.startTime = start;
                editingMeeting.endTime = end;
                editingMeeting.roomId = selectedRoom.maPhong;
                editingMeeting.roomName = selectedRoom.tenPhong;
                db.meetingDao().update(editingMeeting);
                db.attendeeDao().deleteByMeetingId(editingMeetingId);
                saveAttendees(editingMeetingId);
            } else {
                Meeting newM = new Meeting(selectedRoom.maPhong, currentLeaderId, title, date, start, end);
                newM.roomName = selectedRoom.tenPhong;
                long newId = db.meetingDao().insert(newM);
                saveAttendees((int) newId);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Lưu lịch họp thành công!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void saveAttendees(int meetingId) {
        if (userAdapter == null) return;
        List<Integer> selectedIds = userAdapter.getSelectedUserIds();
        int currentLeaderId = getSharedPreferences("USER_SESSION", MODE_PRIVATE).getInt("userId", -1);

        // Luôn thêm người tạo
        if (currentLeaderId != -1) {
            db.attendeeDao().insert(new Attendee(meetingId, currentLeaderId));
        }

        // Thêm người tham gia được chọn
        if (selectedIds != null) {
            for (Integer uId : selectedIds) {
                if (uId != currentLeaderId) {
                    db.attendeeDao().insert(new Attendee(meetingId, uId));
                }
            }
        }
    }

    private void prepareEditMode() {
        btnSaveMeeting.setText("CẬP NHẬT THAY ĐỔI");
        new Thread(() -> {
            editingMeeting = db.meetingDao().getMeetingById(editingMeetingId);
            List<Integer> currentAttendeeIds = db.attendeeDao().getUserIdsByMeetingId(editingMeetingId);

            if (editingMeeting != null) {
                runOnUiThread(() -> {
                    edtTitle.setText(editingMeeting.title);
                    edtDate.setText(editingMeeting.meetingDate);
                    edtStartTime.setText(editingMeeting.startTime);
                    edtEndTime.setText(editingMeeting.endTime);

                    // Logic chọn đúng phòng cũ trên Spinner
                    for (int i = 0; i < spnRooms.getCount(); i++) {
                        Room r = (Room) spnRooms.getItemAtPosition(i);
                        if (r.maPhong == editingMeeting.roomId) {
                            spnRooms.setSelection(i);
                            break;
                        }
                    }
                    if (userAdapter != null) userAdapter.updateCheckedUsers(currentAttendeeIds);
                });
            }
        }).start();
    }

    private void loadUserSelection() {
        new Thread(() -> {
            List<User> listUsers = db.userDao().getAllNormalUsers();
            runOnUiThread(() -> {
                userAdapter = new UserSelectionAdapter(listUsers);
                rvUserSelection.setLayoutManager(new LinearLayoutManager(this));
                rvUserSelection.setAdapter(userAdapter);
            });
        }).start();
    }

    public void showMaterialDatePicker() {
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setValidator(DateValidatorPointForward.now());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày họp")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDateTimestamp = selection;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            edtDate.setText(sdf.format(new Date(selection)));
        });
        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void setupTimePickers() {
        edtStartTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) -> {
                startHour = h;
                edtStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                edtEndTime.setText("");
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        edtEndTime.setOnClickListener(v -> {
            if (startHour == -1) {
                Toast.makeText(this, "Chọn giờ bắt đầu trước!", Toast.LENGTH_SHORT).show();
                return;
            }
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, h, m) -> {
                edtEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
            }, startHour + 1, 0, true).show();
        });
    }
}