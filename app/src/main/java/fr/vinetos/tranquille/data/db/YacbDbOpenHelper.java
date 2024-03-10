package fr.vinetos.tranquille.data.db;

import android.content.Context;

import org.greenrobot.greendao.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YacbDbOpenHelper extends DaoMaster.OpenHelper {

    private static final Logger LOG = LoggerFactory.getLogger(YacbDbOpenHelper.class);

    public YacbDbOpenHelper(Context context, String name) {
        super(context, name);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        LOG.info("onUpgrade() oldVersion={}, newVersion={}", oldVersion, newVersion);

        // upgrade

        LOG.info("onUpgrade() finished");
    }

}
