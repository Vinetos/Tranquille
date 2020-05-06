package dummydomain.yetanothercallblocker.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dummydomain.yetanothercallblocker.event.SecondaryDbUpdateFinished;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdatingEvent;
import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;

public class UpdateWorker extends Worker {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateWorker.class);

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LOG.info("doWork() started");

        EventBus bus = EventBus.getDefault();

        SecondaryDbUpdatingEvent sticky = new SecondaryDbUpdatingEvent();

        bus.postSticky(sticky);
        try {
            DatabaseSingleton.getCommunityDatabase().updateSecondaryDb();
        } finally {
            bus.removeStickyEvent(sticky);
        }

        bus.post(new SecondaryDbUpdateFinished());

        LOG.info("doWork() finished");
        return Result.success();
    }

}
