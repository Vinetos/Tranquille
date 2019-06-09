package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.support.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
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

        DatabaseSingleton.getCommunityDatabase().updateSecondaryDb();

        LOG.info("doWork() finished");
        return Result.success();
    }

}
