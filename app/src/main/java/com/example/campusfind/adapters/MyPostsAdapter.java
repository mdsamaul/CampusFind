package com.example.campusfind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
        holder.binding.tvItemTitle.setText(item.getTitle());
        holder.binding.tvItemCategory.setText("Category: " + item.getCategory());
        
        // Format timestamp
        if (item.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.binding.tvTimePosted.setText(sdf.format(new Date(item.getTimestamp())));
        } else {
            holder.binding.tvTimePosted.setText("Unknown time");
        }
        
        holder.binding.chipStatus.setText(item.getType());

        holder.binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));

        holder.binding.btnMenu.setOnClickListener(v -> showPopupMenu(v, item));
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
