package dummydomain.yetanothercallblocker.work;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dummydomain.yetanothercallblocker.NotificationHelper;
import dummydomain.yetanothercallblocker.R;
import dummydomain.yetanothercallblocker.event.MainDbDownloadFinishedEvent;
import dummydomain.yetanothercallblocker.event.MainDbDownloadingEvent;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdateFinished;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdatingEvent;
import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;
import static dummydomain.yetanothercallblocker.EventUtils.postStickyEvent;
import static dummydomain.yetanothercallblocker.EventUtils.removeStickyEvent;

public class TaskService extends IntentService {

    public static final String TASK_DOWNLOAD_MAIN_DB = "download_main_db";
    public static final String TASK_UPDATE_SECONDARY_DB = "update_secondary_db";

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    public static void start(Context context, String task) {
        Intent intent = new Intent(context, TaskService.class);
        intent.setAction(task);
        ContextCompat.startForegroundService(context, intent);
    }

    public TaskService() {
        super(TaskService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        String action = intent != null ? intent.getAction() : null;

        startForeground(NotificationHelper.NOTIFICATION_ID_TASKS, createNotification(null));
        try {
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case TASK_DOWNLOAD_MAIN_DB:
                        updateNotification(getString(R.string.main_db_downloading));
                        downloadMainDb();
                        break;

                    case TASK_UPDATE_SECONDARY_DB:
                        updateNotification(getString(R.string.secondary_db_updating));
                        updateSecondaryDb();
                        break;

                    default:
                        LOG.warn("Unknown action: " + action);
                        break;
                }
            }
        } finally {
            stopForeground(true);
        }
    }

    private Notification createNotification(String title) {
        if (title == null) title = getString(R.string.notification_background_operation);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), NotificationHelper.CHANNEL_ID_TASKS)
                .setSmallIcon(R.drawable.ic_file_download_black_24dp);

        builder.setContentTitle(title);

        return builder.build();
    }

    private void updateNotification(String title) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat
                .from(getApplicationContext());

        notificationManager.notify(NotificationHelper.NOTIFICATION_ID_TASKS,
                createNotification(title));
    }

    private void downloadMainDb() {
        MainDbDownloadingEvent sticky = new MainDbDownloadingEvent();

        postStickyEvent(sticky);
        try {
            DbManager.downloadMainDb();
            DatabaseSingleton.getCommunityDatabase().reload();
            DatabaseSingleton.getFeaturedDatabase().reload();
        } finally {
            removeStickyEvent(sticky);
        }

        postEvent(new MainDbDownloadFinishedEvent());
    }

    private void updateSecondaryDb() {
        SecondaryDbUpdatingEvent sticky = new SecondaryDbUpdatingEvent();

        postStickyEvent(sticky);
        try {
            DatabaseSingleton.getCommunityDatabase().updateSecondaryDb();
        } finally {
            removeStickyEvent(sticky);
        }

        postEvent(new SecondaryDbUpdateFinished());
    }

}
