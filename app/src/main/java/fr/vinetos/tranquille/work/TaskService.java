package fr.vinetos.tranquille.work;

import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vinetos.tranquille.App;
import fr.vinetos.tranquille.EventUtils;
import fr.vinetos.tranquille.NotificationHelper;
import fr.vinetos.tranquille.event.MainDbDownloadFinishedEvent;
import fr.vinetos.tranquille.event.MainDbDownloadingEvent;
import fr.vinetos.tranquille.R;
import fr.vinetos.tranquille.data.YacbHolder;

public class TaskService extends IntentService {

    public static final String TASK_DOWNLOAD_MAIN_DB = "download_main_db";
    public static final String TASK_UPDATE_SECONDARY_DB = "update_secondary_db";
    public static final String TASK_FILTER_DB = "filter_db";

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID_TASKS, createNotification(null),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            );
        } else {
            startForeground(
                NotificationHelper.NOTIFICATION_ID_TASKS, createNotification(null)
            );
        }
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

                    case TASK_FILTER_DB:
                        updateNotification(getString(R.string.filtering_db));
                        filterDb();
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
        return NotificationHelper.createServiceNotification(getApplicationContext(), title);
    }

    private void updateNotification(String title) {
        NotificationHelper.notify(getApplicationContext(),
                NotificationHelper.NOTIFICATION_ID_TASKS, createNotification(title));
    }

    private void downloadMainDb() {
        MainDbDownloadingEvent sticky = new MainDbDownloadingEvent();

        EventUtils.postStickyEvent(sticky);
        try {
            YacbHolder.getDbManager().downloadMainDb(App.getSettings().getDatabaseDownloadUrl());
            YacbHolder.getCommunityDatabase().reload();
            YacbHolder.getFeaturedDatabase().reload();
            YacbHolder.getSiaMetadata().reload();
        } catch (Exception e) {
            LOG.warn("downloadMainDb()", e);
        } finally {
            EventUtils.removeStickyEvent(sticky);
        }

        EventUtils.postEvent(new MainDbDownloadFinishedEvent());
    }

    private void updateSecondaryDb() {
        new DbUpdater().update();
    }

    private void filterDb() {
        YacbHolder.getDbManager().filterDb();
    }

}
