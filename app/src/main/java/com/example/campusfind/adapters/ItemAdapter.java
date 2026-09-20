package com.example.campusfind.adapters;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.campusfind.ItemDetailsActivity;
import com.example.campusfind.R;
import com.example.campusfind.databinding.ItemCardBinding;
import com.example.campusfind.models.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items;
    private boolean isAdmin = false;
    private OnItemLongClickListener longClickListener;

    public interface OnItemLongClickListener {
        void onItemLongClick(Item item);
    }

    public ItemAdapter(List<Item> items) {
        this.items = items;
    }

    public ItemAdapter(List<Item> items, boolean isAdmin, OnItemLongClickListener longClickListener) {
        this.items = items;
        this.isAdmin = isAdmin;
        this.longClickListener = longClickListener;
    }

    public void updateList(List<Item> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        ItemCardBinding binding = holder.binding;

        binding.tvItemTitle.setText(item.getTitle());
        binding.tvItemCategory.setText("Category: " + item.getCategory());
        binding.tvItemLocation.setText("Location: " + item.getLocation());
        
        // Format timestamp
        if (item.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            binding.tvTimePosted.setText(sdf.format(new Date(item.getTimestamp())));
        } else {
            binding.tvTimePosted.setText("Unknown time");
        }
        
        binding.chipType.setText(item.getType());

        if ("Lost".equals(item.getType())) {
            binding.chipType.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            binding.chipType.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            binding.chipType.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
            binding.chipType.setTextColor(Color.parseColor("#388E3C"));
        }

        // Load image (Support both URL and Base64)
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            if (item.getImageUrl().startsWith("data:image")) {
                // It's a Base64 string
                byte[] decodedString = android.util.Base64.decode(item.getImageUrl().split(",")[1], android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.ivItemImage.setImageBitmap(decodedByte);
            } else {
                // It's a regular URL
                Glide.with(binding.ivItemImage.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .into(binding.ivItemImage);
            }
        } else {
            binding.ivItemImage.setImageResource(R.drawable.ic_image);
        }

        binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(binding.getRoot().getContext(), ItemDetailsActivity.class);
            // Pass item ID or whole object
            intent.putExtra("itemId", item.getId());
            binding.getRoot().getContext().startActivity(intent);
        });

        if (isAdmin && longClickListener != null) {
            binding.btnMenu.setVisibility(android.view.View.VISIBLE);
            binding.btnMenu.setOnClickListener(v -> longClickListener.onItemLongClick(item));
            
            binding.getRoot().setOnLongClickListener(v -> {
                longClickListener.onItemLongClick(item);
                return true;
            });
        } else {
            binding.btnMenu.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        final ItemCardBinding binding;

        public ItemViewHolder(ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
