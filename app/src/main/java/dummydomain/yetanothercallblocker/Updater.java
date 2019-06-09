package dummydomain.yetanothercallblocker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class Updater {

    private static final String AUTO_UPDATE_WORK_TAG = "autoUpdateWork";

    private static final Logger LOG = LoggerFactory.getLogger(Updater.class);

    public static void scheduleAutoUpdateWorker() {
        if (isAutoUpdateScheduled()) return;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest updateRequest =
                new PeriodicWorkRequest.Builder(UpdateWorker.class, 1, TimeUnit.DAYS)
                        .addTag(AUTO_UPDATE_WORK_TAG)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance().enqueue(updateRequest);
    }

    public static void cancelAutoUpdateWorker() {
        WorkManager.getInstance().cancelAllWorkByTag(AUTO_UPDATE_WORK_TAG);
    }

    public static boolean isAutoUpdateScheduled() {
        return findScheduled() != null;
    }

    private static WorkInfo findScheduled() {
        for (WorkInfo workInfo : getWorkInfoList()) {
            if (workInfo.getState() == WorkInfo.State.ENQUEUED) return workInfo;
        }
        return null;
    }

    private static List<WorkInfo> getWorkInfoList() {
        try {
            return WorkManager.getInstance().getWorkInfosByTag(AUTO_UPDATE_WORK_TAG).get();
        } catch (Exception e) {
            LOG.warn("getWorkInfoList()", e);
        }
        return new ArrayList<>();
    }

}
