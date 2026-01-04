package vn.edu.stu.com.example.da_meetingroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.Meeting;

public class MeetingDetailDialog extends BottomSheetDialogFragment {
    private Meeting meeting;
    private AppDatabase db;

    public MeetingDetailDialog(Meeting meeting) {
        this.meeting = meeting;
    }

    // --- BỔ SUNG QUAN TRỌNG: Để hiện bo góc ---
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Theme này giúp làm trong suốt phần nền mặc định của Dialog
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo file XML dialog_meeting_details đã dùng android:background="@drawable/bg_bottom_sheet"
        View view = inflater.inflate(R.layout.dialog_meeting_details, container, false);
        db = AppDatabase.getInstance(getContext());

        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvRoom = view.findViewById(R.id.tvDetailRoom);
        TextView tvDateTime = view.findViewById(R.id.tvDetailDateTime);
        TextView tvAttendees = view.findViewById(R.id.tvDetailAttendees);
        Button btnClose = view.findViewById(R.id.btnCloseDetail);

        tvTitle.setText(meeting.title);
        tvRoom.setText("📍 " + meeting.roomName);
        tvDateTime.setText("📅 " + meeting.meetingDate + "  |  " + meeting.startTime + " - " + meeting.endTime);

        // Lấy danh sách tên người tham gia từ Database
        new Thread(() -> {
            List<String> names = db.userDao().getAttendeeNamesByMeetingId(meeting.id);
            System.out.println("DEBUG: Số lượng người lấy được: " + names.size());

            // Xử lý chuỗi tên
            final String allNames;
            if (names == null || names.isEmpty()) {
                allNames = "Chưa có người tham gia";
            } else {
                // Sử dụng StringBuilder nếu String.join báo lỗi ở phiên bản Android cũ
                allNames = android.text.TextUtils.join(", ", names);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> tvAttendees.setText(allNames));
            }
        }).start();

        btnClose.setOnClickListener(v -> dismiss());
        return view;
    }
}