package com.example.campusfind.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.campusfind.R;
import com.example.campusfind.databinding.ItemUserCardBinding;
import com.example.campusfind.models.User;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private final List<User> userList;
    private final OnUserDeleteListener deleteListener;

    public interface OnUserDeleteListener {
        void onUserDelete(User user);
    }

    public AdminUserAdapter(List<User> userList, OnUserDeleteListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserCardBinding binding = ItemUserCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.binding.tvUserName.setText(user.getName());
        holder.binding.tvUserEmail.setText(user.getEmail());

        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.binding.ivUserImage);
        } else {
            holder.binding.ivUserImage.setImageResource(R.drawable.ic_profile);
        }

        holder.binding.btnDeleteUser.setOnClickListener(v -> deleteListener.onUserDelete(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        final ItemUserCardBinding binding;

        public UserViewHolder(ItemUserCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
