package fr.vinetos.tranquille;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;

import fr.vinetos.tranquille.data.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;

import static fr.vinetos.tranquille.IntentHelper.clearTop;
import static fr.vinetos.tranquille.IntentHelper.pendingActivity;

public class NotificationHelper {

    private static final String NOTIFICATION_TAG_INCOMING_CALL = "incomingCallNotification";
    private static final String NOTIFICATION_TAG_BLOCKED_CALL = "blockedCallNotification";

    private static final int NOTIFICATION_ID_INCOMING_CALL = 1;
    private static final int NOTIFICATION_ID_BLOCKED_CALL = 2;
    public static final int NOTIFICATION_ID_MONITORING_SERVICE = 3;
    public static final int NOTIFICATION_ID_TASKS = 4;

    private static final String CHANNEL_GROUP_ID_INCOMING_CALLS = "incoming_calls";
    private static final String CHANNEL_GROUP_ID_BLOCKED_CALLS = "blocked_calls";
    private static final String CHANNEL_GROUP_ID_SERVICES = "services";

    private static final String CHANNEL_ID_KNOWN = "known_calls";
    private static final String CHANNEL_ID_POSITIVE = "positive_calls";
    private static final String CHANNEL_ID_NEUTRAL = "neutral_calls";
    private static final String CHANNEL_ID_UNKNOWN = "unknown_calls";
    private static final String CHANNEL_ID_NEGATIVE = "negative_calls";
    private static final String CHANNEL_ID_BLOCKED_INFO = "blocked_info";
    private static final String CHANNEL_ID_MONITORING_SERVICE = "monitoring_service";
    private static final String CHANNEL_ID_TASKS = "tasks";

    private static boolean notificationChannelsInitialized;

    public static void notify(Context context, int id, Notification notification) {
        initNotificationChannels(context);

        NotificationManagerCompat.from(context).notify(id, notification);
    }

    public static void notify(Context context, String tag, int id, Notification notification) {
        initNotificationChannels(context);

        NotificationManagerCompat.from(context).notify(tag, id, notification);
    }

    public static void showIncomingCallNotification(Context context, NumberInfo numberInfo) {
        NotificationWithInfo notificationWithInfo = createIncomingCallNotification(context, numberInfo);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (CHANNEL_ID_KNOWN.equals(notificationWithInfo.channelId)) {
                if (!App.getSettings().getNotificationsForKnownCallers()) {
                    return;
                }
            } else if (CHANNEL_ID_UNKNOWN.equals(notificationWithInfo.channelId)) {
                if (!App.getSettings().getNotificationsForUnknownCallers()) {
                    return;
                }
            }
        }

        notify(context, NOTIFICATION_TAG_INCOMING_CALL, NOTIFICATION_ID_INCOMING_CALL,
                notificationWithInfo.notification);
    }

    public static void hideIncomingCallNotification(Context context) {
        NotificationManagerCompat.from(context)
                .cancel(NOTIFICATION_TAG_INCOMING_CALL, NOTIFICATION_ID_INCOMING_CALL);
    }

    public static void showBlockedCallNotification(Context context, NumberInfo numberInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (!App.getSettings().getNotificationsForBlockedCalls()) {
                return;
            }
        }

        Notification notification = createBlockedCallNotification(context, numberInfo);

        String tag = NOTIFICATION_TAG_BLOCKED_CALL
                + (!numberInfo.noNumber ? numberInfo.number : System.nanoTime()); // TODO: handle repeating
        notify(context, tag, NOTIFICATION_ID_BLOCKED_CALL, notification);
    }

    public static Notification createMonitoringServiceNotification(Context context) {
        initNotificationChannels(context);

        PendingIntent contentIntent = pendingActivity(context,
                new Intent(context, MainActivity.class));

        return new NotificationCompat.Builder(context, CHANNEL_ID_MONITORING_SERVICE)
                .setSmallIcon(R.drawable.ic_security_24dp)
                .setContentTitle(context.getString(R.string.monitoring_service_notification_title))
                .setContentIntent(contentIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setShowWhen(false)
                .build();
    }

    public static Notification createServiceNotification(Context context, String title) {
        initNotificationChannels(context);

        if (title == null) title = context.getString(R.string.notification_background_operation);

        PendingIntent contentIntent = pendingActivity(context,
                new Intent(context, MainActivity.class));

        return new NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
                .setSmallIcon(R.drawable.ic_file_download_24dp)
                .setContentIntent(contentIntent)
                .setContentTitle(title).build();
    }

    private static NotificationWithInfo createIncomingCallNotification(
            Context context, NumberInfo numberInfo) {
        boolean unknown = false;
        String channelId;
        String title;
        String text = "";
        switch (numberInfo.rating) {
            case POSITIVE:
                channelId = CHANNEL_ID_POSITIVE;
                title = context.getString(R.string.notification_incoming_call_positive);
                break;

            case NEUTRAL:
                channelId = CHANNEL_ID_NEUTRAL;
                title = context.getString(R.string.notification_incoming_call_neutral);
                break;

            case NEGATIVE:
                channelId = CHANNEL_ID_NEGATIVE;
                title = context.getString(R.string.notification_incoming_call_negative);
                break;

            default:
                unknown = true;
                channelId = CHANNEL_ID_UNKNOWN;
                title = context.getString(R.string.notification_incoming_call_unknown);
                break;
        }

        // up for debate
        if (numberInfo.contactItem != null && unknown) {
            channelId = CHANNEL_ID_KNOWN;
            title = context.getString(R.string.notification_incoming_call_contact);
        }

        title = concat(title, " - ", getTitleExtra(context, numberInfo));

        text += getInfoDescription(context, numberInfo);

        IconAndColor iconAndColor = IconAndColor.forNumberRating(
                numberInfo.rating, numberInfo.contactItem != null);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconAndColor.iconResId)
                .setColor(iconAndColor.getColorInt(context))
                .setContentTitle(title)
                .setContentText(firstLine(text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC); // TODO: check

        addCallNotificationIntents(context, builder, numberInfo);

        return new NotificationWithInfo(builder.build(), channelId);
    }

    private static Notification createBlockedCallNotification(Context context, NumberInfo numberInfo) {
        String title = concat(context.getString(R.string.notification_blocked_call),
                " - ", getTitleExtra(context, numberInfo));

        String text = getBlockedDescription(context, numberInfo);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, CHANNEL_ID_BLOCKED_INFO)
                .setSmallIcon(R.drawable.ic_brick_24dp)
                .setColor(UiUtils.getColorInt(context, R.color.rateNegative))
                .setContentTitle(title)
                .setContentText(firstLine(text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS);

        addCallNotificationIntents(context, builder, numberInfo);

        return builder.build();
    }

    private static String getTitleExtra(Context context, NumberInfo numberInfo) {
        return NumberInfoUtils.getShortDescription(context, numberInfo);
    }

    private static String getInfoDescription(Context context, NumberInfo numberInfo) {
        String text = numberInfo.name;

        text = concat(text, "; ", getCommunityDescriptionPart(context, numberInfo));
        text = concat(text, "\n", getBlacklistDescriptionPart(context, numberInfo));
        text = concat(text, "\n", getNumberDescriptionPart(context, numberInfo));

        return text;
    }

    private static String getBlockedDescription(Context context, NumberInfo numberInfo) {
        String text = numberInfo.name;

        text = concat(text, "\n", getCommunityDescriptionPart(context, numberInfo));
        text = concat(text, "\n", getBlacklistDescriptionPart(context, numberInfo));
        text = concat(text, "\n", getNumberDescriptionPart(context, numberInfo));

        return text;
    }

    private static String getNumberDescriptionPart(Context context, NumberInfo numberInfo) {
        return numberInfo.noNumber ? context.getString(R.string.no_number) : numberInfo.number;
    }

    private static String getCommunityDescriptionPart(Context context, NumberInfo numberInfo) {
        if (numberInfo.communityDatabaseItem != null) {
            CommunityDatabaseItem communityItem = numberInfo.communityDatabaseItem;

            if (communityItem.hasRatings()) {
                return context.getString(R.string.notification_incoming_call_text_description,
                        communityItem.getNegativeRatingsCount(), communityItem.getPositiveRatingsCount(),
                        communityItem.getNeutralRatingsCount());
            }
        }

        return null;
    }

    private static String getBlacklistDescriptionPart(Context context, NumberInfo numberInfo) {
        if (numberInfo.blacklistItem != null && numberInfo.contactItem == null) {
            String name = numberInfo.blacklistItem.getName();
            return context.getString(R.string.info_in_blacklist)
                    + (!TextUtils.isEmpty(name) ? " (" + name + ")" : "");
        }

        return null;
    }

    private static String concat(String base, String delimiter, String extra) {
        if (!TextUtils.isEmpty(extra)) {
            if (TextUtils.isEmpty(base)) {
                base = "";
            } else {
                base += delimiter;
            }
            base += extra;
        }
        return base;
    }

    private static String firstLine(String s) {
        int index;
        if (TextUtils.isEmpty(s) || (index = s.indexOf('\n')) == -1) return s;
        return s.substring(0, index);
    }

    private static void addCallNotificationIntents(Context context,
                                                   NotificationCompat.Builder builder,
                                                   NumberInfo numberInfo) {
        builder.setContentIntent(createInfoIntent(context, numberInfo));

        if (!numberInfo.noNumber && numberInfo.contactItem == null) {
            builder.addAction(0, context.getString(R.string.online_reviews),
                    createReviewsIntent(context, numberInfo));
        }
    }

    private static PendingIntent createInfoIntent(Context context, NumberInfo numberInfo) {
        return pendingActivity(context, InfoDialogActivity.getIntent(context, numberInfo.number));
    }

    private static PendingIntent createReviewsIntent(Context context, NumberInfo numberInfo) {
        return pendingActivity(context, clearTop(
                ReviewsActivity.getNumberIntent(context, numberInfo.number)));
    }

    public static void initNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        if (notificationChannelsInitialized) return;
        synchronized (NotificationHelper.class) {
            if (notificationChannelsInitialized) return;
            createNotificationChannels(context);
            notificationChannelsInitialized = true;
        }
    }

    private static void createNotificationChannels(Context context) {
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

            NotificationChannelGroup channelGroupServices = new NotificationChannelGroup(
                    CHANNEL_GROUP_ID_SERVICES,
                    context.getString(R.string.notification_channel_group_name_services));
            notificationManager.createNotificationChannelGroup(channelGroupServices);

            List<NotificationChannel> channels = new ArrayList<>();

            NotificationChannel channel;

            channel = new NotificationChannel(
                    CHANNEL_ID_KNOWN, context.getString(R.string.notification_channel_name_known),
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
                    CHANNEL_ID_MONITORING_SERVICE,
                    context.getString(R.string.notification_channel_name_monitoring_service),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroupServices.getId());
            channels.add(channel);

            channel = new NotificationChannel(
                    CHANNEL_ID_TASKS, context.getString(R.string.notification_channel_name_tasks),
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setGroup(channelGroupServices.getId());
            channels.add(channel);

            notificationManager.createNotificationChannels(channels);
        }
    }

    private static class NotificationWithInfo {
        private Notification notification;
        private String channelId;

        NotificationWithInfo(Notification notification, String channelId) {
            this.notification = notification;
            this.channelId = channelId;
        }
    }

}
