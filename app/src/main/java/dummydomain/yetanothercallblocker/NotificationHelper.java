package dummydomain.yetanothercallblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import dummydomain.yetanothercallblocker.sia.model.NumberCategory;
import dummydomain.yetanothercallblocker.sia.model.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;

public class NotificationHelper {

    private static final String NOTIFICATION_TAG_INCOMING_CALL = "incomingCallNotification";
    private static final String NOTIFICATION_TAG_BLOCKED_CALL = "blockedCallNotification";

    private static final int NOTIFICATION_ID_INCOMING_CALL = 1;
    private static final int NOTIFICATION_ID_BLOCKED_CALL = 2;
    public static final int NOTIFICATION_ID_TASKS = 3;

    private static final String CHANNEL_GROUP_ID_INCOMING_CALLS = "incoming_calls";
    private static final String CHANNEL_GROUP_ID_BLOCKED_CALLS = "blocked_calls";
    private static final String CHANNEL_GROUP_ID_TASKS = "tasks";

    private static final String CHANNEL_ID_POSITIVE_KNOWN = "positive_known_calls";
    private static final String CHANNEL_ID_POSITIVE = "positive_calls";
    private static final String CHANNEL_ID_NEUTRAL = "neutral_calls";
    private static final String CHANNEL_ID_UNKNOWN = "unknown_calls";
    private static final String CHANNEL_ID_NEGATIVE = "negative_calls";
    private static final String CHANNEL_ID_BLOCKED_INFO = "blocked_info";
    public static final String CHANNEL_ID_TASKS = "tasks";

    public static void notify(Context context, int id, Notification notification) {
        NotificationManagerCompat.from(context).notify(id, notification);
    }

    public static void notify(Context context, String tag, int id, Notification notification) {
        NotificationManagerCompat.from(context).notify(tag, id, notification);
    }

    public static void showIncomingCallNotification(Context context, NumberInfo numberInfo) {
        Notification notification = createIncomingCallNotification(context, numberInfo);

        String tag = numberInfo.number != null ? NOTIFICATION_TAG_INCOMING_CALL + numberInfo.number : null;
        notify(context, tag, NOTIFICATION_ID_INCOMING_CALL, notification);
    }

    public static void hideIncomingCallNotification(Context context, String number) {
        String tag = number != null ? NOTIFICATION_TAG_INCOMING_CALL + number : null;
        NotificationManagerCompat.from(context).cancel(tag, NOTIFICATION_ID_INCOMING_CALL);
    }

    public static void showBlockedCallNotification(Context context, NumberInfo numberInfo) {
        Notification notification = createBlockedCallNotification(context, numberInfo);

        String tag = numberInfo.number != null ? NOTIFICATION_TAG_BLOCKED_CALL + numberInfo.number : null; // TODO: handle repeating
        notify(context, tag, NOTIFICATION_ID_BLOCKED_CALL, notification);
    }

    public static Notification createServiceNotification(Context context, String title) {
        if (title == null) title = context.getString(R.string.notification_background_operation);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, intent, 0);

        return new NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
                .setSmallIcon(R.drawable.ic_file_download_24dp)
                .setContentIntent(contentIntent)
                .setContentTitle(title).build();
    }

    private static Notification createIncomingCallNotification(Context context, NumberInfo numberInfo) {
        String channelId;
        String title;
        String text = "";
        @DrawableRes int icon;
        @ColorInt int color;
        switch (numberInfo.rating) {
            case POSITIVE:
                channelId = numberInfo.known ? CHANNEL_ID_POSITIVE_KNOWN : CHANNEL_ID_POSITIVE;
                title = context.getString(R.string.notification_incoming_call_positive);
                icon = R.drawable.ic_thumb_up_24dp;
                color = 0xff00ff00;
                break;

            case NEUTRAL:
                channelId = CHANNEL_ID_NEUTRAL;
                title = context.getString(R.string.notification_incoming_call_neutral);
                icon = R.drawable.ic_thumbs_up_down_24dp;
                color = 0xffffff60;
                break;

            case NEGATIVE:
                channelId = CHANNEL_ID_NEGATIVE;
                title = context.getString(R.string.notification_incoming_call_negative);
                icon = R.drawable.ic_thumb_down_24dp;
                color = 0xffff0000;
                break;

            default:
                channelId = CHANNEL_ID_UNKNOWN;
                title = context.getString(R.string.notification_incoming_call_unknown);
                icon = R.drawable.ic_thumbs_up_down_24dp;
                color = 0xffffff60;
                break;
        }

        if (numberInfo.communityDatabaseItem != null) {
            CommunityDatabaseItem communityItem = numberInfo.communityDatabaseItem;

            NumberCategory category = NumberCategory.getById(communityItem.getCategory());
            if (category != null && category != NumberCategory.NONE) {
                title += " - " + NumberCategory.getString(context, category);
            }
        }

        text += getDescription(context, numberInfo);

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setColor(color)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // TODO: check
                .setContentIntent(createReviewsIntent(context, numberInfo))
                .build();
    }

    private static Notification createBlockedCallNotification(Context context, NumberInfo numberInfo) {
        String title = context.getString(R.string.notification_blocked_call);
        String text = getDescription(context, numberInfo);

        return new NotificationCompat.Builder(context, CHANNEL_ID_BLOCKED_INFO)
                .setSmallIcon(R.drawable.ic_thumb_down_24dp)
                .setColor(0xffffff60)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setContentIntent(createReviewsIntent(context, numberInfo))
                .build();
    }

    private static String getDescription(Context context, NumberInfo numberInfo) {
        String text = "";

        if (numberInfo.communityDatabaseItem != null) {
            CommunityDatabaseItem communityItem = numberInfo.communityDatabaseItem;

            if (numberInfo.name != null) {
                text += numberInfo.name;
            }

            if (communityItem.hasRatings()) {
                if (!text.isEmpty()) text += "; ";
                text += context.getString(R.string.notification_incoming_call_text_description,
                        communityItem.getNegativeRatingsCount(), communityItem.getPositiveRatingsCount(),
                        communityItem.getNeutralRatingsCount());
            }
        }

        return text;
    }

    private static PendingIntent createReviewsIntent(Context context, NumberInfo numberInfo) {
        Intent intent = ReviewsActivity.getNumberIntent(context, numberInfo.number);
        intent.setAction(Long.toString(System.currentTimeMillis())); // make the intent "unique"
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            NotificationChannelGroup channelGroupIncoming = new NotificationChannelGroup(
                    CHANNEL_GROUP_ID_INCOMING_CALLS,
                    context.getString(R.string.notification_channel_group_name_incoming_calls));
            notificationManager.createNotificationChannelGroup(channelGroupIncoming);

            NotificationChannelGroup channelGroupBlocked = new NotificationChannelGroup(
                    CHANNEL_GROUP_ID_BLOCKED_CALLS,
                    context.getString(R.string.notification_channel_group_name_blocked_calls));
            notificationManager.createNotificationChannelGroup(channelGroupBlocked);

            NotificationChannelGroup channelGroupTasks = new NotificationChannelGroup(
                    CHANNEL_GROUP_ID_TASKS,
                    context.getString(R.string.notification_channel_group_name_tasks));
            notificationManager.createNotificationChannelGroup(channelGroupTasks);

            List<NotificationChannel> channels = new ArrayList<>();

            NotificationChannel channel;

            channel = new NotificationChannel(
                    CHANNEL_ID_POSITIVE_KNOWN, context.getString(R.string.notification_channel_name_positive_known),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setGroup(channelGroupIncoming.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_POSITIVE, context.getString(R.string.notification_channel_name_positive),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroupIncoming.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_NEUTRAL, context.getString(R.string.notification_channel_name_neutral),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setGroup(channelGroupIncoming.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_UNKNOWN, context.getString(R.string.notification_channel_name_unknown),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setGroup(channelGroupIncoming.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_NEGATIVE, context.getString(R.string.notification_channel_name_negative),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroupIncoming.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_BLOCKED_INFO, context.getString(R.string.notification_channel_name_blocked_info),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroupBlocked.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_TASKS, context.getString(R.string.notification_channel_name_tasks),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroupTasks.getId());
            channels.add(channel);

            notificationManager.createNotificationChannels(channels);
        }
    }

}
