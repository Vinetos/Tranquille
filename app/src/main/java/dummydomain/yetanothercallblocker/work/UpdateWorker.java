package dummydomain.yetanothercallblocker.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dummydomain.yetanothercallblocker.App;
import dummydomain.yetanothercallblocker.Settings;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdateFinished;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdatingEvent;
import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;
import static dummydomain.yetanothercallblocker.EventUtils.postStickyEvent;
import static dummydomain.yetanothercallblocker.EventUtils.removeStickyEvent;

public class UpdateWorker extends Worker {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateWorker.class);

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LOG.info("doWork() started");

        Settings settings = App.getSettings();

        SecondaryDbUpdatingEvent sticky = new SecondaryDbUpdatingEvent();

        postStickyEvent(sticky);
        try {
            if (DatabaseSingleton.getCommunityDatabase().updateSecondaryDb()) {
                settings.setLastUpdateTime(System.currentTimeMillis());
            }
            settings.setLastUpdateCheckTime(System.currentTimeMillis());
        } finally {
            removeStickyEvent(sticky);
        }

        postEvent(new SecondaryDbUpdateFinished());

        LOG.info("doWork() finished");
        return Result.success();
    }

}
