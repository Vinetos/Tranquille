package dummydomain.yetanothercallblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import dummydomain.yetanothercallblocker.sia.model.NumberCategory;
import dummydomain.yetanothercallblocker.sia.model.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;

public class NotificationHelper {

    private static final String NOTIFICATION_TAG = "incomingCallNotification";
    private static final int NOTIFICATION_ID = 1;

    private static final String CHANNEL_GROUP_ID_INCOMING_CALLS = "incoming_calls";
    private static final String CHANNEL_ID_POSITIVE_KNOWN = "positive_known_calls";
    private static final String CHANNEL_ID_POSITIVE = "positive_calls";
    private static final String CHANNEL_ID_NEUTRAL = "neutral_calls";
    private static final String CHANNEL_ID_UNKNOWN = "unknown_calls";
    private static final String CHANNEL_ID_NEGATIVE = "negative_calls";

    public static void showIncomingCallNotification(Context context, NumberInfo numberInfo) {
        createNotificationChannels(context);

        Notification notification = createIncomingCallNotification(context, numberInfo);

        String tag = numberInfo.number != null ? NOTIFICATION_TAG + numberInfo.number : null;
        NotificationManagerCompat.from(context).notify(tag, NOTIFICATION_ID, notification);
    }

    public static void hideIncomingCallNotification(Context context, String number) {
        String tag = number != null ? NOTIFICATION_TAG + number : null;
        NotificationManagerCompat.from(context).cancel(tag, NOTIFICATION_ID);
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
                icon = R.drawable.ic_thumb_up_black_24dp;
                color = 0xff00ff00;
                break;

            case NEUTRAL:
                channelId = CHANNEL_ID_NEUTRAL;
                title = context.getString(R.string.notification_incoming_call_neutral);
                icon = R.drawable.ic_thumbs_up_down_black_24dp;
                color = 0xffffff60;
                break;

            case NEGATIVE:
                channelId = CHANNEL_ID_NEGATIVE;
                title = context.getString(R.string.notification_incoming_call_negative);
                icon = R.drawable.ic_thumb_down_black_24dp;
                color = 0xffff0000;
                break;

            default:
                channelId = CHANNEL_ID_UNKNOWN;
                title = context.getString(R.string.notification_incoming_call_unknown);
                icon = R.drawable.ic_thumbs_up_down_black_24dp;
                color = 0xffffff60;
                break;
        }

        if (numberInfo.communityDatabaseItem != null) {
            CommunityDatabaseItem communityItem = numberInfo.communityDatabaseItem;

            NumberCategory category = NumberCategory.getById(communityItem.getCategory());
            if (category != null && category != NumberCategory.NONE) {
                title += " - " + NumberCategory.getString(context, category);
            }

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

        Intent intent = ReviewsActivity.getNumberIntent(context, numberInfo.number);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setColor(color)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // TODO: check
                .setContentIntent(pendingIntent)
                .build();

        return notification;
    }

    private static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            if (notificationManager.getNotificationChannel(CHANNEL_ID_POSITIVE_KNOWN) != null) {
                // already created
                return;
            }

            NotificationChannelGroup channelGroup = new NotificationChannelGroup(CHANNEL_GROUP_ID_INCOMING_CALLS,
                    context.getString(R.string.notification_channel_group_name_incoming_calls));
            notificationManager.createNotificationChannelGroup(channelGroup);

            List<NotificationChannel> channels = new ArrayList<>();

            NotificationChannel channel;

            channel = new NotificationChannel(
                    CHANNEL_ID_POSITIVE_KNOWN, context.getString(R.string.notification_channel_name_positive_known),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setGroup(channelGroup.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_POSITIVE, context.getString(R.string.notification_channel_name_positive),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroup.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_NEUTRAL, context.getString(R.string.notification_channel_name_neutral),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setGroup(channelGroup.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_UNKNOWN, context.getString(R.string.notification_channel_name_unknown),
                    NotificationManager.IMPORTANCE_MIN
            );
            channel.setGroup(channelGroup.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_NEGATIVE, context.getString(R.string.notification_channel_name_negative),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroup.getId());
            channels.add(channel);

            notificationManager.createNotificationChannels(channels);
        }
    }

}
