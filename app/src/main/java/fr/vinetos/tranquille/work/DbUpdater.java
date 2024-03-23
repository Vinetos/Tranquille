package fr.vinetos.tranquille.work;

import fr.vinetos.tranquille.App;
import fr.vinetos.tranquille.EventUtils;
import fr.vinetos.tranquille.Settings;
import fr.vinetos.tranquille.event.SecondaryDbUpdateFinished;
import fr.vinetos.tranquille.event.SecondaryDbUpdatingEvent;
import fr.vinetos.tranquille.data.YacbHolder;
import dummydomain.yetanothercallblocker.sia.model.database.DbManager;

public class DbUpdater {

    public void update() {
        Settings settings = App.getSettings();

        boolean updated = false;

        SecondaryDbUpdatingEvent sticky = new SecondaryDbUpdatingEvent();

        EventUtils.postStickyEvent(sticky);
        try {
            DbManager.UpdateResult updateResult = YacbHolder.getDbManager().updateSecondaryDb();
            if (updateResult.isUpdated()) {
                settings.setLastUpdateTime(System.currentTimeMillis());
                updated = true;
            } // TODO: handle other results
            settings.setLastUpdateCheckTime(System.currentTimeMillis());
        } finally {
            EventUtils.removeStickyEvent(sticky);
            EventUtils.postEvent(new SecondaryDbUpdateFinished(updated));
        }
    }

}
