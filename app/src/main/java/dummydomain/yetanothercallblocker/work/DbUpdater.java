package dummydomain.yetanothercallblocker.work;

import dummydomain.yetanothercallblocker.App;
import dummydomain.yetanothercallblocker.Settings;
import dummydomain.yetanothercallblocker.data.YacbHolder;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdateFinished;
import dummydomain.yetanothercallblocker.event.SecondaryDbUpdatingEvent;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;
import static dummydomain.yetanothercallblocker.EventUtils.postStickyEvent;
import static dummydomain.yetanothercallblocker.EventUtils.removeStickyEvent;

public class DbUpdater {

    public void update() {
        Settings settings = App.getSettings();

        boolean updated = false;

        SecondaryDbUpdatingEvent sticky = new SecondaryDbUpdatingEvent();

        postStickyEvent(sticky);
        try {
            DbManager.UpdateResult updateResult = YacbHolder.getDbManager().updateSecondaryDb();
            if (updateResult.isUpdated()) {
                settings.setLastUpdateTime(System.currentTimeMillis());
                updated = true;
            } // TODO: handle other results
            settings.setLastUpdateCheckTime(System.currentTimeMillis());
        } finally {
            removeStickyEvent(sticky);
            postEvent(new SecondaryDbUpdateFinished(updated));
        }
    }

}
