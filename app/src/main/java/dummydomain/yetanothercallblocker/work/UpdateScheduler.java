package dummydomain.yetanothercallblocker.work;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpdateScheduler {

    private static final String AUTO_UPDATE_WORK_TAG = "autoUpdateWork";

    private static final Logger LOG = LoggerFactory.getLogger(UpdateScheduler.class);

    private Context context;

    public static UpdateScheduler get(Context context) {
        return new UpdateScheduler(context);
    }

    private UpdateScheduler(Context context) {
        this.context = context.getApplicationContext();
    }

    public void scheduleAutoUpdates() {
        if (isAutoUpdateScheduled()) return;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest updateRequest =
                new PeriodicWorkRequest.Builder(UpdateWorker.class, 1, TimeUnit.DAYS)
                        .addTag(AUTO_UPDATE_WORK_TAG)
                        .setConstraints(constraints)
                        .build();

        getWorkManager().enqueue(updateRequest);
    }

    public void cancelAutoUpdateWorker() {
        getWorkManager().cancelAllWorkByTag(AUTO_UPDATE_WORK_TAG);
    }

    public boolean isAutoUpdateScheduled() {
        return findScheduled() != null;
    }

    private WorkInfo findScheduled() {
        for (WorkInfo workInfo : getWorkInfoList()) {
            if (workInfo.getState() == WorkInfo.State.ENQUEUED) return workInfo;
        }
        return null;
    }

    private List<WorkInfo> getWorkInfoList() {
        try {
            return getWorkManager().getWorkInfosByTag(AUTO_UPDATE_WORK_TAG).get();
        } catch (Exception e) {
            LOG.warn("getWorkInfoList()", e);
        }
        return new ArrayList<>();
    }

    private WorkManager getWorkManager() {
        return WorkManager.getInstance(context);
    }

}
