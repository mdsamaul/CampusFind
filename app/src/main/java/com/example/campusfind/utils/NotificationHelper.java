package com.example.campusfind.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.campusfind.ChatActivity;
import com.example.campusfind.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "campus_find_chat_channel";
    private static final String CHANNEL_NAME = "Chat Notifications";

    public static void showChatNotification(Context context, String senderName, String messageText, String receiverId, String itemId, String itemTitle) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for new chat messages");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Intent to open ChatActivity when notification is clicked
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("RECEIVER_ID", receiverId);
        intent.putExtra("ITEM_ID", itemId);
        intent.putExtra("ITEM_TITLE", itemTitle);
        intent.putExtra("USER_NAME", senderName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setContentTitle(senderName != null ? senderName : "New Message")
                .setContentText(messageText)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
