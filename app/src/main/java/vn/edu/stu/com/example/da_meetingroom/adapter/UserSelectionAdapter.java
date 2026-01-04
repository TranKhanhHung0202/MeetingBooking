package vn.edu.stu.com.example.da_meetingroom.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.edu.stu.com.example.da_meetingroom.model.User;

public class UserSelectionAdapter extends RecyclerView.Adapter<UserSelectionAdapter.UserViewHolder> {
    private List<User> userList;

    public UserSelectionAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout mặc định của Android cho chọn nhiều mục
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        CheckedTextView checkedTextView = (CheckedTextView) holder.itemView;

        // 1. Hiển thị tên (Đã sửa từ username thành fullName)
        checkedTextView.setText(user.fullName);

        // 2. Cập nhật trạng thái check
        checkedTextView.setChecked(user.isChecked);

        // 3. Xử lý khi click vào dòng
        holder.itemView.setOnClickListener(v -> {
            user.isChecked = !user.isChecked;
            checkedTextView.setChecked(user.isChecked);
        });
    }

    // Hàm lấy danh sách ID đã chọn để lưu vào Database
    public List<Integer> getSelectedUserIds() {
        List<Integer> selectedIds = new ArrayList<>();
        for (User user : userList) {
            if (user.isChecked) {
                selectedIds.add(user.id);
            }
        }
        return selectedIds;
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // Hàm này giúp tích lại các checkbox khi mở màn hình Sửa
    public void updateCheckedUsers(List<Integer> alreadySelectedIds) {
        if (alreadySelectedIds == null) return;

        for (User user : userList) {
            // Nếu ID của user nằm trong danh sách đã đi họp, đánh dấu là true
            user.isChecked = alreadySelectedIds.contains(user.id);
        }
        notifyDataSetChanged();
    }
}