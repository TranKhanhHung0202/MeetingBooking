package vn.edu.stu.com.example.da_meetingroom.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.R;
import vn.edu.stu.com.example.da_meetingroom.model.MeetingRoom;
import vn.edu.stu.com.example.da_meetingroom.model.Room;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private List<Room> roomList;
    private OnRoomActionListener listener;

    public interface OnRoomActionListener {
        void onEdit(Room room);
        void onDelete(Room room);
    }

    public RoomAdapter(List<Room> roomList, OnRoomActionListener listener) {
        this.roomList = roomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.txtName.setText(room.tenPhong);
        holder.txtCapacity.setText("Sức chứa: " + room.sucChua);
        holder.txtStatus.setText("Trạng thái: " + room.trangThai);

        // Đổi màu trạng thái trực quan
        if (room.trangThai.equals("Đang trống")) {
            holder.txtStatus.setTextColor(Color.GREEN);
        } else {
            holder.txtStatus.setTextColor(Color.RED);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(room));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(room));
    }

    @Override
    public int getItemCount() { return roomList.size(); }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtCapacity, txtStatus;
        ImageButton btnEdit, btnDelete;

        RoomViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtRoomName);
            txtCapacity = itemView.findViewById(R.id.txtCapacity);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            btnEdit = itemView.findViewById(R.id.btnEditRoom);
            btnDelete = itemView.findViewById(R.id.btnDeleteRoom);
        }
    }
}