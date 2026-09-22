package com.example.campusfind.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.campusfind.R;
import com.example.campusfind.databinding.ItemMyPostBinding;
import com.example.campusfind.models.Item;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.MyPostViewHolder> {

    private final List<Item> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
        void onDeleteClick(Item item);
        void onResolveClick(Item item);
        void onEditClick(Item item);
    }

    public MyPostsAdapter(List<Item> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMyPostBinding binding = ItemMyPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyPostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostViewHolder holder, int position) {
        Item item = items.get(position);
        ItemMyPostBinding binding = holder.binding;

        binding.tvItemTitle.setText(item.getTitle());
        binding.tvItemCategory.setText("Category: " + item.getCategory());
        
        // Format timestamp
        if (item.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            binding.tvTimePosted.setText(sdf.format(new Date(item.getTimestamp())));
        } else {
            binding.tvTimePosted.setText("Unknown time");
        }

        // Status badge display
        String status = item.getStatus() != null ? item.getStatus() : "Pending";
        String type = item.getType() != null ? item.getType() : "Post";

        if ("Pending".equalsIgnoreCase(status)) {
            binding.chipStatus.setText(type + " • Pending Approval");
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFF3E0")));
            binding.chipStatus.setTextColor(Color.parseColor("#E65100"));
        } else if ("Resolved".equalsIgnoreCase(status)) {
            binding.chipStatus.setText(type + " • Resolved");
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#ECEFF1")));
            binding.chipStatus.setTextColor(Color.parseColor("#455A64"));
        } else {
            binding.chipStatus.setText(type + " • Approved");
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            binding.chipStatus.setTextColor(Color.parseColor("#2E7D32"));
        }

        // Load item image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            if (item.getImageUrl().startsWith("data:image")) {
                try {
                    byte[] decodedString = android.util.Base64.decode(item.getImageUrl().split(",")[1], android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    binding.ivItemImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    binding.ivItemImage.setImageResource(R.drawable.ic_image);
                }
            } else {
                Glide.with(binding.ivItemImage.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(binding.ivItemImage);
            }
        } else {
            binding.ivItemImage.setImageResource(R.drawable.ic_image);
        }

        binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        binding.btnMenu.setOnClickListener(v -> showPopupMenu(v, item));
    }

    private void showPopupMenu(View view, Item item) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.getMenu().add("Mark as Resolved");

        popup.setOnMenuItemClickListener(menuItem -> {
            String title = menuItem.getTitle().toString();
            switch (title) {
                case "Delete":
                    listener.onDeleteClick(item);
                    break;
                case "Mark as Resolved":
                    listener.onResolveClick(item);
                    break;
                case "Edit":
                    listener.onEditClick(item);
                    break;
            }
            return true;
        });
        popup.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class MyPostViewHolder extends RecyclerView.ViewHolder {
        final ItemMyPostBinding binding;

        public MyPostViewHolder(ItemMyPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
