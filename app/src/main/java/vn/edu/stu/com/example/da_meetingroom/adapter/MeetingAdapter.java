package vn.edu.stu.com.example.da_meetingroom.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.stu.com.example.da_meetingroom.AddMeetingActivity;
import vn.edu.stu.com.example.da_meetingroom.MeetingDetailDialog;
import vn.edu.stu.com.example.da_meetingroom.R;
import vn.edu.stu.com.example.da_meetingroom.database.AppDatabase;
import vn.edu.stu.com.example.da_meetingroom.model.Meeting;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {
    private List<Meeting> meetingList;
    private String userRole;
    private Context context;
    private AppDatabase db;

    public MeetingAdapter(Context context, List<Meeting> meetingList, String userRole) {
        this.context = context;
        this.meetingList = meetingList;
        this.userRole = userRole;
        this.db = AppDatabase.getInstance(context);
    }

    public void updateList(List<Meeting> newList) {
        this.meetingList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng item_meeting mới thiết kế
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);

        // 1. Đổ dữ liệu vào giao diện mới
        holder.txtTitle.setText(meeting.title);
        holder.txtDate.setText("📅 " + meeting.meetingDate);
        holder.txtRoom.setText("📍 " + meeting.roomName);

        // Hiển thị thời gian bắt đầu và kết thúc riêng biệt
        holder.txtStartTime.setText(meeting.startTime);
        holder.txtEndTime.setText(meeting.endTime);

        // Lấy userId hiện tại từ Session
        int currentUserId = context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE).getInt("userId", -1);

        // 2. Phân quyền: Chỉ hiện nút Sửa/Xóa nếu là người tạo (Leader) hoặc Admin
        if (meeting.leaderId == currentUserId || "admin".equals(userRole)) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnDelete.setVisibility(View.GONE);
        }

        // 3. Xử lý sự kiện Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (isPastMeeting(meeting.meetingDate)) {
                Toast.makeText(context, "Không thể xóa cuộc họp đã qua!", Toast.LENGTH_SHORT).show();
                return;
            }
            showConfirmDeleteDialog(meeting, position);
        });

        // 4. Xử lý sự kiện Sửa
        holder.btnEdit.setOnClickListener(v -> {
            if (isPastMeeting(meeting.meetingDate)) {
                Toast.makeText(context, "Không thể sửa cuộc họp đã qua!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, AddMeetingActivity.class);
            intent.putExtra("EDIT_MODE", true);
            intent.putExtra("MEETING_ID", meeting.id);
            context.startActivity(intent);
        });
        holder.itemView.setOnClickListener(v -> {
            MeetingDetailDialog dialog = new MeetingDetailDialog(meeting);
            dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "MeetingDetail");
        });
    }

    private void showConfirmDeleteDialog(Meeting meeting, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa: " + meeting.title + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    new Thread(() -> {
                        // Lưu ý: Nhớ viết hàm xóa trong MeetingDao
                        db.meetingDao().delete(meeting);

                        // Cần xóa cả bản ghi trong bảng attendees liên quan (nếu không dùng ForeignKey CASCADE)
                        db.attendeeDao().deleteByMeetingId(meeting.id);

                        ((AppCompatActivity)context).runOnUiThread(() -> {
                            meetingList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, meetingList.size());
                            Toast.makeText(context, "Đã xóa lịch họp", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean isPastMeeting(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date meetingDate = sdf.parse(dateStr);
            Date today = new Date();
            // Chỉ tính là quá khứ nếu ngày trước ngày hôm nay (không tính giờ)
            return meetingDate.before(new Date(today.getTime() - (1000 * 60 * 60 * 24)));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDate, txtStartTime, txtEndTime, txtRoom;
        ImageButton btnEdit, btnDelete;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các ID từ file item_meeting.xml mới
            txtTitle = itemView.findViewById(R.id.txtMeetingTitle);
            txtDate = itemView.findViewById(R.id.txtMeetingDate);
            txtStartTime = itemView.findViewById(R.id.txtStartTime);
            txtEndTime = itemView.findViewById(R.id.txtEndTime);
            txtRoom = itemView.findViewById(R.id.txtRoomName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}