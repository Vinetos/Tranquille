package fr.vinetos.tranquille.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateWorker extends Worker {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateWorker.class);

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        LOG.info("doWork() started");

        try {
            new DbUpdater().update();
        } catch (Exception e) {
            LOG.error("doWork() error", e);
        }

        LOG.info("doWork() finished");
        return Result.success();
    }

}
