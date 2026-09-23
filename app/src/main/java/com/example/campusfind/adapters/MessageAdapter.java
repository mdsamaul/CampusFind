package com.example.campusfind.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.campusfind.R;
import com.example.campusfind.databinding.ItemMessageReceivedBinding;
import com.example.campusfind.databinding.ItemMessageSentBinding;
import com.example.campusfind.models.Message;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messages;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    private String getCurrentUserId() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        return "";
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        String currentUid = getCurrentUserId();
        if (message != null && message.getSenderId() != null && !currentUid.isEmpty() && currentUid.equals(message.getSenderId())) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            ItemMessageSentBinding binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SentViewHolder(binding);
        } else {
            ItemMessageReceivedBinding binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ReceivedViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        
        String timeStr = "";
        if (message.getTimestamp() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            timeStr = sdf.format(new Date(message.getTimestamp()));
        }

        if (holder instanceof SentViewHolder) {
            SentViewHolder sentHolder = (SentViewHolder) holder;
            bindMessageContent(message, sentHolder.binding.tvMessage, sentHolder.binding.ivMessageImage);
            sentHolder.binding.tvSentTime.setText(timeStr);

            if (message.isSeen()) {
                sentHolder.binding.tvSeenStatus.setText("✓✓ Seen");
                sentHolder.binding.tvSeenStatus.setTextColor(android.graphics.Color.parseColor("#1976D2"));
            } else {
                sentHolder.binding.tvSeenStatus.setText("✓ Delivered");
                sentHolder.binding.tvSeenStatus.setTextColor(android.graphics.Color.parseColor("#74777F"));
            }
        } else if (holder instanceof ReceivedViewHolder) {
            ReceivedViewHolder recvHolder = (ReceivedViewHolder) holder;
            bindMessageContent(message, recvHolder.binding.tvMessage, recvHolder.binding.ivMessageImage);
            recvHolder.binding.tvReceivedTime.setText(timeStr);
        }
    }

    private void bindMessageContent(Message message, android.widget.TextView tvMessage, com.google.android.material.imageview.ShapeableImageView ivMessageImage) {
        String type = message.getType();
        String content = message.getContent();

        if ("IMAGE".equalsIgnoreCase(type) && content != null && !content.isEmpty()) {
            ivMessageImage.setVisibility(View.VISIBLE);
            tvMessage.setVisibility(View.GONE);

            if (content.startsWith("data:image")) {
                try {
                    byte[] decodedString = android.util.Base64.decode(content.split(",")[1], android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivMessageImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    ivMessageImage.setImageResource(R.drawable.ic_image);
                }
            } else {
                Glide.with(ivMessageImage.getContext())
                        .load(content)
                        .placeholder(R.drawable.ic_image)
                        .into(ivMessageImage);
            }
        } else {
            ivMessageImage.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(content);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {
        final ItemMessageSentBinding binding;

        public SentViewHolder(ItemMessageSentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        final ItemMessageReceivedBinding binding;

        public ReceivedViewHolder(ItemMessageReceivedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
