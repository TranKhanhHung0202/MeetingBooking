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
import vn.edu.stu.com.example.da_meetingroom.model.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onEdit(User user);
        void onDelete(User user);
    }

    public UserAdapter(List<User> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.txtName.setText(user.fullName);
        holder.txtEmail.setText(user.email);
        holder.txtRole.setText(user.role);

        // Đổi màu hiển thị dựa trên chức vụ
        if (user.role.equals("Trưởng nhóm")) {
            holder.viewColor.setBackgroundColor(Color.RED);
            holder.txtRole.setTextColor(Color.RED);
        } else {
            holder.viewColor.setBackgroundColor(Color.BLUE);
            holder.txtRole.setTextColor(Color.BLUE);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }

    @Override
    public int getItemCount() { return userList.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtEmail, txtRole;
        View viewColor;
        ImageButton btnEdit, btnDelete;

        UserViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtUserName);
            txtEmail = itemView.findViewById(R.id.txtUserEmail);
            txtRole = itemView.findViewById(R.id.txtUserRole);
            viewColor = itemView.findViewById(R.id.viewRoleColor);
            btnEdit = itemView.findViewById(R.id.btnEditUser);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}